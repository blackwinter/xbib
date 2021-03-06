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
package org.xbib.tools.merge.holdingslicenses.entities;

import org.xbib.tools.merge.holdingslicenses.support.NaturalOrderComparator;
import org.xbib.util.LinkedHashMultiMap;
import org.xbib.util.MultiMap;
import org.xbib.common.Strings;
import org.xbib.util.TreeMultiMap;

import java.time.Year;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("unchecked")
public class TitleRecord implements Comparable<TitleRecord> {

    private final static Integer currentYear = Year.now().getValue();

    protected final Map<String, Object> map;

    protected String identifier;

    protected String externalID;

    protected String title;

    private String extendedTitle;

    protected Set<String> titleComponents;

    protected String corporate;

    protected String meeting;

    protected String publisherName;

    protected String publisherPlace;

    protected String language;

    protected List<String> country;

    protected Integer firstDate;

    protected Integer lastDate;

    private Set<Integer> dates;

    private List<Map<String,Object>> greenInfo;

    protected Map<String, Object> identifiers;

    protected String resourceType;

    private boolean isDatabase;

    private boolean isPacket;

    private boolean isNewspaper;

    private boolean isWebsite;

    private boolean isSubseries;

    private boolean isSupplement;

    private boolean isBibliography;

    private boolean isAggregate;

    private boolean openAccess;

    private String genre;

    private String license;

    private String contentType;

    private String mediaType;

    private String carrierType;

    private List<Map<String, Object>> links;

    private final MultiMap<String, String> relations = new TreeMultiMap<>();

    private final MultiMap<String, String> externalRelations = new TreeMultiMap();

    private final MultiMap<String, TitleRecord> relatedRecords = new LinkedHashMultiMap<>();

    private final MultiMap<String, Holding> relatedHoldings = new LinkedHashMultiMap<>();

    private final MultiMap<Integer, Holding> holdingsByDate =  new LinkedHashMultiMap<>();

    private final Collection<MonographVolume> monographVolumes = new TreeSet(new NaturalOrderComparator<MonographVolume>());

    public TitleRecord(Map<String, Object> map) {
        this.map = map;
        build();
    }

    protected void build() {
        // we use DNB ID. ZDB ID collides with GND ID. Example: 21573803
        String s = getString("IdentifierDNB.identifierDNB");
        // DNB ID always upper case
        this.identifier = s != null ? s.toUpperCase() : "undefined";
        s = getString("IdentifierZDB.identifierZDB");
        this.externalID = s != null ? s : "undefined";
        this.isSubseries = getString("TitleStatement.titlePartName") != null
                || getString("TitleStatement.titlePartNumber") != null;
        makeCorporate();
        makeMeeting();
        makeTitle();
        this.publisherName = findPublisherName();
        this.publisherPlace = findPublisherPlace();
        this.language = getString("Language.valueSource");
        findCountry();
        Integer firstDate = getInteger("date1");
        this.firstDate = firstDate == null ? null : firstDate == 9999 ? null : firstDate;
        Integer lastDate = getInteger("date2");
        this.lastDate = lastDate == null ? null : lastDate == 9999 ? null : lastDate;
        this.dates = getIntegerSet("Dates");
        findLinks();
        findSupplement();
        this.genre = getString("OtherCodes.genre");
        this.resourceType = getString("typeOfContinuingResource");
        this.isWebsite = "Updating Web site".equals(resourceType);
        this.isDatabase = "Updating database".equals(resourceType);
        this.isNewspaper = "Newspaper".equals(resourceType);
        String natureOfContent = getString("natureOfContent");
        this.isBibliography = "Bibliographies".equals(natureOfContent);
        String genreCode = getString("OtherCodes.genreSource");
        this.isPacket = "pt".equals(genreCode);
        boolean isAgg = "ag".equals(genreCode);
        this.isAggregate = isWebsite || isDatabase || isPacket || isNewspaper || isBibliography || isAgg;
        computeContentTypes();
        makeIdentifiers();
        makeRelations();
    }

    private <T> T get(String key) {
        return this.<T>get(map, key.split("\\."));
    }

    private <T> T get(Map m, String[] key) {
        if (m == null) {
            return null;
        }
        Object o = m.get(key[0]);
        if (o instanceof List) {
            o = ((List) o).get(0);
        }
        if (o instanceof Map && key.length > 1) {
            return get((Map) o, Arrays.copyOfRange(key, 1, key.length));
        }
        return (T)o;
    }

