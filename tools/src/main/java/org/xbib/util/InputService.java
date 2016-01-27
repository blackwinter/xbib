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

import org.xbib.common.unit.ByteSizeValue;
import org.xbib.io.StreamCodecService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public final class InputService {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final static StreamCodecService streamCodecService = StreamCodecService.getInstance();

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
        InputStream in;
        try {
            in = uri.toURL().openStream();
        } catch (MalformedURLException e) {
            // ordinary file path?
            Path path = Paths.get(uri.getSchemeSpecificPart());
            if (Files.isRegularFile(path)) {
                in = Files.newInputStream(path);
            } else {
                throw new IOException("can't open for input, check file existence or access rights: "
                        + uri.getSchemeSpecificPart());
            }
        }
        /*} else {
            // URL?
            try {
                in = uri.toURL().openStream();
            } catch (MalformedURLException e) {
                // ordinary file path?
                Path path = Paths.get(uri.getSchemeSpecificPart());
                if (Files.isRegularFile(path)) {
                    in = Files.newInputStream(path);
                } else {
                    throw new IOException("can't open for input, check file existence or access rights: "
                            + uri.getSchemeSpecificPart());
                }
            }
        }
        if (!"file".equals(uri.getScheme())) {
            // hack for non-file URIs: apply compression codecs if possible
            // file:// scheme is already decoded :)
            for (String codec : StreamCodecService.getCodecs()) {
                if (uri.getSchemeSpecificPart().endsWith("." + codec) || (suffix != null && suffix.equals(codec))) {
                    in = bufferSize != null ?
                            streamCodecService.getCodec(codec).decode(in, bufferSize.bytesAsInt()) :
                            streamCodecService.getCodec(codec).decode(in);
                    break;
                }
            }
        }*/
        return in;
    }

    public static Set<String> asLinesFromResource(String name) {
        Set<String> set = new HashSet<>();
        try {
            InputStream in = InputService.class.getResourceAsStream(name);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, UTF8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    set.add(line);
                }
            }
        } catch (IOException e) {
            // ignore
        }
        return set;
    }

}
