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
package org.xbib.tools.merge.serials.entities;

import org.xbib.util.Strings;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newTreeSet;

public class Indicator extends License {

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private final static Pattern movingWallYearPattern = Pattern.compile("\\-(\\d+)Y");

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
        this.serviceisil = isil;
        buildDateArray();
        this.info = buildInfo();
        this.findContentType();
        this.priority = this.findPriority();
    }

    @Override
    protected void findContentType() {
        this.mediaType = "computer";
        this.carrierType = "online resource";
    }

    @Override
    protected void buildDateArray() {
        List<Integer> dates = newLinkedList();
        String firstDate = getString("xbib:firstDate");
        int first;
        int last;
        if (!Strings.isNullOrEmpty(firstDate)) {
            first = Integer.parseInt(firstDate);
            String lastDate = getString("xbib:lastDate");
            last = Strings.isNullOrEmpty(lastDate) ? currentYear : Integer.parseInt(lastDate);
            String movingWall = getString("xbib:movingWall");
            if (movingWall != null) {
                Matcher m = movingWallYearPattern.matcher(movingWall);
                if (m.find()) {
                    Integer delta = Integer.parseInt(m.group(1));
                    last = last - delta;
                }
            }
            if (first > 0 && last > 0) {
                for (int d = first; d <= last; d++) {
                    dates.add(d);
                }
            }
        }
        if (!dates.isEmpty()) {
            this.firstdate = dates.get(0);
            this.lastdate = dates.get(dates.size() - 1);
        }
        this.dates = newTreeSet(dates);
    }

    @Override
    protected Map<String, Object> buildInfo() {
        Map<String, Object> m = newLinkedHashMap();
        String s = getString("xbib:interlibraryloanCode");
        if (s != null) {
            switch (s) {
                case "kxn": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "domestic";
                    break;
                }
                case "kxx": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "unrestricted";
                    break;
                }
                case "kpn": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = Arrays.asList("postal", "domestic");
                    break;
                }
                case "kpx": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "postal";
                    break;
                }
                case "exn": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = Arrays.asList("electronic", "domestic");
                    break;
                }
                case "exx": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "electronic";
                    break;
                }
                default: {
                    servicetype = "interlibrary";
                    servicemode = "none";
                    servicedistribution = "none";
                    break;
                }
            }
            String comment = getString("xbib:comment");
            if (!Strings.isNullOrEmpty(comment)) {
                servicecomment = comment;
            }
        }
        Map<String, Object> group = newHashMap();
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
        Map<String, Object> holdings = newHashMap();
        holdings.put("group", group);
        m.put("holdings", holdings);
        return m;
    }

}