    private Object get(String key, Object defValue) {
        Object o = get(key);
        return (o != null) ? o : defValue;
    }

    protected String getString(String key) {
        return get(key);
    }

    protected String getString(String key, String defValue) {
        return (String) get(key, defValue);
    }

    protected <T> T getAnyObject(String key) {
        return get(key);
    }

    protected Integer getInteger(String key) {
        try {
            Object o = get(key);
            return o == null ? null : o instanceof Integer ? (Integer) o : Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Set<Integer> getIntegerSet(String key) {
        Object o = map.get(key);
        if (o instanceof Collection) {
            return new HashSet<>((Collection<Integer>)o);
        } else if (o != null) {
            return Collections.singleton((Integer)o);
        }
        return Collections.EMPTY_SET;
    }

    public Map getMap() {
        return map;
    }

    public String getID() {
        return identifier;
    }

    public String getExternalID() {
        return externalID;
    }

    public String getContentType() {
        return contentType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getCarrierType() {
        return carrierType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setExtendedTitle(String extendedTitle) {
        this.extendedTitle = extendedTitle;
    }

    public String getExtendedTitle() {
        return extendedTitle;
    }

    public Collection<String> getTitleComponents() {
        return titleComponents;
    }

    public String getCorporateName() {
        return corporate;
    }

    public String getMeetingName() {
        return meeting;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public String getPublisherPlace() {
        return publisherPlace;
    }

    public String getLanguage() {
        return language;
    }

    public List<String> getCountry() {
        return country;
    }

    public Integer getFirstDate() {
        return firstDate;
    }

    public Integer getLastDate() {
        return lastDate;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getGenre() {
        return genre;
    }

    public boolean isSupplement() {
        return isSupplement;
    }

    public boolean isDatabase() {
        return isDatabase;
    }

    public boolean isNewspaper() {
        return isNewspaper;
    }

    public boolean isPacket() {
        return isPacket;
    }

    public boolean isWebsite() {
        return isWebsite;
    }

    public boolean isBibliography() {
        return isBibliography;
    }

    public boolean isAggregate() {
        return isAggregate;
    }

    public boolean isSubseries() {
        return isSubseries;
    }

    public TitleRecord setOpenAccess(boolean openAccess) {
        this.openAccess = openAccess;
        return this;
    }

    public boolean isOpenAccess() {
        return openAccess;
    }

    public TitleRecord setLicense(String license) {
        this.license = license;
        return this;
    }

    public String getLicense() {
        return license;
    }

    public boolean hasIdentifiers() {
        return identifiers != null && !identifiers.isEmpty();
    }

    public Map<String, Object> getIdentifiers() {
        return identifiers;
    }

    public Collection<String> getISSNs() {
        Collection<String> issns = (Collection<String>) getIdentifiers().get("issn");
        return issns == null ? Collections.EMPTY_LIST : issns;
    }

    public boolean hasLinks() {
        return links != null && !links.isEmpty();
    }

    public void setLinks(List<Map<String, Object>> links) {
        this.links = links;
    }

    public List<Map<String, Object>> getLinks() {
        return links;
    }

    public MultiMap<String, String> getRelations() {
        return relations;
    }

    public MultiMap<String, String> getRelationsExternalIDs() {
        return externalRelations;
    }

    public Set<Integer> getDates() {
        return dates;
    }

    public List<Map<String,Object>> getGreenInfo() {
        return greenInfo;
    }

    public void addVolume(MonographVolume volume) {
        monographVolumes.add(volume);
        // copy monograph volume holdings to this holdings
        MultiMap<String, Holding> mm = volume.getRelatedHoldings();
        for (String key : mm.keySet()) {
            for (Holding holding : mm.get(key)) {
                relatedHoldings.put(key, holding);
                for (Integer date : holding.dates()) {
                    holdingsByDate.put(date, holding);
                }
            }
        }
    }

    public Collection<MonographVolume> getMonographVolumes() {
        return monographVolumes;
    }

    private void findSupplement() {
        this.isSupplement = "isSupplementOf".equals(getString("SupplementParentEntry.relation"));
        if (!isSupplement) {
            this.isSupplement = "isSupplementOf".equals(getString("SupplementSpecialEditionEntry.relation"));
        }
    }

    protected void makeTitle() {
        StringBuilder sb = new StringBuilder();
        String titleMain = getString("TitleStatement.titleMain");
        sb.append(clean(titleMain));
        titleComponents = new LinkedHashSet<>();
        titleComponents.addAll(split(titleMain));
        String titleRemainder = getString("TitleStatement.titleRemainder");
        if (!Strings.isNullOrEmpty(titleRemainder)) {
            sb.append(" ; ").append(titleRemainder);
            titleComponents.addAll(split(titleRemainder));
        }
        Map<String, Object> m = (Map<String, Object>) map.get("TitleStatement");
        if (m != null) {
            String medium = getString("titleMedium");
            if (medium != null) {
                // delete synthetic medium titles
                if ("[Elektronische Ressource]".equals(medium)) {
                    m.remove("titleMedium");
                }
                medium = medium.replaceAll("\\[Elektronische Ressource\\]", "");
                if (!Strings.isNullOrEmpty(medium)) {
                    sb.append(" ; ").append(medium);
                    titleComponents.addAll(split(medium));
                }
            }
            // add part name / part number
            if (m.containsKey("titlePartName")) {
                String partName = getString("TitleStatement.titlePartName");
                if (!Strings.isNullOrEmpty(partName)) {
                    partName = partName.replaceAll("\\[Elektronische Ressource\\]", "");
                    if (!Strings.isNullOrEmpty(partName)) {
                        sb.append(" ; ").append(clean(partName));
                        titleComponents.addAll(split(partName));
                    }
                }
            }
            if (m.containsKey("titlePartNumber")) {
                String partNumber = getString("TitleStatement.titlePartNumber");
                if (!Strings.isNullOrEmpty(partNumber)) {
                    sb.append(" ; ").append(clean(partNumber));
                    titleComponents.addAll(split(partNumber));
                }
            }
        }
        setTitle(sb.toString());
        // extended title: combine with corporate name and/or meeting name
        // more titles for title components
        String varyingTitle = getString("VaryingTitle.titleMain");
        if (!Strings.isNullOrEmpty(varyingTitle)) {
            titleComponents.addAll(split(varyingTitle));
        }
        // former titles
        Object o = get("FormerTitle");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            List<Map<String,Object>> list = (List<Map<String, Object>>) o;
            for (Map<String,Object> map : list) {
                String s = (String)map.get("titleMain");
                if (!Strings.isNullOrEmpty(s)) {
                    titleComponents.addAll(split(s));
                }
            }
        }
        if (!Strings.isNullOrEmpty(corporate)) {
            sb.append(" / ").append(corporate);
            titleComponents.add(corporate);
        }
        if (!Strings.isNullOrEmpty(meeting)) {
            sb.append(" / ").append(meeting);
            titleComponents.add(meeting);
        }
        setExtendedTitle(sb.toString());
    }

    protected String clean(String s) {
        if (s == null) {
            return null;
        }
        int pos = s.indexOf("/ ");
        if (pos > 0) {
            s = s.substring(0, pos);
        }
        s = s.replaceAll("\\[.*?\\]", "").trim();
        return s;
    }

    protected List<String> split(String title) {
        List<String> list = new LinkedList<>();
        if (title != null) {
            title = title.replaceAll(" ; ", "\n").replaceAll(" / ", "\n").replaceAll(" = ", "\n");
            for (String s : title.split("\n")) {
                if (s != null) {
                    if (s.equals("[...]")) {
                        continue;
                    }
                    // transliteration?
                    if (s.startsWith("= ")) {
                        s = s.substring(2);
                    }
                    list.add(s.trim());
                }
            }
        }
        return list;
    }

    protected void makeCorporate() {
        this.corporate = getString("CorporateName.corporateName");
    }

    protected void makeMeeting() {
        this.meeting = getString("MeetingName.meetingName");
    }

    private String findPublisherName() {
        Object o = map.get("PublicationStatement");
        if (o == null) {
            return "";
        }
        if (!(o instanceof List)) {
            o = Collections.singletonList(o);
        }
        Set<String> set = new LinkedHashSet();
        for (Map<String, Object> m : (List<Map<String, Object>>) o) {
            o = m.get("name");
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            for (String s : (List<String>) o) {
                set.add(s);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String s : set) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            s = Strings.unquote(s);
            s = s.replaceAll("\\[.*\\]", ""); // [u.a.]
            sb.append(s.trim());
        }
        return sb.toString();
    }

    private String findPublisherPlace() {
        Object o = map.get("PublicationStatement");
        if (o == null) {
            return "";
        }
        if (!(o instanceof List)) {
            o = Collections.singletonList(o);
        }
        Set<String> set = new LinkedHashSet();
        for (Map<String, Object> m : (List<Map<String, Object>>) o) {
            o = m.get("place");
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            for (String s : (List<String>) o) {
                set.add(s);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String s : set) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            s = Strings.unquote(s);
            s = s.replaceAll("\\[.*\\]", ""); // [u.a.]
            sb.append(s.trim());
        }
        return sb.toString();
    }

    private void findLinks() {
        Object o = map.get("ElectronicLocationAndAccess");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            this.links = (List) o;
            // green statements?
            this.greenInfo = new LinkedList<>();
            for (Map<String, Object> link : links) {
                if ("kostenfrei".equals(link.get("publicnote"))) {
                    Map<String,Object> map = new HashMap<>();
                    map.putAll(link);
                    map.remove("publicnote");
                    greenInfo.add(map);
                }
            }
        } else {
            this.links = Collections.EMPTY_LIST;
        }
    }

    private void computeContentTypes() {
        Object o = map.get("physicalDescriptionElectronicResource");
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
        // before assuming text without any media, check title strings for media phrases
        String[] phraseTitles = new String[]{
                getString("AdditionalPhysicalFormNote.value"),
                getString("OtherCodes.genre"),
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
            }
        }
        // default
        this.contentType = "text";
        this.mediaType = "unmediated";
        this.carrierType = "volume";
    }

    private final static String[] ER = new String[] {
            "Elektronische Ressource"
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
            d1 = firstDate == null ? currentYear : firstDate;
            d2 = lastDate == null ? currentYear : lastDate;
            delta = d2 - d1;
        } catch (NumberFormatException e) {
            delta = 0;
        }
        String d2Str = Integer.toString(d2);
        String deltaStr = Integer.toString(delta);
        return d2Str.length() + d2Str + deltaStr.length() + deltaStr + sb.toString();
    }

    protected void findCountry() {
        Object o = getAnyObject("publishingCountry.isoCountryCodesSource");
        if (o instanceof List) {
            this.country = (List<String>) o;
        } else if (o instanceof String) {
            List<String> l = new LinkedList<>();
            l.add((String) o);
            this.country = l;
        } else {
            List<String> l = new LinkedList<>();
            l.add("unknown");
            this.country = l;
        }
    }

    protected void makeIdentifiers() {
        Map<String, Object> m = new HashMap();
        Set<String> issns = new LinkedHashSet();
        Set<String> formattedISSNs = new LinkedHashSet();
        // get and process all ISSN (with and without hyphen)
        Object o = map.get("IdentifierISSN");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            List<Map<String, Object>> l = (List<Map<String, Object>>) o;
            for (Map<String, Object> aL : l) {
                String s = (String) aL.get("value");
                if (s != null) {
                    formattedISSNs.add(s);
                    // lower case ISSN for better search
                    issns.add(s.replaceAll("\\-", "").toLowerCase());
                }
            }
        }
        if (!issns.isEmpty()) {
            m.put("issn", issns);
        }
        if (!formattedISSNs.isEmpty()) {
            m.put("formattedissn", formattedISSNs);
        }
        // get CODEN for better article matching
        o = map.get("IdentifierCODEN");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            List list = (List)o;
            if (!list.isEmpty()) {
                m.put("coden", o);
            }
        }
        // TODO more identifiers?
        this.identifiers = m;
    }

