/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2015 Jörg Prante and xbib
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.support.EnumerationAndChronologyHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Holding implements Comparable<Holding> {

    private final static Logger logger = LogManager.getLogger(Holding.class);

    protected final static Integer currentYear = LocalDate.now().getYear();

    protected final Map<String, Object> map;

    protected String identifier;

    protected String parent;

    protected Set<String> parents;

    protected String isil;

    protected Map<String, Object> info;

    protected Map<String, Object> license;

    protected String mediaType;

    protected String carrierType;

    protected Set<Integer> dates;

    protected Integer firstdate;

    protected Integer lastdate;

    // delta from a moving wall
    protected Integer delta;

    protected boolean deleted;

    protected String gap;

    private Object servicetype;
    private Object servicemode;
    private Object servicedistribution;
    private Object servicecomment;
    private Integer priority;
    private String serviceisil;
    private String name;
    private String region;
    private String organization;

    public Holding(Map<String, Object> map) {
        this.map = map;
        this.parents = new HashSet<>();
        build();
    }

    public Map<String, Object> map() {
        return map;
    }

    protected String getString(String key) {
        return get(key);
    }

    <T> T get(String key) {
        return this.<T>get(map, key.split("\\."));
    }

    @SuppressWarnings("unchecked")
    private <T> T get(Map inner, String[] key) {
        if (inner == null) {
            return null;
        }
        Object o = inner.get(key[0]);
        if (o instanceof List) {
            o = ((List) o).get(0); // only first entry
        }
        return (T) (o instanceof Map && key.length > 1 ?
                get((Map) o, Arrays.copyOfRange(key, 1, key.length)) : o);
    }

    @SuppressWarnings("unchecked")
    protected void build() {
        this.identifier = getString("RecordIdentifier.identifierForTheRecord");
        if (this.identifier == null) {
            throw new IllegalArgumentException("parent identifier must not be null");
        }
        this.identifier = this.identifier.replace('X', 'x');
        this.parent = getString("ParentRecordIdentifier.identifierForTheParentRecord");
        if (this.parent == null) {
            throw new IllegalArgumentException("parent identifier must not be null");
        }
        this.parent = this.parent.replace('X','x'); // DNB-ID, turn 'X' to lower case
        Object leader = map.get("leader");
        if (!(leader instanceof List)) {
            leader = Collections.singletonList(leader);
        }
        for (String s : (List<String>) leader) {
            if ("Deleted".equals(s)) {
                this.deleted = true;
                break;
            }
        }
        Object o = map.get("Location");
        if (!(o instanceof List)) {
            o = Collections.singletonList(o);
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) o;
        for (Map<String, Object> map : list) {
            if (map == null) {
                continue;
            }
            if (map.containsKey("location")) {
                setServiceISIL((String) map.get("location"));
                String s = getServiceISIL();
                if (s != null) {
                    if (s.startsWith("DE-")) {
                        // find "base" ISIL in DE namespace: cut from last '-' if there is more than one '-'
                        int firstpos = s.indexOf('-');
                        int lastpos = s.lastIndexOf('-');
                        setISIL(lastpos > firstpos ? s.substring(0, lastpos) : s);
                    } else {
                        setISIL(s);
                    }
                }
            }
        }
        if (isil == null) {
            // e.g. DNB-ID 036674168 WEU GB-LON63
            String isil = getString("service.organization");
            setISIL(isil);
            setServiceISIL(isil); // Sigel
        }
        // isil may be null, broken holding record, e.g. DNB-ID 114091315 ZDB-ID 2476016x
        if (isil != null) {
            findContentType();
            this.info = buildInfo();
            buildService();
        }
        // serviceisil may be null
        if (getServiceISIL() == null) {
            setServiceISIL(isil);
        }
        buildDateArray();
        setPriority(findPriority());
        this.gap = getString("TextualHoldings.gap");
    }

    public String identifier() {
        return identifier;
    }

    public String parentIdentifier() {
        return parent;
    }

    public void addParent(String parent) {
        if (parent != null) {
            this.parents.add(parent);
        }
    }

    public Set<String> parents() {
        return parents;
    }

    public void setISIL(String isil) {
        this.isil = isil;
    }

    public String getISIL() {
        return isil;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setServiceISIL(String isil) {
        this.serviceisil = isil;
    }

    public String getServiceISIL() {
        return serviceisil != null ? serviceisil : isil;
    }

    public Holding setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public Holding setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public Holding setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public void setServiceType(Object servicetype) {
        this.servicetype = servicetype;
    }

    public Object getServiceType() {
        return servicetype;
    }

    public void setServiceMode(Object servicemode) {
        this.servicemode = servicemode;
    }

    public Object getServiceMode() {
        return servicemode;
    }

    public void setServiceDistribution(Object servicedistribution) {
        this.servicedistribution = servicedistribution;
    }

    public Object getServiceDistribution() {
        return servicedistribution;
    }

    public void setServiceComment(Object servicecomment) {
        this.servicecomment = servicecomment;
    }

    public Object getServiceComment() {
        return servicecomment;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public Set<Integer> dates() {
        return dates;
    }

    public Integer getFirstDate() {
        return firstdate;
    }

    public Integer getLastDate() {
        return lastdate;
    }

    public String getGap() {
        return gap;
    }

    public String mediaType() {
        return mediaType;
    }

    public String carrierType() {
        return carrierType;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }

    @SuppressWarnings("unchecked")
    protected void buildDateArray() {
        List<Integer> dates = new ArrayList<>();
        // our pre-parsed dates
        Object o = map().get("dates");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            dates.addAll((List<Integer>) o);
        } else {
            // let's parse dates
            o = map().get("FormattedEnumerationAndChronology");
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Collections.singletonList(o);
                }
                dates = parseDates((List<Map<String, Object>>) o);
            } else {
                o = map().get("NormalizedHolding");
                if (o != null) {
                    if (!(o instanceof List)) {
                        o = Collections.singletonList(o);
                    }
                    dates = parseDates((List<Map<String, Object>>) o);
                }
            }
        }
        if (!dates.isEmpty()) {
            this.firstdate = dates.get(0);
            this.lastdate = dates.get(dates.size() - 1);
        }
        this.dates = new TreeSet<>(dates);
    }

    @SuppressWarnings("unchecked")
    private List<Integer> parseDates(List<Map<String, Object>> groups) {
        EnumerationAndChronologyHelper eac = new EnumerationAndChronologyHelper();
        List<Integer> begin = new LinkedList<>();
        List<Integer> end = new LinkedList<>();
        List<String> beginvolume = new LinkedList<>();
        List<String> endvolume = new LinkedList<>();
        List<Boolean> open = new LinkedList<>();
        for (Map<String, Object> m : groups) {
            Object o = m.get("movingwall");
            if (o != null) {
                logger.debug("movingwall detected: '{}' what now?", o);
            }
            o = m.get("date");
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            for (String content : (List<String>) o) {
                if (content == null) {
                    continue;
                }
                eac.parse(content, begin, end, beginvolume, endvolume, open);
            }
        }
        List<Integer> dates = new ArrayList<>();
        for (int i = 0; i < begin.size(); i++) {
            if (open.get(i)) {
                end.set(i, currentYear);
            }
            if (begin.get(i) != null && end.get(i) != null) {
                for (int d = begin.get(i); d <= end.get(i); d++) {
                    dates.add(d);
                }
            } else if (begin.get(i) != null && begin.get(i) > 0) {
                dates.add(begin.get(i));
            }
        }
        return dates;
    }

    @SuppressWarnings("unchecked")
    protected void findContentType() {
        this.mediaType = "unmediated";
        this.carrierType = "volume";
        if ("EZB".equals(getString("license.origin")) || map.containsKey("ElectronicLocationAndAccess")) {
            this.mediaType = "computer";
            this.carrierType = "online resource"; // triggers license/indicator search
            return;
        }
        Object o = map.get("textualholdings");
        if (!(o instanceof List)) {
            o = Collections.singletonList(o);
        }
        for (String s : (List<String>) o) {
            if (s == null) {
                continue;
            }
            if (s.contains("Microfiche")) {
                this.mediaType = "microform";
                this.carrierType = "other";
                return;
            }
            if (s.contains("CD-ROM") || s.contains("CD-Rom")) {
                this.mediaType = "computer optical disc";
                this.carrierType = "computer disc";
                return;
            }
        }
        String s = getString("SourceOfAcquisition.accessionNumber");
        if (s != null && s.contains("Microfiche")) {
            this.mediaType = "microform";
            this.carrierType = "other";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
        for (String locKey : new String[]{"Location","AdditionalLocation"}) {
            Object o = map.get(locKey);
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Collections.singletonList(o);
                }
                for (Map<String,Object> oldlocation : (List<Map<String,Object>>)o) {
                    Map<String,Object> location = new HashMap<>();
                    // Beschreibung
                    if (oldlocation.containsKey("collection")) {
                        location.put("collection", oldlocation.get("collection"));
                    }
                    // Signatur
                    if (oldlocation.containsKey("shelvinglocation")) {
                        location.put("callnumber", oldlocation.get("shelvinglocation"));
                    }
                    // Notiz, e.g. Digitalisierungsmaster
                    if (oldlocation.containsKey("publicnote")) {
                        location.put("publicnote", oldlocation.get("publicnote"));
                    }
                    // Bestandsverlauf
                    if (oldlocation.containsKey("enumerationAndChronology")) {
                        location.put("enumerationAndChronology", oldlocation.get("enumerationAndChronology"));
                    }
                    if (!location.isEmpty()) {
                        l.add(location);
                    }
                }
            }
        }
        info.put("location", l);
        Object textualholdings = map.get("textualholdings");
        info.put("textualholdings", textualholdings);
        info.put("holdings", map.get("holdings"));
        Object o = map.get("ElectronicLocationAndAccess");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            info.put("links", o);
        }
        o = map.get("license");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            for (Map<String, Object> m : (List<Map<String, Object>>) o) {
                if (m != null) {
                    m.remove("originSource");
                    m.remove("typeSource");
                    m.remove("scopeSource");
                    m.remove("chargeSource");
                    m.remove("accessSource");
                }
            }
        }
        return info;
    }

    @SuppressWarnings("unchecked")
    private void buildService() {
        Map<String, Object> service = (Map<String, Object>) map.get("service");
        if (service != null) {
            service.remove("organization"); // drop Sigel
            service.remove("region"); // drop region marker here, we use bibdat
            service.remove("bik"); // not required
            service.remove("servicetypeSource");  // not required
            service.remove("servicemodeSource"); // not required
            this.servicetype = service.remove("servicetype");
            map.put("type", this.servicetype);
            Object o = service.remove("servicemode");
            // split to array if copy-loan
            this.servicemode = "copy-loan".equals(o) ? Arrays.asList("copy", "loan") : o;
            map.put("mode", servicemode);
            // in ZDB holdings there is no distribution information, but we keep it here
            this.servicedistribution = service.remove("servicedistribution");
            map.put("distribution", this.servicedistribution);
        }
    }

    protected Integer findPriority() {
        Object o = getServiceType();
        if (o == null) {
            return 9;
        }
        o = getServiceMode();
        if (o == null) {
            return 9;
        }
        if (carrierType == null) {
            return 9;
        }
        switch (carrierType) {
            case "online resource": {
                o = getServiceDistribution();
                if (o != null) {
                    if (o instanceof List) {
                        List l = (List)o;
                        if (l.contains("postal")) {
                            return 3;
                        }
                    } else if (o.toString().equals("postal")) {
                        return 3;
                    }
                }
                return 1;
            }
            case "volume":
                return 2;
            case "computer disc":
                return 4;
            case "computer tape cassette":
                return 4;
            case "computer chip cartridge":
                return 4;
            case "microform":
                return 5;
            case "other":
                return 6;
            default:
                throw new IllegalArgumentException("unknown carrier: " + carrierType());
        }
    }

    public String toString() {
        return map.toString();
    }

    @Override
    public int compareTo(Holding o) {
        return identifier().compareTo(o.identifier());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Holding && identifier().equals(((Holding)o).identifier());
    }

    @Override
    public int hashCode() {
        return identifier().hashCode();
    }
}
