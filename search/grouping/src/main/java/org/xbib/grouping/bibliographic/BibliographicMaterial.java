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
package org.xbib.grouping.bibliographic;

import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration of bibliographic materials
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public enum BibliographicMaterial implements Comparable<BibliographicMaterial> {

    BOOK_WITH_ID("BOOK_ID"),

    SERIAL_WITH_ID("SERIAL_ID"),

    MUSIC_WITH_ID("MUSIC_ID"),

    REFERENCE_WITH_ID("REF_ID"),

    ARTICLE_WITH_ID("ART_ID"),

    BOOK("BOOK"),

    SERIAL("SERIAL"),

    MUSIC("MUSIC"),

    ARTICLE("ART"),

    REFERENCE("REF"),

    OTHER("OTHER"),

    UNKNOWN("X");

    private final String str;

    private Map<String, BibliographicMaterial> tokens;

    private Map<BibliographicMaterial, String> strings;

    private BibliographicMaterial(String token) {
        this.str = token;
        map(token, this);
    }

    private void map(String token, BibliographicMaterial type) {
        if (tokens == null) {
            tokens = new HashMap();
        }
        if (strings == null) {
            strings = new HashMap();
        }
        tokens.put(token, type);
        strings.put(type, token);
    }

    public String getString() {
        return str;
    }

}
