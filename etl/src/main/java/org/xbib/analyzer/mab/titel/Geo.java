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
package org.xbib.analyzer.mab.titel;

import org.xbib.etl.marc.dialects.mab.MABEntity;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.rdf.Resource;

public class Geo extends MABEntity {

    /**
     * g = Geografikum (Gebietskörperschaft) (NW)
     * 9 = GND-Identifikationsnummer
     * h = Zusatz (W)
     * x = nachgeordneter Teil (W)
     * z = geografische Unterteilung (W)
     */

    @Override
    public String data(MABEntityQueue.MABWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if (value == null) {
            return null;
        }
        if ("geoIdentifier".equals(property)) {
            resource.add("geoIdentifier", value);
            if (value.startsWith("(DE-588)")) {
                // GND-ID: upper case, with hyphen
                resource.add("identifierGND", value.substring(8));
            } else if (value.startsWith("(DE-101)")) {
                // DNB-ID: upper case, with hyphen
                resource.add("identifierDNB", value.substring(8));
            } else if (value.startsWith("(DE-600)")) {
                // ZDB-ID does not matter at all
                resource.add("identifierZDB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                return value.replaceAll("\\-", "").toLowerCase();
            }
            return null;
        }
        return value
                .replaceAll("<<(.*?)>>", "\u0098$1\u009C")
                .replaceAll("<(.*?)>", "[$1]")
                .replaceAll("¬(.*?)¬", "\u0098$1\u009C");
    }

}
