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
package org.xbib.elasticsearch.tools.aggregate.zdb.entities;

import com.google.common.base.Supplier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

import org.elasticsearch.common.collect.Sets;
import org.xbib.date.DateUtil;
import org.xbib.grouping.bibliographic.endeavor.PublishedJournal;
import org.xbib.map.MapBasedAnyObject;
import org.xbib.util.Strings;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Manifestation extends MapBasedAnyObject
        implements Comparable<Manifestation> {

    private final DecimalFormat df = new DecimalFormat("0000");

    private final String id;

    private final String externalID;

    private final String key;

    private String title;

    private String corporate;

    private String meeting;

    private final String publisher;

    private final String publisherPlace;

    private final String language;

    private List<String> country;

    private final Integer firstDate;

    private final Integer lastDate;

    private final String description;

    private boolean isPartial;

    private String printID;

    private String onlineID;

    private String printExternalID;

    private String onlineExternalID;

    private String contentType;

    private String mediaType;

    private String carrierType;

    private boolean isSupplement;

    private String supplementID;

    private String supplementExternalID;

    private String unique;

    private List links;

    private final Map<String,Object> identifiers;

    private SetMultimap<String, String> relations;

    private SetMultimap<String, Manifestation> relatedManifestations;

    public Manifestation(Map<String, Object> m) {
        super(m);
        // we use DNB ID. ZDB ID collides with GND ID. Example: 21573803
        this.id = getString("IdentifierDNB.identifierDNB");
        this.externalID = getString("IdentifierZDB.identifierZDB");

        this.isPartial = getString("TitleStatement.titlePartName") != null
                || getString("TitleStatement.titlePartNumber") != null;

        makeTitle();
        makeCorporate();
        makeMeeting();

        this.publisher = getString("PublicationStatement.publisherName");
        this.publisherPlace = getPublisherPlace();
        this.language = getString("Language.value", "unknown");
        findCountry();
        Integer firstDate = getInteger("date1");
        this.firstDate = firstDate == null ? null : firstDate == 9999 ? null : firstDate;
        Integer lastDate = getInteger("date2");
        this.lastDate = lastDate == null ? null : lastDate == 9999 ? null : lastDate;
        findLinks();
        this.description = getString("DatesOfPublication.value");
        // recognize supplement
        this.isSupplement = "isSupplementOf".equals(getString("SupplementParentEntry.relation"));
        if (isSupplement) {
            this.supplementID = getString("SupplementParentEntry.identifierDNB");
            this.supplementExternalID = getString("SupplementParentEntry.identifierZDB");
        } else {
            this.isSupplement = "isSupplementOf".equals(getString("SupplementSpecialEditionEntry.relation"));
            if (isSupplement) {
                this.supplementID = getString("SupplementSpecialEditionEntry.identifierDNB");
                this.supplementExternalID = getString("SupplementSpecialEditionEntry.identifierZDB");
            }
        }
        // first, compute content types
        computeContentTypes();
        // last, compute key
        this.key = computeKey();

        // ISSN, CODEN, ...
        this.identifiers = makeIdentifiers();

        this.relations = makeRelations();

        this.relatedManifestations = newRelatedManifestations();

        // unique identifier
        StringBuilder p = new StringBuilder();
        if (publisher != null) {
            p.append(publisher);
        }
        if (publisherPlace != null && !publisherPlace.isEmpty()) {
            p.append('-').append(publisherPlace);
        }
        this.unique = new PublishedJournal()
                .journalName(title)
                .publisherName(p.toString())
                .createIdentifier();
    }

    public String id() {
        return id;
    }

    public String externalID() {
        return externalID;
    }

    public Manifestation contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String contentType() {
        return contentType;
    }

    public Manifestation mediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public String mediaType() {
        return mediaType;
    }

    public Manifestation carrierType(String carrierType) {
        this.carrierType = carrierType;
        return this;
    }

    public String carrierType() {
        return carrierType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    public String corporateName() {
        return corporate;
    }

    public String meetingName() {
        return meeting;
    }

    public String publisher() {
        return publisher;
    }

    public String publisherPlace() {
        return publisherPlace;
    }

    public String language() {
        return language;
    }

    public List<String> country() {
        return country;
    }

    public Integer firstDate() {
        return firstDate;
    }

    public Integer lastDate() {
        return lastDate;
    }

    public String description() {
        return description;
    }

    public boolean isSupplement() {
        return isSupplement;
    }

    public String supplementID() {
        return supplementID;
    }

    public String supplementExternalID() {
        return supplementExternalID;
    }

    public boolean isPartial() {
        return isPartial;
    }

    public boolean hasPrint() {
        return id.equals(onlineID) && printID != null;
    }

    public boolean hasOnline() {
        return id.equals(printID) && onlineID != null;
    }

    public String getPrintID() {
        return printID;
    }

    public String getOnlineID() {
        return onlineID;
    }

    public String getPrintExternalID() {
        return printExternalID;
    }

    public String getOnlineExternalID() {
        return onlineExternalID;
    }

    public Map<String,Object> getIdentifiers() {
        return identifiers.isEmpty() ? null : identifiers;
    }

    public void setLinks(List<Map<String,Object>> links) {
        this.links = links;
    }

    public List<Map<String,Object>> getLinks() {
        return links;
    }

    public Manifestation addRelation(String key, String value) {
        relations.put(key, value);
        return this;
    }

    public SetMultimap<String, String> relations() {
        return relations;
    }

    public Set<String> relation(String key) {
        return relations.get(key);
    }

    public boolean hasCarrierRelations() {
        for (String key : relations.keys()) {
            if (carrierEditions.contains(key)) {
                return true;
            }
        }
        return false;
    }


    public Map<String,Object> cleanTitle() {
        Map<String, Object> m = (Map<String, Object>) map().get("TitleStatement");
        if (m != null) {
            String titleMedium = (String) m.get("titleMedium");
            if ("[Elektronische Ressource]".equals(titleMedium)) {
                m.remove("titleMedium");
            }
        }
        return m;
    }

    public String getUniqueIdentifier() {
        return unique;
    }

    private void makeTitle() {
        // shorten title (series statement after '/' or ':')
        String title = cleanTitle(getString("TitleStatement.titleMain"));
        if (isPartial) {
            String partName = cleanTitle(getString("TitleStatement.titlePartName"));
            if (!Strings.isNullOrEmpty(partName)) {
                title += " / " + partName;
            }
            String partNumber = cleanTitle(getString("TitleStatement.titlePartNumber"));
            if (!Strings.isNullOrEmpty(partNumber)) {
                title += " / " + partNumber;
            }
        }
        setTitle(title);
    }

    private String cleanTitle(String title) {
        if (title == null) {
            return null;
        }
        int pos = title.indexOf('/');
        if (pos > 0) {
            title = title.substring(0, pos - 1);
        }
        pos = title.indexOf(':');
        if (pos > 0) {
            title = title.substring(0, pos - 1);
        }
        title = title.replaceAll("\\[.*?\\]","").trim();
        return title;
    }

    private void makeCorporate() {
        this.corporate = getString("CorporateName.corporateName");
    }

    private void makeMeeting() {
        this.meeting = getString("MeetingName.meetingName");
    }

    private String getPublisherPlace() {
        Object o = map().get("PublicationStatement");
        if (o == null) {
            return "";
        }
        if (!(o instanceof List)) {
            o = Arrays.asList(o);
        }
        StringBuilder sb = new StringBuilder();
        List<Map<String,Object>> list = (List<Map<String,Object>>)o;
        for (Map<String,Object> m : list) {
            o = m.get("placeOfPublication");
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            List<String> l = (List<String>)o;
            for (String s : l) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(s);
            }
        }
        return sb.toString();
    }

    private void findLinks() {
        Object o = map().get("ElectronicLocationAndAccess");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            this.links = (List)o;
            return;
        }
        this.links = Collections.EMPTY_LIST;
    }

    private void computeContentTypes() {
        Object o = map().get("physicalDescriptionElectronicResource");
        if (o != null) {
            if (o instanceof List) {
                List l = (List) o;
                this.contentType = "text";
                this.mediaType = "computer";
                this.carrierType = l.iterator().next().toString();
                return;
            } else {
                this.contentType = "text";
                this.mediaType = "computer";
                this.carrierType = "online resource".equals(o.toString()) ? "online resource" : "computer disc";
                return;
            }
        }
        // microform (plus unmediated text)
        if (map().containsKey("physicalDescriptionMicroform")) {
            this.contentType = "text";
            this.mediaType = "microform";
            this.carrierType = "other";
            return;
        }
        // before assuming unmediated text, check title strings for media phrases
        String[] phraseTitles = new String[]{
                getString("AdditionalPhysicalFormNote.value"),
                getString("otherCodes.genre"),
                getString("TitleStatement.titleMedium"),
                getString("TitleStatement.titlePartName"),
                getString("Note.value")
        };
        for (String s : phraseTitles) {
            if (s != null) {
                for (String t : ER) {
                    if (s.contains(t)) {
                        this.contentType = "text";
                        this.mediaType = "computer";
                        this.carrierType = "online resource";
                        return;
                    }
                }
                for (String t : MF) {
                    if (s.contains(t)) {
                        this.contentType = "text";
                        this.mediaType = "microform";
                        this.carrierType = "other";
                        return;
                    }
                }
            }
        }
        // default
        this.contentType = "text";
        this.mediaType = "unmediated";
        this.carrierType = "volume";
    }

    private final String[] ER = new String[]{
            "Elektronische Ressource"
    };

    private final String[] MF = new String[]{
            "Mikroform",
            "Microfiche",
            "secondary-microform"
    };

    private String computeKey() {
        StringBuilder sb = new StringBuilder();
        // precedence for text/unmediated/volume
        // contentType
        switch (contentType) {
            case "text": {
                sb.append("0");
                break;
            }
            default: { // non-text
                sb.append("1");
            }
        }
        // mediaType
        switch (mediaType) {
            case "unmediated": {
                sb.append("0");
                break;
            }
            default: { // microform, computer
                sb.append("1");
            }
        }
        // carrierType
        switch (carrierType) {
            case "volume": {
                sb.append("0");
                break;
            }
            default: { // online resource, computer disc, other
                sb.append("1");
                break;
            }
        }
        int delta;
        int d1;
        int d2 = 0;
        try {
            d1 = firstDate == null ? DateUtil.getYear() : firstDate;
            d2 = lastDate == null ? DateUtil.getYear() : lastDate;
            delta = d2 - d1;
        } catch (NumberFormatException e) {
            delta = 0;
        }
        return df.format(d2) + df.format(delta) + sb.toString();
    }

    private void findCountry() {
        Object o = getAnyObject("publishingCountry.isoCountryCodesSource");
        if (o instanceof List) {
            this.country = (List<String>)o;
        } else if (o instanceof String) {
            List<String> l = new ArrayList();
            l.add((String)o);
            this.country = l;
        } else {
            List<String> l = new ArrayList();
            l.add("unknown");
            this.country = l;
        }
    }

    private Map<String,Object> makeIdentifiers() {
        Map<String,Object> m = new HashMap();
        // get and convert all ISSN
        Object o = map().get("IdentifierISSN");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            List<String> issns = new ArrayList();
            List<Map<String,Object>> l = (List<Map<String,Object>>)o;
            for (Map<String, Object> aL : l) {
                String s = (String) aL.get("value");
                if (s != null) {
                    issns.add(s.replaceAll("\\-", "").toLowerCase());
                }
            }
            m.put("issn", issns);
        }
        // get CODEN
        o = map().get("IdentifierCODEN");
        if (o != null) {
            m.put("coden", o);
        }
        return m;
    }

    private final static Supplier<Set<String>> supplier = new Supplier<Set<String>>() {

        @Override
        public Set<String> get() {
            return Sets.newLinkedHashSet();
        }
    };

    private SetMultimap<String, String> makeRelations() {
        Map<String, Collection<String>> map = Maps.newTreeMap();
        SetMultimap<String, String> multi = Multimaps.newSetMultimap(map, supplier);
        for (String entry : relationEntries) {
            Object o = map().get(entry);
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            for (Object s : (List) o) {
                Map<String, Object> m = (Map<String, Object>) s;
                String value = (String)m.get("identifierDNB");
                String key = (String)m.get("relation");
                if (key != null && value != null) {
                    multi.put(key, value);
                    if ("hasOnlineEdition".equals(key)) {
                        this.printID = id();
                        this.printExternalID = externalID();
                        this.onlineID = value;
                        this.onlineExternalID = (String)m.get("identifierZDB");
                    } else if ("hasPrintEdition".equals(key)) {
                        this.onlineID = id();
                        this.onlineExternalID = externalID();
                        this.printID = value;
                        this.printExternalID =  (String)m.get("identifierZDB");
                    }
                }
            }
        }
        return multi;
    }

    private final static Set<String> relationEntries = Sets.newHashSet(
            "PrecedingEntry",
            "SucceedingEntry",
            "OtherEditionEntry",
            "OtherRelationshipEntry",
            "SupplementSpecialIssueEntry",
            "SupplementParentEntry" // verbindet Titel auch mit (Datenbank)werken über "In" = "isPartOf" --> Work/Work
    );

    public static Set<String> relationEntries() {
        return relationEntries;
    }

    private final static Set<String> carrierEditions =  Sets.newHashSet(
            "hasPrintEdition",
            "hasOnlineEdition",
            "hasBrailleEdition",
            "hasCDEdition",
            "hasDVDEdition",
            "hasMicroformEdition",
            "hasDigitizedEdition"
    );

    public static Set<String> carrierEditions() {
        return carrierEditions;
    }

    private SetMultimap<String, Manifestation> newRelatedManifestations() {
        return HashMultimap.create();
    }

    public void addRelatedManifestation(String relation, Manifestation manifestation) {
        relatedManifestations.put(relation, manifestation);
    }

    public SetMultimap<String, Manifestation> getRelatedManifestations() {
        return relatedManifestations;
    }

    public String getKey() {
        return key;
    }

    public String toString() {
        return externalID;
    }

    @Override
    public boolean equals(Object other) {
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(Manifestation m) {
        return externalID.compareTo(m.externalID());
    }
    private final static IDComparator idComparator = new IDComparator();

    private static class IDComparator implements Comparator<Manifestation> {

        @Override
        public int compare(Manifestation m1, Manifestation m2) {
            if (m1 == m2) {
                return 0;
            }
            return m2.id().compareTo(m1.id());
        }
    }

    public static Comparator<Manifestation> getIdComparator() {
        return idComparator;
    }


}