    /**
     * Iterate through relations.
     * Check for DNB IDs and remember as internal IDs.
     * Check for ZDB IDs and remember as external IDs.
     */
    private void makeRelations() {
        for (String rel : relationEntries) {
            Object o = map.get(rel);
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            for (Object obj : (List) o) {
                Map<String, Object> m = (Map<String, Object>) obj;
                Object relObj = m.get("relation");
                if (relObj == null) {
                    continue;
                }
                String key = relObj instanceof List ?
                        ((List) relObj).get(0).toString() : relObj.toString();
                // internal ID = DNB ID (used for identifying relationships between hits)
                // more than one DNB identifier is strange...
                Object internalObj = m.get("identifierDNB");
                // take only first entry from list
                String internal = internalObj == null ? null : internalObj instanceof List ?
                        ((List) internalObj).get(0).toString() : internalObj.toString();
                if (internal == null) {
                    continue;
                }
                relations.put(key, internal);
                // external ID = ZDB ID (used for external typed linking, internal linking may collide with GND ID)
                Object externalObj = m.get("identifierZDB");
                String external = externalObj == null ? null : externalObj instanceof List ?
                        ((List) externalObj).get(0).toString() : externalObj.toString();
                if (external == null) {
                    continue;
                }
                externalRelations.put(key, external);
            }
        }
    }

