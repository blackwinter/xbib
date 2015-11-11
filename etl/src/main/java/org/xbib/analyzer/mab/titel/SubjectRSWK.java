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

public class SubjectRSWK extends MABEntity {

    private final static SubjectRSWK element = new SubjectRSWK();

    public static SubjectRSWK getInstance() {
        return element;
    }

    /*
         * alt: 9 ID p Personenschlagwort g geographisch-ethnographisches
         * Schlagwort s Sachschlagwort k Koerperschaftsschlagwort: Ansetzung
         * unter dem Individualnamen c Koerperschaftsschlagwort: Ansetzung unter
         * dem Ortssitz z Zeitschlagwort f Formschlagwort t Werktitel als
         * Schlagwort blank Unterschlagwort einer Ansetzungskette
         *
         *
         * neu: 902: Unterfelder: p = Personenschlagwort (NW) g = Geografikum
         * (Gebietskörperschaft) (NW) e = Kongressname (NW) k = Körperschaft s =
         * Sachschlagwort (NW), Version (NW) b = Untergeordnete Körperschaft,
         * untergeordnete Einheit (W) c = Beiname (NW), Ort (NW) d = Datum (NW)
         * h = Zusatz (W) z = Zeitschlagwort = geographische Unterteilung (W) f
         * = Formschlagwort (NW), Erscheinungsjahr eines Werkes (NW) t =
         * Werktitel als Schlagwort (NW) m = Besetzung im Musikbereich (W) n =
         * Zählung (NW) o = Angabe des Musikarrangements (NW) u = Titel eines
         * Teils/einer Abteilung eines Werkes (W) r = Tonart (NW) x =
         * nachgeordneter Teil (W) 9 = GND-Identifikationsnummer a =
         * (Alt-)Schlagwort ohne IDN-Verknüpfung (NW)
         *
     */

    @Override
    public String data(MABEntityQueue.MABWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if (value == null) {
            return null;
        }
        if ("subjectIdentifier".equals(property)) {
            resource.add("subjectIdentifier", value);
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
