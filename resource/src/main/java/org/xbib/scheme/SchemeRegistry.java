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
package org.xbib.scheme;

import java.util.HashMap;
import java.util.Map;

/**
 * Static registry of custom IRI schemes.
 */
public final class SchemeRegistry {

    private static SchemeRegistry registry;

    public static SchemeRegistry getInstance() {
        if (registry == null) {
            registry = new SchemeRegistry();
        }
        return registry;
    }

    private final Map<String, Scheme> schemes;

    SchemeRegistry() {
        schemes = new HashMap<String, Scheme>();
        schemes.put(HttpScheme.NAME, new HttpScheme());
        schemes.put(HttpsScheme.NAME, new HttpsScheme());
        schemes.put(FtpScheme.NAME, new FtpScheme());
    }

    public boolean register(String schemeClass) throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        Class<Scheme> klass = (Class<Scheme>) Thread.currentThread().getContextClassLoader().loadClass(schemeClass);
        return register(klass);
    }

    public boolean register(Class<Scheme> schemeClass) throws IllegalAccessException,
            InstantiationException {
        Scheme scheme = schemeClass.newInstance();
        return register(scheme);
    }

    public boolean register(Scheme scheme) {
        String name = scheme.getName();
        if (schemes.get(name) == null) {
            schemes.put(name.toLowerCase(), scheme);
            return true;
        } else {
            return false;
        }
    }

    public Scheme getScheme(String scheme) {
        if (scheme == null) {
            return null;
        }
        Scheme s = schemes.get(scheme.toLowerCase());
        return (s != null) ? s : new DefaultScheme(scheme);
    }

}
