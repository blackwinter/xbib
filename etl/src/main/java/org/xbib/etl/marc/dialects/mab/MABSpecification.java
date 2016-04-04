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
package org.xbib.etl.marc.dialects.mab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.DefaultSpecification;
import org.xbib.etl.Entity;

import java.util.HashMap;
import java.util.Map;

public class MABSpecification extends DefaultSpecification<MABEntity> {

    private final static Logger logger = LogManager.getLogger(MABSpecification.class.getName());

    private String value;

    public MABSpecification(Map<String, MABEntity> entites, Map<String, Object> params, ClassLoader cl, String packageName, String... paths) throws Exception {
        super(entites, params, cl, packageName, paths);
    }

    @Override
    public Map addKey(String value, Entity entity, Map map) {
        this.value = value;
        int pos = value.indexOf('$');
        String h = pos > 0 ? value.substring(0,pos) : null;
        String t = pos > 0 ? value.substring(pos+1) : value;
        addKey(h, t, entity, map);
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map addKey(String head, String tail, Entity entity, Map map) {
        if (head == null) {
            if (map.containsKey(tail)) {
                logger.warn("already exist in map: value={} {} {}", value, tail, map);
                return map;
            }
            map.put(tail, entity);
            return map;
        }
        int pos = tail != null ? tail.indexOf('$') : 0;
        String h = pos > 0 ? tail.substring(0, pos) : null;
        String t = pos > 0 ? tail.substring(pos+1) : tail;
        Object o = map.get(head);
        if (o instanceof Map) {
            addKey(h, t, entity, (Map) o);
            return map;
        } else {
            Map m = new HashMap();
            Map n = addKey(h, t, entity, m);
            map.put(head, n);
            return map;
        }
    }

}
