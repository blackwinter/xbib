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
package org.xbib.entities.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurableClassifier {

    private final static Logger logger = LogManager.getLogger(ConfigurableClassifier.class);

    private String classifierID;

    private Map<String,Entry> map;

    public ConfigurableClassifier load(InputStream in, String name, String classifierID) throws IOException {
        return load(new InputStreamReader(in, "UTF-8"), name, classifierID);
    }

    public ConfigurableClassifier load(Reader reader, String name, String classifierID) throws IOException {
        this.map = new HashMap<String,Entry>();
        this.classifierID = classifierID;
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines()
                    .map(mapToKeyValue)
                    .filter(Objects::nonNull)
                    .forEach(kv -> map.put(name + '.' + kv.doc + '.' + kv.code, kv));
        }
        logger.debug("classifier map={}", map);
        return this;
    }

    public Map<String,Entry> getMap() {
        return map;
    }

    /**
     * One-to-one pattern matching
     * @param key the key e.g. DE-605.12345.ABC
     * @return found entry or null
     */
    public Entry lookup(String key) {
        return map.get(key);
    }

    /**
     * Full lookup with whitespace splitting and optional pattern
     * @param name the namespace e.g. DE-605
     * @param doc the doc ID e.g. 12345
     * @param code the code e.g. ABC
     * @param pattern optional regex for examining the code
     * @return found entry or null
     */
    public Entry lookup(String name, String doc, String code, Pattern pattern) {
        if (code == null || code.isEmpty() || code.trim().isEmpty()) {
            return null;
        }
        String k = name + '.' + doc + '.' + code;
        Entry entry = map.containsKey(k) ? map.get(k) : null;
        if (entry != null) {
            return entry;
        }
        if (pattern == null) {
            // split code into fragments
            String[] array = code.split("\\s+");
            for (String s : array) {
                if (s.length() > 0 && !code.equals(s)) {
                    entry = lookup(name, doc, s, null);
                    if (entry != null) {
                        return entry;
                    }
                }
            }
            return null;
        } else {
            // pattern matching
            Matcher m = pattern.matcher(code);
            if (m.find()) {
                entry = lookup(name, doc, code.substring(m.start(), m.end()), null);
                if (entry != null) {
                    return entry;
                }
            }
        }
        return null;
    }

    public Function<String, Entry> mapToKeyValue = (line) -> {
        String[] p = line.split("\t");
        return p.length > 2 ? new Entry(classifierID + p[0].trim(), p[1].trim(), p[2].trim()) : null;
    };

    public class Entry {
        String doc;
        String code;
        String text;
        Entry(String doc, String code, String text) {
            this.doc = doc;
            this.code = code;
            this.text = text;
        }

        public String getCode() {
            return code;
        }

        public String getText() {
            return text;
        }
    }
}
