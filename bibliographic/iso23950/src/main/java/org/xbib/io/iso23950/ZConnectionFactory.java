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
import java.io.InputStream;
import java.net.URI;
import org.xbib.io.ConnectionFactory;

/**
 * A Z39.50 connection factory
 *
 */
public final class ZConnectionFactory implements ConnectionFactory<ZSession> {

    @Override
    public String getName() {
        return "z3950";
    }

    /**
     * A new connection
     *
     * @param uri the Z39.50 URI
     *
     * @return a Z39.50 connection
     *
     * @throws IOException if the Z39.50 connection can not be opened
     */
    @Override
    public ZConnection getConnection(final URI uri) throws IOException {
        ZConnection conn = new ZConnection();
        conn.setURI(uri);
        return conn;
    }

    @Override
    public InputStream open(URI uri) throws IOException {
        return null; // TODO (how to stream a z3950 URL?)
    }

    @Override
    public boolean canOpen(URI uri) {
        return uri.getScheme().startsWith("z3950");
    }
}
