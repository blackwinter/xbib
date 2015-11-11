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
package org.xbib.etl.marc;

import org.xbib.marc.Field;

import java.util.Map;

/**
 * A tag value mapper is a helper class to map MARC indicators to declarative names.
 *
 * "tag" : {
 *     "indicator1" :
 *     "indicator2" : ...
 *
 * }
 *
 */
public class TagValueMapper {

    private TagValueMapper() {
    }

    public static Map.Entry<String, Object> map(final Map indicators, final Field field) {
        String k = null;
        Object v = field.indicator();
        // first level is a tag map
        Object indicatorDef = indicators.get(field.tag());
        if (indicatorDef instanceof Map) {
            // key/value mapping
            Map indicatormap = (Map) indicatorDef;
            if (indicatormap.containsKey(v)) {
                Object o = indicatormap.get(v);
                if (o instanceof Map) {
                    Map.Entry<String, Object> me = (Map.Entry<String, Object>) ((Map) o).entrySet().iterator().next();
                    k = me.getKey();
                    v = me.getValue();
                } else {
                    v = o;
                }
            } else if (indicatormap.containsKey("")) {
                k = (String) indicatormap.get(""); // default value
            }
        } else {
            // new key (may be null to skip the value)
            k = (String)indicatorDef;
        }
        // create result map entry: return {k, v}
        final String newKey = k;
        final Object newValue = v;
        final Map.Entry<String, Object> entry = new Map.Entry<String, Object>() {
            @Override
            public String getKey() {
                return newKey;
            }

            @Override
            public Object getValue() {
                return newValue;
            }

            @Override
            public Object setValue(Object value) {
               return null;
            }
        };
        return entry;
    }
}
