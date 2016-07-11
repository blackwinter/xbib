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

import org.xbib.common.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indicator extends License {

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private final static Pattern movingWallMonthPattern = Pattern.compile("^[+-](\\d+)M$");

    private final static Pattern movingWallYearPattern = Pattern.compile("^[+-](\\d+)Y$");

    public Indicator(Map<String, Object> m) {
        super(m);
        // do not call complete(), it's done with super(m)
    }

    @Override
    protected void build() {
        this.identifier = getString("dc:identifier");
        String parent = getString("xbib:identifier");
        addParent(parent);
        this.isil = getString("xbib:isil");
        setISIL(isil);
        buildDateArray();
        this.info = buildInfo();
        this.findContentType();
        setPriority(this.findPriority());
    }

    @Override
    protected void findContentType() {
        this.mediaType = "computer";
        this.carrierType = "online resource";
    }

    @Override
    protected void buildDateArray() {
        List<Integer> dates = new LinkedList<>();
        Integer first;
        Integer last;
        this.delta = null;
        String movingWall = getString("xbib:movingWall");
        if (movingWall != null) {
            Matcher m = movingWallYearPattern.matcher(movingWall);
            if (m.find()) {
                this.delta = Integer.parseInt(m.group(1));
            } else {
                m = movingWallMonthPattern.matcher(movingWall);
                if (m.find()) {
                    this.delta = Integer.parseInt(m.group(1)) / 12;
                }
            }
        }
        String firstDateStr = getString("xbib:firstDate");
        String lastDateStr = getString("xbib:lastDate");
        last = Strings.isNullOrEmpty(lastDateStr) ? currentYear : Integer.parseInt(lastDateStr);
        if (!Strings.isNullOrEmpty(firstDateStr)) {
            first = Integer.parseInt(firstDateStr);
            for (int d = first; d <= last; d++) {
                dates.add(d);
            }
        }
        if (movingWall != null && delta != null) {
            first = last - delta + 1;
            if (movingWall.startsWith("+")) {
                for (Integer d = first; d <= last; d++) {
                    dates.add(d);
                }
            } else if (movingWall.startsWith("-")) {
                for (Integer d = first; d <= last; d++) {
                    dates.remove(d);
                }
            }
        }
        if (!dates.isEmpty()) {
            this.firstdate = dates.get(0);
            this.lastdate = dates.get(dates.size() - 1);
        }
        // ordered unique dates
        this.dates = new TreeSet<>(dates);
    }

    @Override
    protected Map<String, Object> buildInfo() {
        Map<String, Object> m = new LinkedHashMap<>();
        String s = getString("xbib:interlibraryloanCode");
        if (s != null) {
            switch (s) {
                case "kxn": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution("domestic");
                    break;
                }
                case "kxx": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution("unrestricted");
                    break;
                }
                case "kpn": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution(Arrays.asList("postal", "domestic"));
                    break;
                }
                case "kpx": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution("postal");
                    break;
                }
                case "exn": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution(Arrays.asList("electronic", "domestic"));
                    break;
                }
                case "exx": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution("electronic");
                    break;
                }
                default: {
                    setServiceType("interlibrary");
                    setServiceMode("none");
                    setServiceDistribution("none");
                    break;
                }
            }
            String comment = getString("xbib:comment");
            if (!Strings.isNullOrEmpty(comment)) {
                setServiceComment(comment);
            }
        }
        Map<String, Object> group = new HashMap<>();
        group.put("begindate", getString("xbib:firstDate"));
        String lastDate = getString("xbib:lastDate");
        group.put("enddate", Strings.isNullOrEmpty(lastDate) ? currentYear : lastDate);
        // optional, can be null
        String firstVolume = getString("xbib:firstVolume");
        if (!Strings.isNullOrEmpty(firstVolume)) {
            group.put("beginvolume", firstVolume);
        }
        String lastVolume = getString("xbib:lastVolume");
        if (!Strings.isNullOrEmpty(lastVolume)) {
            group.put("endvolume", lastVolume);
        }
        String firstIssue = getString("xbib:firstIssue");
        if (!Strings.isNullOrEmpty(firstIssue)) {
            group.put("beginissue", firstIssue);
        }
        String lastIssue = getString("xbib:lastIssue");
        if (!Strings.isNullOrEmpty(lastIssue)) {
            group.put("endissue", lastIssue);
        }
        // moving wall
        if (delta != null) {
            group.put("delta", delta);
        }
        Map<String, Object> holdings = new HashMap<>();
        holdings.put("group", group);
        m.put("holdings", holdings);
        return m;
    }

    /**
     * Iterate through given holdings and build a new list that contains
     * unified holdings, coerce with licenses
     *
     * @param holdings the holdings to iterte
     * @return unique holdings
     */
    public Collection<Holding> coerceWithLicense(Collection<Holding> holdings) {
        if (holdings == null) {
            return null;
        }
        // check if there are other licenses that match
        boolean found = isIn(holdings);
        if (!found) {
            Collection<Holding> newHoldings = new ArrayList<>(holdings);
            newHoldings.add(this);
            return newHoldings;
        }
        return holdings;
    }

    /**
     * Similarity of license/indicator: they must have same media type, same
     * carrier type, and same date period (if any).
     *
     * @param holdings the holdings to check for similarity against this holding
     * @return collection of holdings which are similar, or an empty collection if no holding is similar
     */
    private boolean isIn(Collection<Holding> holdings) {
        if (holdings == null) {
            return false;
        }
        boolean found = false;
        for (Holding holding : holdings) {
            // same ISIL, media, carrier, from/to?
            if (isil.equals(holding.isil)
                    && getISIL().equals(holding.getISIL())
                    && mediaType.equals(holding.mediaType)
                    && carrierType.equals(holding.carrierType)) {
                // check if start date / end date are the same
                // both no dates?
                if (dates == null && holding.dates == null) {
                    // hit, no dates at all
                    found = true;
                } else if (firstdate != null && holding.firstdate != null) {
                    if (lastdate != null && holding.lastdate != null) {
                        // both first and last date are present
                        Integer d1 = firstdate;
                        Integer d2 = lastdate;
                        Integer e1 = holding.firstdate;
                        Integer e2 = holding.lastdate;
                        if (d1.equals(e1) && d2.equals(e2)) {
                            found = true;
                        }
                    } else {
                        // no last date
                        Integer d1 = firstdate;
                        Integer e1 = holding.firstdate;
                        if (d1.equals(e1)) {
                            found = true;
                        }
                    }
                } else if (lastdate != null && holding.lastdate != null) {
                    // no first date
                    Integer d1 = lastdate;
                    Integer e1 = holding.lastdate;
                    if (d1.equals(e1)) {
                        found = true;
                    }
                }
            }
            if (found) {
                // move new indicators to existing license, effectivly
                // superseding existing indicatos
                holding.setPriority(getPriority());
                holding.setServiceType(getServiceType());
                holding.setServiceMode(getServiceMode());
                holding.setServiceDistribution(getServiceDistribution());
                holding.setServiceComment(getServiceComment());
            }
        }
        return found;
    }

}
