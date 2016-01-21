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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.scripting.ScriptEntity;

public class DefaultSpecification implements Specification {

    private final static Logger logger = LogManager.getLogger(DefaultSpecification.class.getName());

    private final static Map<String, Map> maps = new TreeMap<>();

    private final static Map<String, Entity> entities = new HashMap<>();

    private final static ReentrantLock lock = new ReentrantLock();

    private final static int DEFAULT_BUFFER_SIZE = 8192;

    private Map<String,Object> params = new HashMap<>();

    private Map<String,List<String>> elements;

    public DefaultSpecification() {
    }

    public DefaultSpecification addParameters(Map<String,Object> params) {
        this.params.putAll(params);
        return this;
    }

    public Map<String,Map> map() {
        return maps;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map getEntityMap(ClassLoader cl, String packageName, String... paths) throws Exception {
        if (paths == null || paths.length == 0) {
            throw new IllegalArgumentException("no path");
        }
        String path = paths[0];
        if (maps.containsKey(path)) {
            return maps.get(path);
        }
        try {
            if (lock.isLocked()) {
                try {
                    logger.info("waiting for initialized specification");
                    lock.tryLock(30, TimeUnit.SECONDS);
                    if (maps.containsKey(path)) {
                        return maps.get(path);
                    }
                } catch (InterruptedException e) {
                    logger.warn("interrupted");
                }
            } else {
                lock.lock();
            }
            final Map elementMap = new TreeMap<>();
            for (String s : paths) {
                logger.info("initializing from {}", s);
                InputStream in = loadResource(cl, s);
                if (in == null) {
                    String msg = "not found: " + s;
                    throw new IOException(msg);
                }
                Map<String, Map<String, Object>> defs = new ObjectMapper()
                        .configure(Feature.ALLOW_COMMENTS, true).readValue(in, Map.class);
                init(cl, packageName, s, elementMap, defs);
            }
            maps.put(path, elementMap);
            logger.info("initialized {} elements", elementMap.size());
            return maps.get(path);
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void init(ClassLoader cl, String packageName, String path, Map elementMap, Map<String, Map<String, Object>> defs)
                throws Exception {
        for (Map.Entry<String, Map<String,Object>> entry : defs.entrySet()) {
            String key = entry.getKey();
            Map<String,Object> struct = entry.getValue();
            // allow override static struct map from json with given params
            struct.putAll(params);
            Entity entity = null;
            Collection<String> values = (Collection<String>) struct.get("values");
            String type = (String) struct.get("_type");
            if (type != null && type.startsWith("application/x-")) {
                String language = type.substring("application/x-".length());
                String invocable = (String) struct.get("class");
                String source = (String) struct.get("source");
                String script = (String) struct.get("script");
                if (script != null) {
                    InputStream input = loadResource(cl, path + script);
                    if (input == null) {
                        throw new IOException(path + script + " not found: " + path + script);
                    }
                    InputStreamReader reader = new InputStreamReader(input, "UTF-8");
                    ScriptEntity scriptElement = new ScriptEntity(language, getString(reader), invocable);
                    scriptElement.setSettings(struct);
                    entity = scriptElement.getEntity();
                    reader.close();
                } else if (source != null) {
                    ScriptEntity scriptElement = new ScriptEntity(language, source, invocable);
                    scriptElement.setSettings(struct);
                    entity = scriptElement.getEntity();
                }
            } else {
                // sub resource in classpath?
                InputStream in = loadResource(cl, path + key);
                if (in != null) {
                    Map<String, Map<String, Object>> children =
                            new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true).readValue(in, Map.class);
                    // recursive
                    init(cl, packageName, path + key, elementMap, children);
                } else {
                    // load class
                    Class clazz = loadClass(cl, packageName + "." + key);
                    if (clazz == null) {
                        // custom class name, try without package
                        clazz = loadClass(cl, key);
                    }
                    if (clazz != null) {
                        Method factoryMethod = clazz.getDeclaredMethod("getInstance");
                        try {
                            entity = (Entity) factoryMethod.invoke(null);
                        } catch (NullPointerException e) {
                            logger.error("'getInstance' method declared not static in {}" + clazz.getName());
                        } catch (ClassCastException e) {
                            logger.error("not an Entity class: " + clazz.getName());
                        }
                        if (entity != null) {
                            entity.setSettings(struct);
                            entities.put(packageName + "." + key, entity);
                        }
                    }
                }
            }
            // connect each value to an element class
            if (values != null) {
                for (String value : values) {
                    addKey(value, entity, elementMap);
                }
            }
        }
    }

    @Override
    public Map<String,Entity> getEntities() {
        return entities;
    }

    @Override
    public Entity getEntity(String key, Map map) {
        int pos = key != null ? key.indexOf('$') : 0;
        String h = pos > 0 ? key.substring(0, pos) : null;
        String t = pos > 0 ? key.substring(pos+1) : key;
        return getElement(h, t, map);
    }

    @Override
    public Entity getEntityByKey(String key, Map map) {
        return getElement(null, key, map);
    }

    public Map addKey(String value, Entity entity, Map map) {
        return map;
    }

    @SuppressWarnings("unchecked")
    public void dump(String format, Writer writer) throws IOException {
        Map<String,Object> m = map().get(format);
        if (m == null) {
            throw new IOException("format "+ format + " missing");
        }
        elements = new TreeMap<>();
        dump(null, m);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, elements);
    }

    private Entity getElement(String head, String tail, Map map) {
        if (head == null) {
            return (Entity)map.get(tail);
        }
        int pos = tail != null ? tail.indexOf('$') : 0;
        String h = pos > 0 ? tail.substring(0, pos) : null;
        String t = pos > 0 ? tail.substring(pos+1) : tail;
        Object o = map.get(head);
        if (o != null) {
            return o instanceof Map ? getElement(h, t, (Map)o) :
                   o instanceof Entity ? (Entity)o : null;
        } else {
            return null;
        }
    }

    private String getString(Reader input) throws IOException {
        StringWriter sw = new StringWriter();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n;
        while ((n = input.read(buffer)) != -1) {
            sw.write(buffer, 0, n);
        }
        return sw.toString();
    }

    private InputStream loadResource(ClassLoader cl, String resourcePath) {
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
    }

    private Class loadClass(ClassLoader cl, String className) {
        Class clazz = null;
        try {
            // load from custom class loader        
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                // load from same class loader as class ElementMap
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
    private void dump(String key, Map<String,Object> m) {
        for (Map.Entry<String,Object> entry : m.entrySet()) {
            String k = entry.getKey();
            Object o = m.get(k);
            String kk = key == null ? k : key + "$" + k;
            if (o instanceof Map) {
                dump(kk, (Map) o);
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
