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
package org.xbib.tools.merge.zdb.entities;

import org.xbib.common.xcontent.ToXContent;
import org.xbib.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newTreeSet;

public class MonographVolumeHolding extends Holding {

    private final MonographVolume volume;

    public MonographVolumeHolding(Map<String, Object> map, MonographVolume volume) {
        super(map);
        this.volume = volume;
        this.identifier = makeIdentity(volume);
    }

    private String makeIdentity(MonographVolume volume) {
        StringBuilder sb = new StringBuilder();
        sb.append(volume.id()).append('_').append(isil);
        if (map.containsKey("callnumber")) {
            sb.append('_').append(map.get("callnumber"));
        }
        if (map.containsKey("shelfmark")) {
            sb.append('_').append(map.get("shelfmark"));
        }
        return sb.toString();
    }

    protected void build() {
        this.isil = getString("member");
        Object o = get("interlibraryservice");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            this.servicemode = o;
            this.servicetype = "interlibrary";
        }
        this.info = buildInfo();
    }

    public MonographVolumeHolding setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public MonographVolumeHolding setCarrierType(String carrierType) {
        this.carrierType = carrierType;
        this.priority = findPriority();
        return this;
    }

    public MonographVolumeHolding setDate(Integer from, Integer to) {
        List<Integer> dates = newArrayList();
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
            this.dates = newTreeSet(dates);
        }
        return this;
    }

    protected Map<String, Object> buildInfo() {
        return newHashMap();
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

    @Override
    public void toXContent(XContentBuilder builder, ToXContent.Params params)
            throws IOException {
        builder.startObject();
        builder.field("identifierForTheHolding", "(" + getServiceISIL() + ")" + volume.externalID)
                .array("parents", parents);
        builder.array("date", dates)
                .startObject("institution")
                .field("isil", isil)
                .startObject("service")
                .array("parents", parents)
                .field("mediatype", mediaType)
                .field("carriertype", carrierType)
                .field("region", getRegion())
                .field("organization", getOrganization())
                .field("name", getName())
                .field("isil", getServiceISIL())
                .field("serviceisil", getServiceISIL())
                .field("priority", priority)
                .field("type", servicetype);
        // grr
        if (servicemode instanceof List) {
            builder.array("mode", (List) servicemode);
        } else {
            builder.field("mode", servicemode);
        }
        builder.startObject("info")
                .startObject("location")
                        // https://www.hbz-nrw.de/dokumentencenter/produkte/verbunddatenbank/aktuell/plausi/Exemplar-Online-Kurzform.pdf
                .fieldIfNotNull("collection", map.get("shelfmark")) // 088 b sublocation (Standort)
                .fieldIfNotNull("callnumber", map.get("callnumber")) // 088 c (Signatur)
                        //.fieldIfNotNull("collection", map.get("collection")) // 088 d zus. Bestandsangabe (nicht vorhanden)

                .endObject();
        builder.endObject().endObject();
    }
}