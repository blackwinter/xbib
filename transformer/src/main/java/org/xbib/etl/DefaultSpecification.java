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
package org.xbib.etl;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultSpecification<E extends Entity> implements Specification<E> {

    private final static Logger logger = LogManager.getLogger(DefaultSpecification.class.getName());

    private final Map<String, Object> map;

    private final Map<String, E> entities;

    private final Map<String,Object> params;

    public DefaultSpecification(InputStream inputStream, Map<String,E> entites, Map<String,Object> params,
                                String packageName) throws Exception {
        this.entities = entites;
        this.params = params;
        this.map = new TreeMap<>();
        init(inputStream, packageName);
        logger.info("initialized map of {} keys", map.size());
    }

    @Override
    public Map<String,Object> getMap() {
        return map;
    }

    @Override
    public Map<String,E> getEntities() {
        return entities;
    }

    @SuppressWarnings("unchecked")
    private void init(InputStream inputStream, String packageName) throws Exception {
        Map<String, Map<String, Object>> defs = new HashMap<>();
        if (inputStream != null) {
            defs = new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true).readValue(inputStream, Map.class);
            inputStream.close();
        } else {
            logger.warn("no specification input stream found");
        }
        for (Map.Entry<String, Map<String,Object>> entry : defs.entrySet()) {
            String key = entry.getKey();
            Map<String,Object> struct = entry.getValue();
            // allow override static struct map from json with given params
            struct.putAll(params);
            E entity = null;
            // load class
            Class clazz = loadClass(getClass().getClassLoader(), packageName + "." + key);
            if (clazz == null) {
                // custom class name, try without package
                clazz = loadClass(getClass().getClassLoader(), key);
            }
            if (clazz != null) {
                try {
                    entity = (E)clazz.getDeclaredConstructor(Map.class).newInstance(struct);
                } catch (Throwable t1) {
                    try {
                        entity = (E) clazz.newInstance();
                    } catch (Throwable t2) {
                        logger.error("can't instantiate class " + clazz.getName());
                    }
                }
                if (entity != null) {
                    entities.put(packageName + "." + key, entity);
                }
            }
            // connect each value to an entity class
            Collection<String> values = (Collection<String>) struct.get("values");
            if (values != null) {
                for (String value : values) {
                    addKey(value, entity, this.map);
                }
            }
        }
    }

    public Map addKey(String value, Entity entity, Map map) {
        // we do not add anything by default .... have to be overridden
        return map;
    }

    @Override
    public E getEntity(String key, Map map) {
        if (key == null) {
            return null;
        }
        int pos = key.indexOf('$');
        String h = pos > 0 ? key.substring(0, pos) : null;
        String t = pos > 0 ? key.substring(pos+1) : key;
        return getEntity(h, t, map);
    }

    @SuppressWarnings("unchecked")
    public void dump(Writer writer) throws IOException {
        Map<String,Object> m = getMap();
        Map<String,List<String>> elements = new TreeMap<>();
        dump(elements, null, m);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, elements);
    }

    @SuppressWarnings("unchecked")
    private E getEntity(String head, String tail, Map map) {
        if (head == null) {
            return (E)map.get(tail);
        }
        int pos = tail != null ? tail.indexOf('$') : 0;
        String h = pos > 0 ? tail.substring(0, pos) : null;
        String t = pos > 0 ? tail.substring(pos+1) : tail;
        Object o = map.get(head);
        if (o != null) {
            return o instanceof Map ? getEntity(h, t, (Map)o) :
                   o instanceof Entity ? (E)o : null;
        } else {
            return null;
        }
    }

    /*private String getString(Reader input) throws IOException {
        StringWriter sw = new StringWriter();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n;
        while ((n = input.read(buffer)) != -1) {
            sw.write(buffer, 0, n);
        }
        return sw.toString();
    }*/

    /*private InputStream loadResource(ClassLoader cl, String resourcePath) {
        // load from root of jar
        InputStream in = cl.getResourceAsStream(resourcePath);
        if (in == null) {
            // load from same path as class ElementMap
            in = DefaultSpecification.class.getResourceAsStream(resourcePath);
            if (in == null) {
                // last resort: load from system class path
                in = ClassLoader.getSystemResourceAsStream(resourcePath);
            }
        }
        return in;
    }*/

    private Class loadClass(ClassLoader cl, String className) {
        Class clazz = null;
        try {
            // load from custom class loader        
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                // last resort: load from system class loader
                try {
                    clazz = ClassLoader.getSystemClassLoader().loadClass(className);
                } catch (ClassNotFoundException e2) {
                    logger.warn("missing class: " + e.getMessage());
                }
            }
        }
        return clazz;
    }

    @SuppressWarnings("unchecked")
    private void dump(Map<String,List<String>> elements, String key, Map<String,Object> m) {
        for (Map.Entry<String,Object> entry : m.entrySet()) {
            String k = entry.getKey();
            Object o = m.get(k);
            String kk = key == null ? k : key + "$" + k;
            if (o instanceof Map) {
                dump(elements, kk, (Map) o);
            } else if (o instanceof Entity) {
                Entity e = (Entity)o;
                String elemKey = e.getClass().getSimpleName();
                List<String> l = elements.get(elemKey);
                if (l == null) {
                    l = new LinkedList<>();
                }
                l.add(kk);
                elements.put(elemKey, l);
            }
        }
    }
}
