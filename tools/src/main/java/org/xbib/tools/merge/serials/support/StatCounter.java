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
package org.xbib.tools.merge.serials.support;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class StatCounter extends HashMap<String,Map<String,Integer>> {

    public void set(String key, String value, Integer i) {
        if (key == null || value == null) {
            return;
        }
        if (!containsKey(key)) {
            put(key, newHashMap());
        }
        Map<String,Integer> map = get(key);
        if (!map.containsKey(value)) {
            map.put(value, 0);
        }
        map.put(value, i);
        put(key, map);
    }

    public void increase(String key, String value, Integer delta) {
        if (key == null || value == null) {
            return;
        }
        if (!containsKey(key)) {
            put(key, newHashMap());
        }
        Map<String,Integer> map = get(key);
        if (!map.containsKey(value)) {
            map.put(value, 0);
        }
        Integer i = map.get(value);
        i += delta;
        map.put(value, i);
        put(key, map);
    }

    public void merge(StatCounter statCounter) {
        if (statCounter != null) {
            for (String key : statCounter.keySet()) {
                Map<String, Integer> map = statCounter.get(key);
                for (String value : map.keySet()) {
                    increase(key, value, map.get(value));
                }
            }
        }
    }
}
