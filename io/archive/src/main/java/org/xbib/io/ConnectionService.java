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
package org.xbib.io;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

/**
 * The Connection service
 */
public final class ConnectionService<S extends Session> {

    private final Map<String, ConnectionFactory<S>> factories = new WeakHashMap<>();

    private final static ConnectionService instance = new ConnectionService();

    private ConnectionService() {
        ServiceLoader<ConnectionFactory> loader = ServiceLoader.load(ConnectionFactory.class);
        for (ConnectionFactory factory : loader) {
            if (!factories.containsKey(factory.getName())) {
                factories.put(factory.getName(), factory);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <S extends Session> ConnectionService<S> getInstance() {
        return instance;
    }

    public ConnectionFactory<S> getConnectionFactory(String name)
            throws IOException {
        if (factories.containsKey(name)) {
            return factories.get(name);
        }
        throw new ServiceConfigurationError("no connection factory found for scheme " + name);
    }

    public ConnectionFactory<S> getConnectionFactory(URI uri) {
        for (ConnectionFactory<S> factory : factories.values()) {
            if (factory.canOpen(uri)) {
                return factory;
            }
        }
        throw new ServiceConfigurationError("no connection factory found for " + uri);
    }
}
