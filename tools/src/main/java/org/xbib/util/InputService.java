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
package org.xbib.util;

import org.xbib.io.ConnectionFactory;
import org.xbib.io.ConnectionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public final class InputService {

    private final static int DEFAULT_BUFFER_SIZE = 8192;

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final static ConnectionService connectionService = ConnectionService.getInstance();

    private final static InputService instance = new InputService();

    private InputService() {
    }

    public static InputService getInstance() {
        return instance;
    }

    public static InputStream getInputStream(URI uri) throws IOException {
        if (uri == null || uri.getScheme() == null) {
            return null;
        }
        ConnectionFactory connectionFactory = connectionService.getConnectionFactory(uri);
        return connectionFactory.canOpen(uri) ? connectionFactory.open(uri) : null;
    }

    public static String asString(InputStream input, String encoding) throws IOException {
        return asString(new InputStreamReader(input, encoding));
    }

    public static String asString(Reader input) throws IOException {
        StringWriter output = new StringWriter();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
        return output.toString();
    }

    public static Set<String> asLinesFromResource(String name) {
        Set<String> set = new HashSet<String>();
        try {
            InputStream in = InputService.class.getResourceAsStream(name);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, UTF8));
            String line;
            while ((line = br.readLine()) != null) {
                set.add(line);
            }
            br.close();
        } catch (IOException e) {
            // ignore
        }
        return set;
    }
}
