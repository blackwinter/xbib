/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.tools.merge.holdingslicenses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.tools.merge.Merger;
import org.xbib.tools.merge.holdingslicenses.entities.Holding;
import org.xbib.tools.merge.holdingslicenses.entities.MonographVolume;
import org.xbib.tools.merge.holdingslicenses.entities.SerialRecord;
import org.xbib.tools.merge.holdingslicenses.support.StatCounter;
import org.xbib.util.IndexDefinition;
import org.xbib.util.MultiMap;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HoldingsLicensesIndexer {

    private final static Logger logger = LogManager.getLogger(HoldingsLicensesIndexer.class);

    private final static Integer currentYear = LocalDate.now().getYear();

    private final Merger merger;

    private final String manifestationsIndex;
    private final String manifestationsIndexType;
    private final String holdingsIndex;
    private final String holdingsIndexType;
    private final String volumesIndex;
    private final String volumesIndexType;
    private final String servicesIndex;
    private final String servicesIndexType;

    public HoldingsLicensesIndexer(Merger merger) {
        this.merger = merger;
        Map<String,IndexDefinition> outputIndexDefinitionMap = merger.getOutputIndexDefinitionMap();
        String indexName = outputIndexDefinitionMap.get("holdingslicenses").getConcreteIndex();
        this.manifestationsIndex = indexName;
        this.manifestationsIndexType = "manifestations";
        this.holdingsIndex = indexName;
        this.holdingsIndexType = "holdings";
        this.volumesIndex = indexName;
        this.volumesIndexType = "volumes";
        this.servicesIndex = indexName;
        this.servicesIndexType = "services";
    }

    public void index(SerialRecord serialRecord) throws IOException {
        index(serialRecord, null);
    }

    @SuppressWarnings("unchecked")
    public void index(SerialRecord serialRecord, StatCounter statCounter) throws IOException {
        // first, index related conference/proceedings/abstracts/...
        if (!serialRecord.getMonographVolumes().isEmpty()) {
            for (MonographVolume volume : serialRecord.getMonographVolumes()) {
                XContentBuilder builder = jsonBuilder();
                buildMonographVolume(builder, volume, statCounter);
                validateIndex(manifestationsIndex, manifestationsIndexType, volume.externalID(), builder);
                MultiMap<String,Holding> mm = volume.getRelatedHoldings();
                for (String key : mm.keySet()) {
                    for (Holding volumeHolding : mm.get(key)){
                        builder = jsonBuilder();
                        buildMonographHolding(builder, volumeHolding);
                        // to holding index
                        String hid = volume.externalID();
                        validateIndex(holdingsIndex, holdingsIndexType, hid, builder);
                        if (statCounter != null) {
                            statCounter.increase("stat", "holdings", 1);
                        }
                        // extra entry by date
                        String vhid = "(" + volumeHolding.getServiceISIL() + ")" + volume.externalID()
                                + (volumeHolding.getFirstDate() != null ? "." + volumeHolding.getFirstDate() : null);
                        validateIndex(volumesIndex, volumesIndexType, vhid, builder);
                        if (statCounter != null) {
                            statCounter.increase("stat", "volumes", 1);
                        }
                    }
                }
            }
            int n = serialRecord.getMonographVolumes().size();
            if (statCounter != null) {
                statCounter.increase("stat", "manifestations", n);
            }
        }
        // write holdings and services
        if (!serialRecord.getRelatedHoldings().isEmpty()) {
            XContentBuilder builder = jsonBuilder();
            builder.startObject()
                    .field("parent", serialRecord.externalID());
            if (serialRecord.hasLinks()) {
                builder.field("links", serialRecord.getLinks());
            }
            builder.startArray("institution");
            int instcount = 0;
            final MultiMap<String, Holding> holdingsMap = serialRecord.getRelatedHoldings();
            for (String isil : holdingsMap.keySet()) {
                Collection<Holding> holdings = holdingsMap.get(isil);
                if (holdings != null && !holdings.isEmpty()) {
                    instcount++;
                    builder.startObject().field("isil", isil);
                    builder.startArray("service");
                    int count = 0;
                    for (Holding holding : holdings) {
                        if (holding.isDeleted()) {
                            continue;
                        }
                        String serviceId = "(" + holding.getServiceISIL() + ")" + holding.identifier();
                        XContentBuilder serviceBuilder = jsonBuilder();
                        buildService(serviceBuilder, holding);
                        validateIndex(servicesIndex,
                                servicesIndexType,
                                serviceId,
                                serviceBuilder);
                        builder.value(serviceId);
                        count++;
                    }
                    builder.endArray()
                            .field("servicecount", count)
                            .endObject();
                    if (statCounter != null) {
                        statCounter.increase("stat", "services", count);
                    }
                }
            }
            builder.endArray();
            builder.field("institutioncount", instcount);
            builder.endObject();
            // now, build holdings per year
            MultiMap<Integer,Holding> map = serialRecord.getHoldingsByDate();
            for (Integer date : map.keySet()) {
                Collection<Holding> holdings = map.get(date);
                String volumeId = serialRecord.externalID() + (date != -1 ? "." + date : "");
                XContentBuilder volumeBuilder = jsonBuilder();
                buildVolume(volumeBuilder, serialRecord, date, holdings);
                validateIndex(volumesIndex,
                        volumesIndexType,
                        volumeId, volumeBuilder);
            }
            if (statCounter != null) {
                statCounter.increase("stat", "volumes", map.size());
            }
            // finally, add one holding per manifestation
            validateIndex(holdingsIndex,
                    holdingsIndexType,
                    serialRecord.externalID(),
                    builder);
            if (statCounter != null) {
                statCounter.increase("stat", "holdings", 1);
            }
        }
        if (statCounter != null) {
            statCounter.increase("stat", "manifestations", 1);
        }
        XContentBuilder builder = jsonBuilder();
        buildManifestation(builder, serialRecord, statCounter);
        validateIndex(manifestationsIndex,
                manifestationsIndexType,
                serialRecord.externalID(), builder);
    }

    private void buildManifestation(XContentBuilder builder,
                                    SerialRecord serialRecord,
                                    StatCounter statCounter) throws IOException {
        builder.startObject();
        builder.field("title", serialRecord.getExtendedTitle())
                .field("titlecomponents", serialRecord.getTitleComponents());
        String s = serialRecord.corporateName();
        if (s != null) {
            builder.field("corporatename", s);
        }
        s = serialRecord.meetingName();
        if (s != null) {
            builder.field("meetingname", s);
        }
        builder.field("country", serialRecord.country())
                .fieldIfNotNull("language", serialRecord.language())
                .field("publishedat", serialRecord.getPublisherPlace())
                .field("publishedby", serialRecord.getPublisher())
                .field("monographic", serialRecord.isMonographic())
                .field("openaccess", serialRecord.isOpenAccess())
                .fieldIfNotNull("license", serialRecord.getLicense())
                .field("contenttype", serialRecord.contentType())
                .field("mediatype", serialRecord.mediaType())
                .field("carriertype", serialRecord.carrierType())
                .fieldIfNotNull("firstdate", serialRecord.firstDate())
                .fieldIfNotNull("lastdate", serialRecord.lastDate());
        Set<Integer> missing = new HashSet<>(serialRecord.getDates());
        Set<Integer> set = serialRecord.getHoldingsByDate().keySet();
        builder.array("dates", set);
        builder.field("current", set.contains(currentYear));
        missing.removeAll(set);
        builder.array("missingdates", missing);
        builder.array("missingdatescount", missing.size());
        builder.field("greendate", serialRecord.getGreenDates());
        builder.field("greendatecount", serialRecord.getGreenDates().size());
        Set<String> isils = serialRecord.getRelatedHoldings().keySet();
        builder.array("isil", isils);
        builder.field("isilcount", isils.size());
        builder.field("identifiers", serialRecord.getIdentifiers());
        builder.field("subseries", serialRecord.isSubseries());
        builder.field("aggregate", serialRecord.isAggregate());
        builder.field("supplement", serialRecord.isSupplement());
        builder.fieldIfNotNull("resourcetype", serialRecord.resourceType());
        builder.fieldIfNotNull("genre", serialRecord.genre());
        MultiMap<String, SerialRecord> map = serialRecord.getRelated();
        if (!map.isEmpty()) {
            builder.startArray("relations");
            for (String rel : map.keySet()) {
                for (SerialRecord tr : map.get(rel)) {
                    builder.startObject()
                            .field("identifierForTheRelated", tr.externalID())
                            .field("label", rel)
                            .endObject();
                }
            }
            builder.endArray();
        }
        MultiMap<String, String> mm = serialRecord.getExternalRelations();
        if (!mm.isEmpty()) {
            builder.startArray("relations");
            for (String rel : mm.keySet()) {
                for (String relid : mm.get(rel)) {
                    builder.startObject()
                            .field("identifierForTheRelated", relid)
                            .field("label", rel)
                            .endObject();
                }
            }
            builder.endArray();
        }
        if (serialRecord.hasLinks()) {
            builder.array("links", serialRecord.getLinks());
        }
        builder.endObject();
        if (statCounter != null) {
            for (String country : serialRecord.country()) {
                statCounter.increase("country", country, 1);
            }
            statCounter.increase("language", serialRecord.language(), 1);
            statCounter.increase("contenttype", serialRecord.contentType(), 1);
            statCounter.increase("mediatype", serialRecord.mediaType(), 1);
            statCounter.increase("carriertype", serialRecord.carrierType(), 1);
            statCounter.increase("resourcetype", serialRecord.resourceType(), 1);
            statCounter.increase("genre", serialRecord.genre(), 1);
        }
    }

    private void buildMonographVolume(XContentBuilder builder, MonographVolume monographVolume, StatCounter statCounter)
            throws IOException {
        builder.startObject()
                .array("parents", monographVolume.parents())
                .field("title", monographVolume.getTitle())
                .field("titlecomponents", monographVolume.getTitleComponents())
                .fieldIfNotNull("firstdate", monographVolume.firstDate());
        String s = monographVolume.corporateName();
        if (s != null) {
            builder.field("corporateName", s);
        }
        s = monographVolume.meetingName();
        if (s != null) {
            builder.field("meetingName", s);
        }
        if (monographVolume.conference() != null) {
            builder.field("conference");
            builder.map(monographVolume.conference());
        }
        builder.fieldIfNotNull("volume", monographVolume.getVolumeDesignation())
                .fieldIfNotNull("number", monographVolume.getNumbering())
                .fieldIfNotNull("resourcetype", monographVolume.resourceType())
                .fieldIfNotNull("genre", monographVolume.genre());
        if (monographVolume.country() != null && !monographVolume.country().isEmpty()) {
            builder.field("country", monographVolume.country());
        }
        builder.fieldIfNotNull("language", monographVolume.language())
                .fieldIfNotNull("publishedat", monographVolume.getPublisherPlace())
                .fieldIfNotNull("publishedby", monographVolume.getPublisher());
        if (monographVolume.hasIdentifiers()) {
            builder.field("identifiers", monographVolume.getIdentifiers());
        }
        builder.endObject();
        if (statCounter != null) {
            for (String country : monographVolume.country()) {
                statCounter.increase("country", country, 1);
            }
            statCounter.increase("language", monographVolume.language(), 1);
            // TODO
            //structCounter.increase("contenttype", contentType, 1);
            //structCounter.increase("mediatype", mediaType, 1);
            //structCounter.increase("carriertype", carrierType, 1);
            statCounter.increase("resourcetype", monographVolume.resourceType(), 1);
            for (String genre : monographVolume.genres()) {
                statCounter.increase("genre", genre, 1);
            }
        }
    }

    private void buildMonographHolding(XContentBuilder builder, Holding holding) throws IOException {
        builder.startObject();
        builder.array("parents", holding.parents())
                .array("date", holding.dates())
                .startObject("institution")
                .field("isil", holding.getISIL())
                .startObject("service")
                .field("mediatype", holding.mediaType())
                .field("carriertype", holding.carrierType())
                .field("region", holding.getRegion())
                .field("organization", holding.getOrganization())
                .field("name", holding.getName())
                .field("isil", holding.getServiceISIL())
                .field("serviceisil", holding.getServiceISIL())
                .field("priority", holding.getPriority())
                .field("type", holding.getServiceType());
        Object o = holding.getServiceMode();
        if (o instanceof List) {
            builder.array("mode", (List) o);
        } else {
            builder.field("mode", o);
        }
        o = holding.getServiceDistribution();
        if (o instanceof List) {
            builder.array("distribution", (List) o);
        } else {
            builder.field("distribution", o);
        }
        builder.startObject("info")
                .startObject("location")
                // https://www.hbz-nrw.de/dokumentencenter/produkte/verbunddatenbank/aktuell/plausi/Exemplar-Online-Kurzform.pdf
                .fieldIfNotNull("collection", holding.map().get("shelfmark")) // 088 b sublocation (Standort)
                .fieldIfNotNull("callnumber", holding.map().get("callnumber")) // 088 c (Signatur)
                //.fieldIfNotNull("collection", map.get("collection")) // 088 d zus. Bestandsangabe (nicht vorhanden)
                .endObject();
        builder.endObject();
        builder.field("current", holding.dates().contains(currentYear));
        builder.endObject();
    }

    private void buildVolume(XContentBuilder builder,
                             SerialRecord serialRecord,
                             Integer date,
                             Collection<Holding> holdings)
            throws IOException {
        builder.startObject();
        if (date != -1) {
            builder.field("date", date);
        }
        if (serialRecord.hasLinks()) {
            builder.field("links", serialRecord.getLinks());
        }
        Map<String, Set<Holding>> institutions = new HashMap<>();
        for (Holding holding : holdings) {
            // create holdings in order
            Set<Holding> set = institutions.containsKey(holding.getISIL()) ?
                    institutions.get(holding.getISIL()) : new TreeSet<>();
            set.add(holding);
            institutions.put(holding.getISIL(), set);
        }
        builder.field("institutioncount", institutions.size());
        builder.startArray("institution");
        for (Map.Entry<String,Set<Holding>> entry : institutions.entrySet()) {
            String isil = entry.getKey();
            Collection<Holding> set = entry.getValue();
            builder.startObject()
                    .field("isil", isil)
                    .field("servicecount", set.size());
            builder.startArray("service");
            for (Holding holding : set) {
                if (holding.isDeleted()) {
                    continue;
                }
                builder.value("(" + holding.getServiceISIL() + ")" + holding.identifier());
            }
            builder.endArray();
            builder.endObject();
        }
        builder.endArray();
        builder.endObject();
    }

    private void buildService(XContentBuilder builder, Holding holding)
            throws IOException {
        builder.startObject()
                .array("parents", holding.parents());
        builder.field("mediatype", holding.mediaType())
                .field("carriertype", holding.carrierType())
                .field("name", holding.getName())
                .field("isil", holding.getISIL())
                .field("region", holding.getRegion())
                .fieldIfNotNull("organization", holding.getOrganization())
                .field("serviceisil", holding.getServiceISIL())
                .field("priority", holding.getPriority())
                .fieldIfNotNull("type", holding.getServiceType());
        Object o = holding.getServiceMode();
        if (o instanceof List) {
            builder.array("mode", (List) o);
        } else {
            builder.fieldIfNotNull("mode", o);
        }
        o = holding.getServiceDistribution();
        if (o instanceof List) {
            builder.array("distribution", (List) o);
        } else {
            builder.fieldIfNotNull("distribution", o);
        }
        builder.fieldIfNotNull("comment", holding.getServiceComment())
                .field("info", holding.getInfo())
                .endObject();
    }

    private XContentBuilder jsonBuilder() throws IOException {
        if (merger.settings().getAsBoolean("mock", false)) {
            return org.xbib.common.xcontent.XContentService.jsonBuilder().prettyPrint();
        } else {
            return org.xbib.common.xcontent.XContentService.jsonBuilder();
        }
    }

    private void validateIndex(String index, String type, String id, XContentBuilder builder) throws IOException {
        long len = builder.string().length();
        if (len > 1024 * 1024) {
            logger.warn("large document {}/{}/{} detected: {} bytes", index, type, id, len);
            return;
        }
        if (merger.settings().getAsBoolean("mock", false)) {
            logger.debug("{}/{}/{} {}", index, type, id, builder.string());
            return;
        }
        merger.ingest().index(index, type, id, builder.string());
    }
}
