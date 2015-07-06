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
package org.xbib.tools.merge.zdb.entities;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.entities.support.EnumerationAndChronologyHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;

public class Cluster extends TreeSet<Manifestation> {

    private final static Logger logger = LogManager.getLogger(TimeLine.class.getName());

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private Integer firstDate;

    private Integer lastDate;

    private Map<Integer, Set<Holding>> holdingsByDate;

    private Map<Integer, Set<License>> licensesByDate;

    public Cluster(Collection<Manifestation> c) {
        super(c);
        findFirstAndLastDate();
    }

    public Map<Integer, Set<Holding>> getHoldingsByDate() {
        return holdingsByDate;
    }

    public Map<Integer, Set<License>> getLicensesByDate() {
        return licensesByDate;
    }

    public void setHoldings(Set<Holding> holdings) {
        holdingsByDate = newTreeMap();
        for (Holding holding : holdings) {
            if (holding.isDeleted()) {
                continue;
            }
            List<Integer> dates = null;
            // check for our generated dates
            Object o = holding.map().get("dates");
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
                }
                dates = newLinkedList();
                dates.addAll((List<Integer>) o);
            } else {
                // let's parse dates
                o = holding.map().get("FormattedEnumerationAndChronology");
                if (o != null) {
                    if (!(o instanceof List)) {
                        o = Arrays.asList(o);
                    }
                    dates = parseDates((List<Map<String, Object>>) o);
                } else {
                    o = holding.map().get("NormalizedHolding");
                    if (o != null) {
                        if (!(o instanceof List)) {
                            o = Arrays.asList(o);
                        }
                        dates = parseDates((List<Map<String, Object>>) o);
                    }
                }
            }
            if (dates == null || dates.isEmpty()) {
                // no dates, or unparseable dates. Save as "default holdings" at -1
                Set<Holding> set = holdingsByDate.get(-1);
                if (set == null) {
                    set = newHashSet();
                }
                set.add(holding);
                holdingsByDate.put(-1, set);
            } else {
                Collection<Integer> invalid = newLinkedList();
                for (Integer date : dates) {
                    if (((firstDate != null) && (date < firstDate)) ||
                            (lastDate != null && (date > lastDate))) {
                        invalid.add(date);
                        continue;
                    }
                    // there might be more holdings than one per date
                    Set<Holding> set = holdingsByDate.get(date);
                    if (set == null) {
                        set = newHashSet();
                    }
                    set.add(holding);
                    holdingsByDate.put(date, set);
                }
                if (!invalid.isEmpty()) {
                    logger.debug("dates {} in holdings for {} out of range {}-{}",
                            invalid, holding.parents(), firstDate, lastDate);
                }
            }
        }
    }

    /**
     * Reorder licenses by date, sort out those out of range
     * @param licenses a set of licenses
     */
    public void validateByDateRange(Set<License> licenses) {
        licensesByDate = newTreeMap();
        if (licenses == null) {
            return;
        }
        for (License license : licenses) {
            if (license.isDeleted()) {
                continue;
            }
            Collection<Integer> invalid = newHashSet();
            for (Integer date : license.dates()) {
                if (((firstDate != null) && (date < firstDate)) ||
                        (lastDate != null && (date > lastDate))) {
                    invalid.add(date);
                    continue;
                }
                Set<License> l = licensesByDate.get(date);
                if (l == null) {
                    l = newHashSet();
                }
                l.add(license);
                licensesByDate.put(date, l);
            }
            if (!invalid.isEmpty()) {
                logger.debug("dates {} in license for {} out of range {}-{}",
                        invalid, license.parents(), firstDate, lastDate);
            }
        }
    }

    /**
     * Iterate through all dates and make the available services.
     * Concatenate services of same institution.
     */
    public void attachServicesToManifestations() {
        Set<Integer> dates = newTreeSet();
        if (holdingsByDate != null) {
            dates.addAll(holdingsByDate.keySet());
        }
        if (licensesByDate != null) {
            dates.addAll(licensesByDate.keySet());
        }
        for (Integer date : dates) {
            Map<String, List<Holding>> services = newHashMap();
            Set<Holding> holdings = holdingsByDate.get(date);
            if (holdings != null) {
                for (Holding holding : holdings) {
                    String isil = holding.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    List<Holding> list = services.get(isil);
                    if (list == null) {
                        list = newLinkedList();
                        services.put(isil, list);
                    }
                    list.add(holding);
                    for (Manifestation parent : holding.getManifestations()) {
                        if (parent.isDateValid(date)) {
                            parent.addRelatedVolume(date, holding);
                            Set<Manifestation> online;
                            synchronized (parent.getRelatedManifestations()) {
                                // copy print holding over to online manifestation if available
                                online = ImmutableSet.copyOf(parent.getRelatedManifestations().get("hasOnlineEdition"));
                            }
                            if (online != null) {
                                // almost sure we have only one online manifestation...
                                for (Manifestation m : online) {
                                    if (m.isDateValid(date)) {
                                        m.addRelatedVolume(date, holding);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Set<License> licenses = licensesByDate.get(date);
            if (licenses != null) {
                for (License license : licenses) {
                    String isil = license.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    List<Holding> list = services.get(isil);
                    if (list == null) {
                        list = newLinkedList();
                        services.put(isil, list);
                    }
                    list.add(license);
                    for (Manifestation parent : license.getManifestations()) {
                        if (parent.isDateValid(date)) {
                            parent.addRelatedVolume(date, license);
                            Set<Manifestation> print;
                            synchronized (parent.getRelatedManifestations()) {
                                // copy online license over to print manifestation if available
                                print = ImmutableSet.copyOf(parent.getRelatedManifestations().get("hasPrintEdition"));
                            }
                            if (print != null) {
                                for (Manifestation m : print) {
                                    if (m.isDateValid(date)) {
                                        m.addRelatedVolume(date, license);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private List<Integer> parseDates(List<Map<String, Object>> groups) {
        EnumerationAndChronologyHelper eac = new EnumerationAndChronologyHelper();
        List<Integer> begin = newLinkedList();
        List<Integer> end = newLinkedList();
        List<String> beginvolume = newLinkedList();
        List<String> endvolume = newLinkedList();
        List<Boolean> open = newLinkedList();
        for (Map<String, Object> m : groups) {
            Object o = m.get("movingwall");
            if (o != null) {
                logger.debug("movingwall detected: {}", o);
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
        List<Integer> dates = newLinkedList();
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

    private void findFirstAndLastDate() {
        this.firstDate = Integer.MAX_VALUE;
        this.lastDate = Integer.MIN_VALUE;
        for (Manifestation m : this) {
            if (m.firstDate() == null) {
                continue;
            }
            if (m.firstDate() < this.firstDate) {
                this.firstDate = m.firstDate();
            }
            int d = m.lastDate() == null ? currentYear : m.lastDate();
            if (d > this.lastDate) {
                this.lastDate = d;
            }
            // we have to check all related manifestations for first/last dates
            for (Manifestation p : m.getRelatedManifestations().values()) {
                if (p.firstDate() != null && p.firstDate() < firstDate) {
                    this.firstDate = p.firstDate();
                }
                d = p.lastDate() == null ? currentYear : p.lastDate();
                if (d > this.lastDate) {
                    this.lastDate = d;
                }
            }
        }
        if (firstDate == Integer.MAX_VALUE) {
            // "Produkt-ISIL" z.B. 21543057
            firstDate = null;
            lastDate = null;
        } else if (lastDate == Integer.MIN_VALUE) {
            lastDate = currentYear;
        }
    }
}