    public void addRelated(String relation, TitleRecord titleRecord) {
        relatedRecords.put(relation, titleRecord);
    }

    public void addRelatedHolding(String relation, Holding holding) {
        addRelatedHolding(relation, holding, true);
    }

    private void addRelatedHolding(String isil, Holding holding, boolean otherCarriers) {
        Collection<Holding> c = relatedHoldings.get(isil);
        if (c != null && c.contains(holding)) {
            return;
        }
        // add while we go, no coercion
        relatedHoldings.put(isil, holding);
        // unfold dates
        for (Integer date : holding.dates()) {
            holdingsByDate.put(date, holding);
        }
        holding.addParent(this.getExternalID());
        if (!otherCarriers) {
            return;
        }
        // tricky: add this holding also to title records of other carrier editions, but avoid recursion
        List<TitleRecord> list = new LinkedList();
        Collection<TitleRecord> trs = relatedRecords.get("hasPrintEdition");
        if (trs != null) {
            list.addAll(trs);
        }
        trs = relatedRecords.get("hasOnlineEdition");
        if (trs != null) {
            list.addAll(trs);
        }
        trs = relatedRecords.get("hasDigitizedEdition");
        if (trs != null) {
            list.addAll(trs);
        }
        for (TitleRecord tr : list) {
            // add holding, avoid recursion
            tr.addRelatedHolding(holding.getISIL(), holding, false);
        }
    }

