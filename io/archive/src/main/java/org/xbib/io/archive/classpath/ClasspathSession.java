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
package org.xbib.io.archive.classpath;

import org.xbib.io.Session;
import org.xbib.io.StreamCodecService;
import org.xbib.io.StringPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

/**
 * A Classpath stream session
 */
public class ClasspathSession<P extends StringPacket> implements Session<P> {

    private final static StreamCodecService factory = StreamCodecService.getInstance();

    private final static String encoding = System.getProperty("file.encoding");

    private URI uri;

    private boolean isOpen;

    private Reader reader;

    public ClasspathSession(URI uri) {
        this.isOpen = false;
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        this.uri = uri;
    }

    @Override
    public void open(Mode mode) throws IOException {
        if (isOpen()) {
            return;
        }
        this.isOpen = false;
        String filename = uri.getSchemeSpecificPart();
        if ("filegz".equals(uri.getScheme()) && !filename.endsWith(".gz")) {
            filename = filename + ".gz";
        } else if ("filebz2".equals(uri.getScheme()) && !filename.endsWith(".bz2")) {
            filename = filename + ".bz2";
        } else if ("filexz".equals(uri.getScheme()) && !filename.endsWith(".xz")) {
            filename = filename + ".xz";
        }
        switch (mode) {
            case READ: {
                if (filename.endsWith(".gz")) {
                    InputStream in = getClass().getResourceAsStream(filename);
                    this.reader = new InputStreamReader(factory.getCodec("gz").decode(in), encoding);
                    this.isOpen = true;
                } else if (filename.endsWith(".bz2")) {
                    InputStream in = getClass().getResourceAsStream(filename);
                    this.reader = new InputStreamReader(factory.getCodec("bz2").decode(in), encoding);
                    this.isOpen = true;
                } else if (filename.endsWith(".xz")) {
                    InputStream in = getClass().getResourceAsStream(filename);
                    this.reader = new InputStreamReader(factory.getCodec("xz").decode(in), encoding);
                    this.isOpen = true;
                } else {
                    InputStream in = getClass().getResourceAsStream(filename);
                    this.reader = new InputStreamReader(in, encoding);
                    this.isOpen = true;
                }
                break;
            }
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            this.isOpen = false;
        }
    }

    public P newPacket() {
        return (P) new StringPacket();
    }

    public P read() throws IOException {
        char[] ch = new char[1024];
        reader.read(ch);
        return (P) new StringPacket().packet(new String(ch));
    }

    public void write(P packet) throws IOException {
        // do nothing
    }

}
