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
package org.xbib.io.iso23950;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.xbib.io.Session;
import org.xbib.io.iso23950.client.DefaultZClient;
import org.xbib.io.iso23950.client.PropertiesZClient;
import org.xbib.io.iso23950.client.ZClient;
import org.xbib.io.iso23950.service.ZService;

/**
 * Z Session
 *
 */
public class ZSession implements Session<ZPacket>, ZService {

    private final ZConnection connection;

    private Properties properties;

    private boolean isOpen;

    private boolean auth;

    /**
     * Creates a new ZSession object.
     */
    public ZSession(ZConnection connection) throws IOException {
        this.connection = connection;
    }

    public ZSession setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public ZPacket newPacket() {
        return null;
    }

    @Override
    public ZPacket read() throws IOException {
        return null;
    }

    @Override
    public void write(ZPacket packet) throws IOException {
    }

    @Override
    public void open(Mode mode) throws IOException {
        if (!connection.isConnected()) {
            connection.connect();
        }
        this.isOpen = connection.isConnected();
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    @Override
    public ZClient newZClient() {
        return properties != null ? new PropertiesZClient(this, properties) : new DefaultZClient(this);
    }

    @Override
    public void close(ZClient client) throws IOException {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public URI getURI() {
        return connection.getURI();
    }

    public ZConnection getConnection() {
        return connection;
    }
    
    public void setAuthenticated(boolean auth) {
        this.auth = auth;
    }
    
    public boolean isAuthenticated() {
        return auth;
    }

    public PropertiesZClient newZClient(Properties properties) {
        return new PropertiesZClient(this, properties);
    }
}

