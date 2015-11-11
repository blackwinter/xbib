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
package org.xbib.io.iso23950.client;

import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.iso23950.ZConstants;
import org.xbib.io.iso23950.ZSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

/**
 *  Factory for Z client
 *
 */
public final class ZClientFactory implements ZConstants {

    private final ConnectionService<ZSession> connectionService;

    private final static ZClientFactory instance = new ZClientFactory();

    private ZClientFactory() {
        connectionService = ConnectionService.getInstance();
    }

    public static ZClientFactory getInstance() {
        return instance;
    }

    public Properties getProperties(String name) {
        Properties properties = new Properties();
        InputStream in = instance.getClass().getResourceAsStream("/org/xbib/io/iso23950/service/" + name + ".properties");
        if (in != null) {
            try {
                properties.load(in);
            } catch (IOException ex) {
                // ignore
            }
        }
        if (in == null || properties.isEmpty()) {
            throw new IllegalArgumentException("service " + name + " not found");
        }
        return properties;
    }

    public ZClient newZClient(String name) {
        return newZClient(getProperties(name));
    }

    public ZClient newZClient(Properties properties ) {
        try {
            URI uri = URI.create(properties.getProperty(ADDRESS_PROPERTY));
            Connection<ZSession> connection = connectionService
                    .getConnectionFactory(uri)
                    .getConnection(uri);
            ZSession session = connection.createSession();
            return session.newZClient(properties);
        } catch (IOException ex) {
            return null;
        }
    }


}
