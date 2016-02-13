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

import org.xbib.util.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class License extends Holding {

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private final static Pattern movingWallYearPattern = Pattern.compile("^[+-](\\d+)Y$");

    private final static Pattern movingWallMonthPattern = Pattern.compile("^[+-](\\d+)M$");

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
        setServiceISIL(isil);
        setDeleted("delete".equals(getString("ezb:action")));
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
        String movingWall = getString("ezb:license_period.ezb:moving_wall");
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
        String firstDateStr = getString("ezb:license_period.ezb:first_date");
        String lastDateStr = getString("ezb:license_period.ezb:last_date");
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
        this.dates = new TreeSet<>(dates);
    }

    protected Map<String, Object> buildInfo() {
        Map<String, Object> m = new HashMap<>();
        String s = getString("ezb:ill_relevance.ezb:ill_code");
        if (s != null) {
            switch (s) {
                case "n":
                case "no":
                case "none":
                case "nein": {
                    setServiceType("interlibrary");
                    setServiceMode("none");
                    setServiceDistribution("none");
                    break;
                }
                case "l":
                case "copy-loan":
                case "ja, Leihe und Kopie": {
                    setServiceType("interlibrary");
                    setServiceMode(Arrays.asList("copy", "loan"));
                    setServiceDistribution("unrestricted");
                    break;
                }
                case "copy-loan-domestic":
                case "ja, Leihe und Kopie (nur Inland)": {
                    setServiceType("interlibrary");
                    setServiceMode(Arrays.asList("copy", "loan"));
                    setServiceDistribution("domestic");
                    break;
                }
                case "k":
                case "copy":
                case "ja, nur Kopie": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution("unrestricted");
                    break;
                }
                case "kn":
                case "copy-domestic":
                case "ja, nur Kopie (nur Inland)": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution("domestic");
                    break;
                }
                case "e":
                case "copy-electronic":
                case "ja, auch elektronischer Versand an Nutzer": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution("electronic");
                    break;
                }
                case "en":
                case "copy-electronic-domestic":
                case "ja, auch elektronischer Versand an Nutzer (nur Inland)": {
                    setServiceType("interlibrary");
                    setServiceMode("copy");
                    setServiceDistribution(Arrays.asList("electronic", "domestic"));
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
            setServiceDistribution(Arrays.asList("postal", "domestic"));
        } else if ("true".equals(q)) {
            setServiceDistribution("domestic");
        } else if ("true".equals(r)) {
            setServiceDistribution("postal");
        }
        String comment = getString("ezb:ill_relevance.ezb:comment");
        if (!Strings.isNullOrEmpty(comment)) {
            setServiceComment(comment);
        }
        Map<String, Object> group = new HashMap<>();
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
        // moving wall
        if (delta != null) {
            group.put("delta", delta);
        }
        Map<String, Object> holdings = new HashMap<>();
        holdings.put("group", group);
        m.put("holdings", holdings);
        Map<String, Object> link = new HashMap<>();
        link.put("uri", map.get("ezb:reference_url"));
        link.put("nonpublicnote", "Verlagsangebot"); // ZDB = "Volltext"
        m.put("links", Collections.singletonList(link));
        this.license = new HashMap<>();
        license.put("type", map.get("ezb:type_id"));
        license.put("scope", map.get("ezb:license_type_id"));
        license.put("charge", map.get("ezb:price_type_id"));
        license.put("readme", map.get("ezb:readme_url"));
        m.put("license", license);
        return m;
    }

    @Override
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
            case "online resource":
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
