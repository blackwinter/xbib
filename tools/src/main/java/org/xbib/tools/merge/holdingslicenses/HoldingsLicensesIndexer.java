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
import org.xbib.tools.merge.holdingslicenses.entities.Monograph;
import org.xbib.tools.merge.holdingslicenses.entities.MonographVolume;
import org.xbib.tools.merge.holdingslicenses.entities.TitleRecord;
import org.xbib.tools.merge.holdingslicenses.support.StatCounter;
import org.xbib.util.IndexDefinition;
import org.xbib.util.MultiMap;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.Worker;
import org.xbib.util.concurrent.WorkerRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HoldingsLicensesIndexer<W extends Worker<Pipeline<W,R>, R>, R extends WorkerRequest> {

    private final static Logger logger = LogManager.getLogger(HoldingsLicensesIndexer.class);

    private final static Integer currentYear = LocalDate.now().getYear();

    private final Merger<W, R> merger;

    private final String manifestationsIndex;
    private final String manifestationsIndexType;
    private final String volumesIndex;
    private final String volumesIndexType;
    private final String holdingsIndex;
    private final String holdingsIndexType;
    private final String shelfIndex;
    private final String shelfIndexType;
    private final String servicesIndex;
    private final String servicesIndexType;

    public HoldingsLicensesIndexer(Merger<W, R> merger) {
        this.merger = merger;
        Map<String,IndexDefinition> outputIndexDefinitionMap = merger.getOutputIndexDefinitionMap();
        String indexName = outputIndexDefinitionMap.get("holdingslicenses").getConcreteIndex();
        this.manifestationsIndex = indexName;
        this.manifestationsIndexType = "manifestations";
        this.volumesIndex = indexName;
        this.volumesIndexType = "volumes";
        this.holdingsIndex = indexName;
        this.holdingsIndexType = "holdings";
        this.shelfIndex = indexName;
        this.shelfIndexType = "shelf";
        this.servicesIndex = indexName;
        this.servicesIndexType = "services";
    }

    public void index(TitleRecord titleRecord) throws IOException {
        index(titleRecord, null);
    }

    @SuppressWarnings("unchecked")
    public void index(TitleRecord titleRecord, StatCounter statCounter) throws IOException {
        // first, index related conferences/proceedings/abstracts/...
        if (!titleRecord.getMonographVolumes().isEmpty()) {
            for (MonographVolume volume : titleRecord.getMonographVolumes()) {
                XContentBuilder builder = jsonBuilder();
                boolean isManifestation = buildMonographVolume(builder, volume, statCounter);
                // if ISBN, index to manifestations for top level search
                if (isManifestation) {
                    validateIndex(manifestationsIndex, manifestationsIndexType, volume.getExternalID(), builder);
                } else {
                    validateIndex(volumesIndex, volumesIndexType, volume.getExternalID(), builder);
                }
                MultiMap<String,Holding> mm = volume.getRelatedHoldings();
                for (String key : mm.keySet()) {
                    for (Holding volumeHolding : mm.get(key)){
                        builder = jsonBuilder();
                        buildMonographHolding(builder, volumeHolding);
                        // to holding index
                        String hid = volume.getExternalID();
                        validateIndex(holdingsIndex, holdingsIndexType, hid, builder);
                        if (statCounter != null) {
                            statCounter.increase("stat", "holdings", 1);
                        }
                        String vhid = "(" + volumeHolding.getISIL() + ")" + volume.getExternalID()
                                + (volumeHolding.getFirstDate() != null ? "." + volumeHolding.getFirstDate() : null);
                        validateIndex(shelfIndex, shelfIndexType, vhid, builder);
                        if (statCounter != null) {
                            statCounter.increase("stat", "shelf", 1);
                        }
                    }
                }
            }
            int n = titleRecord.getMonographVolumes().size();
            if (statCounter != null) {
                statCounter.increase("stat", "manifestations", n);
            }
        }
        // holdings and services
        boolean eonly = "online resource".equals(titleRecord.getCarrierType());
        int instcount = 0;
        if (!titleRecord.getRelatedHoldings().isEmpty()) {
            XContentBuilder builder = jsonBuilder();
            builder.startObject()
                    .field("parent", titleRecord.getExternalID());
            if (titleRecord.hasLinks()) {
                builder.field("links", titleRecord.getLinks());
            }
            builder.startArray("institution");
            final MultiMap<String, Holding> holdingsMap = titleRecord.getRelatedHoldings();
            for (String isil : holdingsMap.keySet()) {
                Collection<Holding> holdings = holdingsMap.get(isil);
                if (holdings != null && !holdings.isEmpty()) {
                    instcount++;
                    builder.startObject().field("isil", isil);
                    Set<Integer> priorities = new TreeSet<>();
                    builder.startArray("service");
                    int count = 0;
                    for (Holding holding : holdings) {
                        if (holding.isDeleted()) {
                            continue;
                        }
                        String serviceId = "(" + holding.getISIL() + ")" + holding.getIdentifier();
                        XContentBuilder serviceBuilder = jsonBuilder();
                        buildService(serviceBuilder, holding);
                        validateIndex(servicesIndex, servicesIndexType, serviceId, serviceBuilder);
                        builder.value(serviceId);
                        priorities.add(holding.getPriority());
                        count++;
                        if (!"online resource".equals(holding.carrierType())) {
                            eonly = false;
                        }
                    }
                    builder.endArray()
                            .field("servicecount", count)
                            .array("priorities", priorities)
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
            MultiMap<Integer,Holding> map = titleRecord.getHoldingsByDate();
            for (Integer date : map.keySet()) {
                Collection<Holding> holdings = map.get(date);
                String shelfId = titleRecord.getExternalID() + (date != -1 ? "." + date : "");
                XContentBuilder shelfBuilder = jsonBuilder();
                buildShelf(shelfBuilder, titleRecord, date, holdings);
                validateIndex(shelfIndex, shelfIndexType, shelfId, shelfBuilder);
            }
            if (statCounter != null) {
                statCounter.increase("stat", "shelf", map.size());
            }
            // finally, add one holding per manifestation
            validateIndex(holdingsIndex, holdingsIndexType, titleRecord.getExternalID(), builder);
            if (statCounter != null) {
                statCounter.increase("stat", "holdings", 1);
            }
        }
        if (statCounter != null) {
            statCounter.increase("stat", "manifestations", 1);
        }
        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        buildManifestation(builder, titleRecord, statCounter);
        builder.field("eonly", instcount > 0 && eonly);
        builder.endObject();
        validateIndex(manifestationsIndex, manifestationsIndexType, titleRecord.getExternalID(), builder);
    }

    private void buildManifestation(XContentBuilder builder,
                                    TitleRecord titleRecord,
                                    StatCounter statCounter) throws IOException {
        builder.field("title", titleRecord.getExtendedTitle())
                .field("titlecomponents", titleRecord.getTitleComponents());
        String s = titleRecord.getCorporateName();
        if (s != null) {
            builder.field("corporatename", s);
        }
        s = titleRecord.getMeetingName();
        if (s != null) {
            builder.field("meetingname", s);
        }
        builder.field("country", titleRecord.getCountry())
                .fieldIfNotNull("language", titleRecord.getLanguage())
                .field("publishedat", titleRecord.getPublisherPlace())
                .field("publishedby", titleRecord.getPublisherName())
                .field("openaccess", titleRecord.isOpenAccess())
                .fieldIfNotNull("license", titleRecord.getLicense())
                .field("contenttype", titleRecord.getContentType())
                .field("mediatype", titleRecord.getMediaType())
                .field("carriertype", titleRecord.getCarrierType())
                .fieldIfNotNull("firstdate", titleRecord.getFirstDate())
                .fieldIfNotNull("lastdate", titleRecord.getLastDate());
        Set<Integer> missing = new HashSet<>(titleRecord.getDates());
        Set<Integer> set = titleRecord.getHoldingsByDate().keySet();
        builder.array("dates", set);
        builder.field("current", set.contains(currentYear));
        missing.removeAll(set);
        builder.array("missingdates", missing);
        builder.array("missingdatescount", missing.size());
        if (titleRecord.getGreenInfo() != null) {
            builder.field("green", true);
            builder.startArray("greeninfo");
            for (Map<String,Object> info : titleRecord.getGreenInfo()) {
                builder.map(info);
            }
            builder.endArray();
        } else {
            builder.field("green", false);
        }
        Set<String> isils = titleRecord.getRelatedHoldings().keySet();
        builder.array("isil", isils);
        builder.field("isilcount", isils.size());
        builder.field("identifiers", titleRecord.getIdentifiers());
        builder.field("subseries", titleRecord.isSubseries());
        builder.field("aggregate", titleRecord.isAggregate());
        builder.field("supplement", titleRecord.isSupplement());
        builder.fieldIfNotNull("resourcetype", titleRecord.getResourceType());
        builder.fieldIfNotNull("genre", titleRecord.getGenre());
        if (!titleRecord.getMonographVolumes().isEmpty()) {
            builder.startArray("volumes");
            for (MonographVolume volume : titleRecord.getMonographVolumes()) {
                builder.value(volume.getExternalID());
            }
            builder.endArray();
            builder.field("volumescount", titleRecord.getMonographVolumes().size());
        }
        MultiMap<String, TitleRecord> map = titleRecord.getRelated();
        if (!map.isEmpty()) {
            builder.startArray("relations");
            for (String rel : map.keySet()) {
                for (TitleRecord tr : map.get(rel)) {
                    builder.startObject()
                            .field("identifierForTheRelated", tr.getExternalID())
                            .field("label", rel)
                            .endObject();
                }
            }
            builder.endArray();
        }
        MultiMap<String, String> mm = titleRecord.getRelationsExternalIDs();
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
        if (titleRecord.hasLinks()) {
            builder.array("links", titleRecord.getLinks());
        }
        if (statCounter != null) {
            for (String country : titleRecord.getCountry()) {
                statCounter.increase("country", country, 1);
            }
            statCounter.increase("language", titleRecord.getLanguage(), 1);
            statCounter.increase("contenttype", titleRecord.getContentType(), 1);
            statCounter.increase("mediatype", titleRecord.getMediaType(), 1);
            statCounter.increase("carriertype", titleRecord.getCarrierType(), 1);
            statCounter.increase("resourcetype", titleRecord.getResourceType(), 1);
            statCounter.increase("genre", titleRecord.getGenre(), 1);
        }
    }

    private boolean buildMonographVolume(XContentBuilder builder, MonographVolume monographVolume, StatCounter statCounter)
            throws IOException {
        builder.startObject()
                .array("parents", monographVolume.getParents())
                .field("title", monographVolume.getTitle())
                .field("titlecomponents", monographVolume.getTitleComponents())
                .fieldIfNotNull("firstdate", monographVolume.getFirstDate());
        String s = monographVolume.getCorporateName();
        if (s != null) {
            builder.field("corporateName", s);
        }
        s = monographVolume.getMeetingName();
        if (s != null) {
            builder.field("meetingName", s);
        }
        if (monographVolume.getConference() != null) {
            builder.field("conference");
            builder.map(monographVolume.getConference());
        }
        builder.fieldIfNotNull("volume", monographVolume.getVolumeDesignation())
                .fieldIfNotNull("number", monographVolume.getNumbering())
                .fieldIfNotNull("resourcetype", monographVolume.getResourceType())
                .fieldIfNotNull("genre", monographVolume.getGenre());
        if (monographVolume.getCountry() != null && !monographVolume.getCountry().isEmpty()) {
            builder.field("country", monographVolume.getCountry());
        }
        builder.fieldIfNotNull("language", monographVolume.getLanguage())
                .fieldIfNotNull("publishedat", monographVolume.getPublisherPlace())
                .fieldIfNotNull("publishedby", monographVolume.getPublisherName());
        boolean isManifestation = false;
        if (monographVolume.hasIdentifiers()) {
            Map<String,Object> identifiers =  monographVolume.getIdentifiers();
            builder.field("identifiers", identifiers);
            if (identifiers.get("isbn") != null) {
                isManifestation = true;
            }
        }
        builder.endObject();
        if (statCounter != null) {
            for (String country : monographVolume.getCountry()) {
                statCounter.increase("country", country, 1);
            }
            statCounter.increase("language", monographVolume.getLanguage(), 1);
            // TODO
            //structCounter.increase("contenttype", contentType, 1);
            //structCounter.increase("mediatype", mediaType, 1);
            //structCounter.increase("carriertype", carrierType, 1);
            statCounter.increase("resourcetype", monographVolume.getResourceType(), 1);
            for (String genre : monographVolume.getGenres()) {
                statCounter.increase("genre", genre, 1);
            }
        }
        return isManifestation;
    }

    private void buildMonographHolding(XContentBuilder builder, Holding holding) throws IOException {
        builder.startObject();
        builder.array("parents", holding.getParents())
                .array("date", holding.dates())
                .startObject("institution")
                .field("isil", holding.getISIL())
                .startObject("service")
                .field("mediatype", holding.mediaType())
                .field("carriertype", holding.carrierType())
                .field("region", holding.getRegion())
                .field("organization", holding.getOrganization())
                .field("name", holding.getName())
                .field("isil", holding.getISIL())
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

    private void buildShelf(XContentBuilder builder, TitleRecord titleRecord, Integer date, Collection<Holding> holdings)
            throws IOException {
        builder.startObject();
        if (date != -1) {
            builder.field("date", date);
        }
        if (titleRecord.hasLinks()) {
            builder.field("links", titleRecord.getLinks());
        }
        Map<String, Set<Holding>> institutions = new HashMap<>();
        for (Holding holding : holdings) {
            // collect holdings per institution
            Set<Holding> set = institutions.containsKey(holding.getISIL()) ?
                    institutions.get(holding.getISIL()) : new TreeSet<>();
            set.add(holding);
            institutions.put(holding.getISIL(), set);
        }
        builder.field("institutioncount", institutions.size());
        builder.startArray("institution");
        for (Map.Entry<String,Set<Holding>> entry : institutions.entrySet()) {
            String isil = entry.getKey();
            Collection<Holding> services = entry.getValue();
            builder.startObject()
                    .field("isil", isil)
                    .field("servicecount", services.size());
            builder.startArray("service");
            Set<Integer> priorities = new TreeSet<>();
            for (Holding service : services) {
                if (service.isDeleted()) {
                    continue;
                }
                builder.value("(" + service.getISIL() + ")" + service.getIdentifier());
                priorities.add(service.getPriority());
            }
            builder.endArray();
            builder.array("priorities", priorities);
            builder.endObject();
        }
        builder.endArray();
        builder.endObject();
    }

    private void buildService(XContentBuilder builder, Holding holding)
            throws IOException {
        builder.startObject()
                .array("parents", holding.getParents());
        builder.field("mediatype", holding.mediaType())
                .field("carriertype", holding.carrierType())
                .field("name", holding.getName())
                .field("isil", holding.getISIL())
                .field("region", holding.getRegion())
                .fieldIfNotNull("organization", holding.getOrganization())
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

    public void indexMonograph(Monograph monograph) throws IOException {
        XContentBuilder builder = jsonBuilder();
        boolean isManifestation = buildMonograph(builder, monograph, null);
        // if ISBN, index to manifestations for top level search
        if (isManifestation) {
            validateIndex(manifestationsIndex, manifestationsIndexType, monograph.getExternalID(), builder);
        } else {
            validateIndex(volumesIndex, volumesIndexType, monograph.getExternalID(), builder);
        }
        MultiMap<String,Holding> mm = monograph.getRelatedHoldings();
        for (String key : mm.keySet()) {
            for (Holding volumeHolding : mm.get(key)){
                builder = jsonBuilder();
                buildMonographHolding(builder, volumeHolding);
                // to holding index
                String hid = monograph.getExternalID();
                validateIndex(holdingsIndex, holdingsIndexType, hid, builder);
                String vhid = "(" + volumeHolding.getISIL() + ")" + monograph.getExternalID()
                        + (volumeHolding.getFirstDate() != null ? "." + volumeHolding.getFirstDate() : null);
                validateIndex(shelfIndex, shelfIndexType, vhid, builder);
            }
        }
    }

    private boolean buildMonograph(XContentBuilder builder, Monograph monograph, StatCounter statCounter)
            throws IOException {
        builder.startObject()
                .field("title", monograph.getTitle())
                .field("titlecomponents", monograph.getTitleComponents())
                .fieldIfNotNull("firstdate", monograph.getFirstDate());
        if (monograph.getPerson() != null) {
            builder.startArray("person");
            for (Map<String,Object> map : monograph.getPerson()) {
                builder.map(map);
            }
            builder.endArray();
        }
        if (monograph.getCorporateBody() != null) {
            builder.startArray("corporatebody");
            for (Map<String,Object> map : monograph.getCorporateBody()) {
                builder.map(map);
            }
            builder.endArray();
        }
        String s = monograph.getMeetingName();
        if (s != null) {
            builder.field("meetingName", s);
        }
        if (monograph.getConference() != null) {
            builder.field("conference");
            builder.map(monograph.getConference());
        }
        builder.fieldIfNotNull("volume", monograph.getVolumeDesignation())
                .fieldIfNotNull("number", monograph.getNumbering())
                .fieldIfNotNull("resourcetype", monograph.getResourceType())
                .fieldIfNotNull("genre", monograph.getGenre());
        if (monograph.getCountry() != null && !monograph.getCountry().isEmpty()) {
            builder.field("country", monograph.getCountry());
        }
        builder.fieldIfNotNull("language", monograph.getLanguage())
                .fieldIfNotNull("publishedat", monograph.getPublisherPlace())
                .fieldIfNotNull("publishedby", monograph.getPublisherName());
        boolean isManifestation = false;
        if (monograph.hasIdentifiers()) {
            Map<String,Object> identifiers =  monograph.getIdentifiers();
            builder.field("identifiers", identifiers);
            if (identifiers.get("isbn") != null) {
                isManifestation = true;
            }
        }
        builder.endObject();
        if (statCounter != null) {
            for (String country : monograph.getCountry()) {
                statCounter.increase("country", country, 1);
            }
            statCounter.increase("language", monograph.getLanguage(), 1);
            // TODO
            //structCounter.increase("contenttype", contentType, 1);
            //structCounter.increase("mediatype", mediaType, 1);
            //structCounter.increase("carriertype", carrierType, 1);
            statCounter.increase("resourcetype", monograph.getResourceType(), 1);
            for (String genre : monograph.getGenres()) {
                statCounter.increase("genre", genre, 1);
            }
        }
        return isManifestation;
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
