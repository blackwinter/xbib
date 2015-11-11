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
package org.xbib.etl.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifierMapper {

    private final static Pattern p = Pattern.compile("^1\\s(.{21})(.{5}).*");

    private Map<String, String> map = new HashMap<>();

    public Map<String, String> load(InputStream in) throws IOException {
        BufferedReader lr = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
        new TextProcessor().execute(lr, new LineProcessor() {

            @Override
            public void process(String line) throws IOException {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String sigel = m.group(1).trim();
                    String owner = m.group(2).trim();
                    String isil = map.containsKey(sigel) ? map.get(sigel) : createISIL(sigel);
                    map.put(owner, isil);
                    map.put(sigel, isil);
                }
            }

            private String createISIL(String sigel) {
                String isil = sigel;
                isil = isil.replaceAll("ä", "ae").replaceAll("ö", "oe").replaceAll("ü", "ue").replaceAll("\\s+", "");
                isil = isil.replace('/', '-');
                // heuristic
                if (!isil.startsWith("ZDB")) {
                    isil = "DE-" + isil;
                }
                return isil;
            }
        });
        lr.close();
        return map;
    }

    public IdentifierMapper add(Map<String,String> map) {
        this.map.putAll(map);
        return this;
    }

    public Map<String,String> getMap() {
        return map;
    }

    public String lookup(String value) {
        return map.containsKey(value) ? map.get(value) : value;
    }

    interface LineProcessor {

        void process(String line) throws IOException;
    }

    class TextProcessor {

        void execute(BufferedReader lr, LineProcessor lp) throws IOException {
            try {
                String line;
                while ((line = lr.readLine()) != null) {
                    if (line.trim().length() > 0 && !line.startsWith("!")) {
                        lp.process(line);
                    }
                }
            } finally {
                if (lr != null) {
                    lr.close();
                }
            }
        }
    }
}