    /**
     * Coserce indicators into holdings/licenses, if exist
     * @param relation relation
     * @param indicator indicator
     */
    public void addRelatedIndicator(String relation, Indicator indicator) {
        Collection<Holding> c = relatedHoldings.get(relation);
        if (c != null && c.contains(indicator)) {
            return;
        }
        // coerce indicator with licenses first
        Collection<Holding> oldHoldings = relatedHoldings.get(relation);
        Collection<Holding> newHoldings = indicator.coerceWithLicense(oldHoldings);
        if (newHoldings == null) {
            return;
        }
        if (oldHoldings != null && oldHoldings.size() == newHoldings.size()) {
            return;
        }
        relatedHoldings.putAll(relation, newHoldings);
        // unfold dates
        for (Integer date : indicator.dates()) {
            holdingsByDate.put(date, indicator);
        }
        indicator.addParent(this.getExternalID());
        // tricky: add this holding also to title records of other carrier editions!
        List<TitleRecord> list = new LinkedList();
        Collection<TitleRecord> trs = relatedRecords.get("hasPrintEdition");
        if (trs != null) {
            list.addAll(trs);
        }
        trs = relatedRecords.get("hasOnlineEdition");
        if (trs != null) {
            list.addAll(trs);
        }
        trs = relatedRecords.get("hasDigitizedEdition");
        if (trs != null) {
            list.addAll(trs);
        }
        // add holding, avoid recursion
        for (TitleRecord tr : list) {
            tr.addRelatedHolding(indicator.getISIL(), indicator, false);
        }
    }

    public MultiMap<String, TitleRecord> getRelated() {
        return relatedRecords;
    }

    public MultiMap<String, Holding> getRelatedHoldings() {
        return relatedHoldings;
    }

    public MultiMap<Integer, Holding> getHoldingsByDate() {
        return holdingsByDate;
    }

    private final static Set<String> temporalRelations = new HashSet<>(Arrays.asList(
            "succeededBy",
            "precededBy",
            "hasTransientEdition",
            "isTransientEditionOf"
    ));

    public static Set<String> getTemporalRelations() {
        return temporalRelations;
    }

    private final static Set<String> carrierRelations = new HashSet<>(Arrays.asList(
            "hasPrintEdition",
            "hasOnlineEdition",
            "hasBrailleEdition",
            "hasCDEdition",
            "hasDVDEdition",
            "hasMicroformEdition",
            "hasDigitizedEdition",
            "hasUSBEdition",
            "hasEdition"
    ));

