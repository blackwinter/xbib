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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class MonographHolding extends Holding {

    public MonographHolding(Map<String, Object> map, Monograph monograph) {
        super(map);
        this.identifier = makeIdentity(monograph);
    }

    @Override
    protected void build() {
        this.isil = getString("member");
        setServiceISIL(isil);
        Object o = get("interlibraryservice");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            setServiceMode(o);
            setServiceType("interlibrary");
        }
        this.info = buildInfo();
    }

    protected void makeParentIdentifier() {
    }

    private String makeIdentity(Monograph monograph) {
        StringBuilder sb = new StringBuilder();
        sb.append(monograph.getID()).append('_').append(isil);
        if (map.containsKey("callnumber")) {
            sb.append('_').append(map.get("callnumber"));
        }
        if (map.containsKey("shelfmark")) {
            sb.append('_').append(map.get("shelfmark"));
        }
        return sb.toString();
    }

    public MonographHolding setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public MonographHolding setCarrierType(String carrierType) {
        this.carrierType = carrierType;
        setPriority(this.findPriority());
        return this;
    }

    public MonographHolding setDate(Integer from, Integer to) {
        List<Integer> dates = new ArrayList<>();
        if (from != null && to != null) {
            for (Integer i = from; i <= to; i++) {
                dates.add(i);
            }
        } else if (from != null) {
            dates.add(from);
        }
        if (!dates.isEmpty()) {
            this.firstdate = dates.get(0);
            this.lastdate = dates.get(dates.size() - 1);
            this.dates = new TreeSet<>(dates);
        }
        return this;
    }

    protected Map<String, Object> buildInfo() {
        return new HashMap<>();
    }

    public String getStatus() {
        return (String)map.get("status");
    }

    @Override
    protected Integer findPriority() {
        if (carrierType == null) {
            return 9;
        }
        switch (carrierType) {
            case "online resource":
                Object o = getServiceDistribution();
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

    @Override
    public boolean equals(Object other) {
        return other instanceof Holding && toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(Holding m) {
        return toString().compareTo(m.toString());
    }

}