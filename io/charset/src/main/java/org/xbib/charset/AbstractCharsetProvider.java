/**
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
package org.xbib.charset;

import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * All character sets provided share this common parent class.
 */
public class AbstractCharsetProvider extends CharsetProvider {

    /**
     * A map for character set classes
     */
    private Map classMap;
    /**
     * A map for aliases of character set classes
     */
    private Map aliasMap;
    /**
     * A named map for aliases of character set classes
     */
    private Map aliasNameMap;
    /**
     * A character set cache
     */
    private Map cache;
    /**
     * The package prefix of this character set classes
     */
    private String packagePrefix;

    /**
     * Constructor
     */
    protected AbstractCharsetProvider() {
        classMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        aliasMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        aliasNameMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        cache = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        packagePrefix = getClass().getPackage().getName();
    }

    /**
     * Add character set to a apecific internal map
     */
    private static void put(Map map, String s, Object obj) {
        if (!map.containsKey(s)) {
            map.put(s, obj);
        }
    }

    /**
     * Add character set to internal maps
     */
    protected void charset(String s, String s1, String[] as) {
        put(classMap, s, s1);

        for (int i = 0; i < as.length; i++) {
            put(aliasMap, as[i], s);
        }

        put(aliasNameMap, s, as);
    }

    /**
     * Find the true name of a character set or character set alias.
     * @param s the alias or character set name
     * @return the true character set name
     */
    private String canonicalize(String s) {
        String s1 = (String) aliasMap.get(s);

        return (s1 == null)
                ? s : s1;
    }

    /**
     * Looks up a specific character set.
     * @param s the name or alias of a character set
     * @return the character set
     */
    protected Charset lookup(String s) {
        String s1;
        SoftReference softreference = (SoftReference) cache.get(s);

        if (softreference != null) {
            Charset charset1 = (Charset) softreference.get();

            if (charset1 != null) {
                return charset1;
            }
        }

        s1 = (String) classMap.get(s);

        if (s1 == null) {
            return null;
        }

        try {
            Charset charset2;
            Class class1 = Class.forName(packagePrefix + "." + s1, true,
                    getClass().getClassLoader());
            charset2 = (Charset) class1.newInstance();
            cache.put(s, new SoftReference(charset2));
            return charset2;
        } catch (ClassNotFoundException e1) {
            System.err.println("Class not found: " + packagePrefix + "." + s1);
        } catch (IllegalAccessException e2) {
            System.err.println("Illegal access: " + packagePrefix + "." + s1);
        } catch (InstantiationException e3) {
            System.err.println("Instantiation failed: " + packagePrefix + "." + s1);
        }
        return null;
    }

    /**
     * Find character set by a given name
     * @param s character set name
     * @return the character set if it can be provided
     *
     */
    @Override
    public final Charset charsetForName(String s) {
        return lookup(canonicalize(s));
    }

    /**
     * @return character sets provided by this provider
     */
    @Override
    public final Iterator charsets() {
        return new Iterator() {

            Iterator i = classMap.keySet().iterator();

            public boolean hasNext() {
                return i.hasNext();
            }

            public Object next() {
                return lookup((String) i.next());
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @return array of aliases of a specific character set name
     */
    public final String[] aliases(String s) {
        return (String[]) aliasNameMap.get(s);
    }
}