    public static Set<String> getCarrierRelations() {
        return carrierRelations;
    }

    private final static Set<String> supplementalRelations = new HashSet<>(Arrays.asList(
            "hasSupplement",
            "isSupplementOf"
    ));

    public static Set<String> getSupplementalRelations() {
        return supplementalRelations;
    }

    private final static Set<String> relationEntries = new HashSet<>(Arrays.asList(
            "PrecedingEntry",
            "SucceedingEntry",
            "OtherEditionEntry",
            "OtherRelationshipEntry",
            "AdditionalPhysicalFormEntry",
            "SupplementSpecialIssueEntry",
            "SupplementParentEntry"
    ));

    public static Set<String> getRelationEntries() {
        return relationEntries;
    }

    public static Map<String, String> getInverseRelations() {
        return inverseRelations;
    }

    private final static Map<String, String> inverseRelations = new HashMap<String, String>() {{

        put("hasPart", "isPartOf");
        put("hasSupplement", "isSupplementOf");
        put("isPartOf", "hasPart");
        put("isSupplementOf", "hasSupplement");

        put("precededBy", "succeededBy");
        put("succeededBy", "precededBy");

        put("hasLanguageEdition", "isLanguageEditionOf");
        put("hasTranslation", "isTranslationOf");
        put("isLanguageEditionOf", "hasLanguageEdition");
        put("isTranslationOf", "hasTranslation");

        put("hasOriginalEdition", "isOriginalEditionOf");
        put("hasPrintEdition", "isPrintEditionOf");
        put("hasOnlineEdition", "isOnlineEditionOf");
        put("hasBrailleEdition", "isBrailleEditionOf");
        put("hasDVDEdition", "isDVDEditionOf");
        put("hasCDEdition", "isCDEditionOf");
        put("hasDiskEdition", "isDiskEditionOf");
        put("hasMicroformEdition", "isMicroformEditionOf");
        put("hasDigitizedEdition", "isDigitizedEditionOf");

        put("hasSpatialEdition", "isSpatialEditionOf");
        put("hasTemporalEdition", "isTemporalEditionOf");
        put("hasPartialEdition", "isPartialEditionOf");
        put("hasTransientEdition", "isTransientEditionOf");
        put("hasLocalEdition", "isLocalEditionOf");
        put("hasAdditionalEdition", "isAdditionalEditionOf");
        put("hasAlternativeEdition", "isAdditionalEditionOf");
        put("hasDerivedEdition", "isDerivedEditionOf");
        put("hasHardcoverEdition", "isHardcoverEditionOf");
        put("hasManuscriptEdition", "isManuscriptEditionOf");
        put("hasBoxedEdition", "isBoxedEditionOf");
        put("hasReproduction", "isReproductionOf");
        put("hasSummary", "isSummaryOf");

        put("isOriginalEditionOf", "hasOriginalEdition");
        put("isPrintEditionOf", "hasPrintEdition");
        put("isOnlineEditionOf", "hasOnlineEdition");
        put("isBrailleEditionOf", "hasBrailleEdition");
        put("isDVDEditionOf", "hasDVDEdition");
        put("isCDEditionOf", "hasCDEdition");
        put("isDiskEditionOf", "hasDiskEdition");
        put("isMicroformEditionOf", "hasMicroformEdition");
        put("isDigitizedEditionOf", "hasMicroformEdition");
        put("isSpatialEditionOf", "hasSpatialEdition");
        put("isTemporalEditionOf", "hasTemporalEdition");
        put("isPartialEditionOf", "hasPartialEdition");
        put("isTransientEditionOf", "hasTransientEdition");
        put("isLocalEditionOf", "hasLocalEdition");
        put("isAdditionalEditionOf", "hasAdditionalEdition");
        put("isDerivedEditionOf", "hasDerivedEdition");
        put("isHardcoverEditionOf", "hasHardcoverEdition");
        put("isManuscriptEditionOf", "hasManuscriptEdition");
        put("isBoxedEditionOf", "hasBoxedEdition");
        put("isReproductionOf", "hasReproduction");
        put("isSummaryOf", "hasSummary");
    }};


    public String toString() {
        return externalID;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TitleRecord && externalID.equals(((TitleRecord)other).externalID);
    }

    @Override
    public int hashCode() {
        return externalID.hashCode();
    }

    @Override
    public int compareTo(TitleRecord m) {
        return externalID.compareTo(m.getExternalID());
    }

}

