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

import org.xbib.util.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newTreeSet;

public class License extends Holding {

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    public License(Map<String, Object> m) {
        super(m);
        build();
    }

    @Override
    protected void build() {
        this.identifier = getString("ezb:license_entry_id");
        String parent = getString("ezb:zdbid");
        addParent(parent);
        this.isil = getString("ezb:isil");
        this.serviceisil = isil;
        this.deleted = "delete".equals(getString("ezb:action"));
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
        String firstDate = getString("ezb:license_period.ezb:first_date");
        int first;
        int last;
        if (firstDate != null) {
            first = Integer.parseInt(firstDate);
            String lastDate = getString("ezb:license_period.ezb:last_date");
            last = lastDate == null ? currentYear : Integer.parseInt(lastDate);
            if (first > 0 && last > 0) {
                for (int d = first; d <= last; d++) {
                    dates.add(d);
                }
            }
        }
        String movingWall = getString("ezb:license_period.ezb:moving_wall");
        if (movingWall != null) {
            Matcher m = movingWallPattern.matcher(movingWall);
            if (m.matches()) {
                int delta = Integer.parseInt(m.group(1));
                last = currentYear;
                first = last - delta;
                if ("+".startsWith(movingWall)) {
                    for (int d = first; d <= last; d++) {
                        dates.add(d);
                    }
                } else if ("-".startsWith(movingWall)) {
                    for (int d = first; d <= last; d++) {
                        dates.remove(d);
                    }
                }
            }
        }
        if (!dates.isEmpty()) {
            this.firstdate = dates.get(0);
            this.lastdate = dates.get(dates.size() - 1);
        }
        this.dates = newTreeSet(dates);
    }

    private final static Pattern movingWallPattern = Pattern.compile("^[+-](\\d+)Y$");

    protected Map<String, Object> buildInfo() {
        Map<String, Object> m = newHashMap();
        String s = getString("ezb:ill_relevance.ezb:ill_code");
        if (s != null) {
            switch (s) {
                case "n":
                case "no":
                case "none":
                case "nein": {
                    servicetype = "interlibrary";
                    servicemode = "none";
                    servicedistribution = "none";
                    break;
                }
                case "l":
                case "copy-loan":
                case "ja, Leihe und Kopie": {
                    servicetype = "interlibrary";
                    servicemode = Arrays.asList("copy", "loan");
                    servicedistribution = "unrestricted";
                    break;
                }
                case "copy-loan-domestic":
                case "ja, Leihe und Kopie (nur Inland)": {
                    servicetype = "interlibrary";
                    servicemode = Arrays.asList("copy", "loan");
                    servicedistribution = "domestic";
                    break;
                }
                case "k":
                case "copy":
                case "ja, nur Kopie": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "unrestricted";
                    break;
                }
                case "kn":
                case "copy-domestic":
                case "ja, nur Kopie (nur Inland)": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "domestic";
                    break;
                }
                case "e":
                case "copy-electronic":
                case "ja, auch elektronischer Versand an Nutzer": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "electronic";
                    break;
                }
                case "en":
                case "copy-electronic-domestic":
                case "ja, auch elektronischer Versand an Nutzer (nur Inland)": {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = Arrays.asList("electronic", "domestic");
                    break;
                }
                default: {
                    throw new IllegalArgumentException("unknown service code: " + s);
                }
            }
        }
        // additional qualifiers for service distribution
        String q = getString("ezb:ill_relevance.ezb:inland_only");
        String r = getString("ezb:ill_relevance.ezb:il_electronic_forbidden");
        if ("true".equals(q) && "true".equals(r)) {
            servicedistribution = Arrays.asList("postal", "domestic");
        } else if ("true".equals(q)) {
            servicedistribution = "domestic";
        } else if ("true".equals(r)) {
            servicedistribution = "postal";
        }
        String comment = getString("ezb:ill_relevance.ezb:comment");
        if (!Strings.isNullOrEmpty(comment)) {
            servicecomment = comment;
        }
        Map<String, Object> group = newHashMap();
        // first date and last date is obligatory
        group.put("begindate", getString("ezb:license_period.ezb:first_date"));
        group.put("enddate", getString("ezb:license_period.ezb:last_date"));
        // volume is optional
        String firstVolume = getString("ezb:license_period.ezb:first_volume");
        if (firstVolume != null) {
            group.put("beginvolume", firstVolume);
        }
        String lastVolume = getString("ezb:license_period.ezb:last_volume");
        if (lastVolume != null) {
            group.put("endvolume", lastVolume);
        }
        // issue is optional
        String firstIssue = getString("ezb:license_period.ezb:first_issue");
        if (firstIssue != null) {
            group.put("beginissue", firstIssue);
        }
        String lastIssue = getString("ezb:license_period.ezb:last_issue");
        if (lastIssue != null) {
            group.put("endissue", lastIssue);
        }
        Map<String, Object> holdings = newHashMap();
        holdings.put("group", group);
        m.put("holdings", holdings);

        Map<String, Object> link = newHashMap();
        link.put("uri", map.get("ezb:reference_url"));
        link.put("nonpublicnote", "Verlagsangebot"); // ZDB = "Volltext"
        m.put("links", Collections.singletonList(link));

        this.license = newHashMap();
        license.put("type", map.get("ezb:type_id"));
        license.put("scope", map.get("ezb:license_type_id"));
        license.put("charge", map.get("ezb:price_type_id"));
        license.put("readme", map.get("ezb:readme_url"));
        m.put("license", license);

        return m;
    }

    @Override
    protected Integer findPriority() {
        if (carrierType == null) {
            return 9;
        }
        switch (carrierType) {
            case "online resource":
                return (servicedistribution != null
                        && servicedistribution.toString().contains("postal")) ? 3 : 1;
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

}
