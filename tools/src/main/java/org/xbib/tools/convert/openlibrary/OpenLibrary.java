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
package org.xbib.tools.convert.openlibrary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.util.InputService;
import org.xbib.tools.convert.Converter;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * OpenLibrary converter from couchdb JSON.
 * This conversion generates NTriples or Turtle format.
 */
public class OpenLibrary extends Converter {

    private final static Logger logger = LogManager.getLogger(OpenLibrary.class.getSimpleName());

    @Override
    protected WorkerProvider provider() {
        return p -> new OpenLibrary().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        String output = settings.get("output");
        if (!output.endsWith(".gz")) {
            output = output + ".gz";
        }
        OutputStream out = new GZIPOutputStream(new FileOutputStream(output)) {
            {
                def.setLevel(Deflater.BEST_COMPRESSION);
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] l = line.split("\t"); // type, unique key, revision, last modified, JSON
            Map<String, Object> m = mapper.readValue(l[4], Map.class);
            logger.info("{}", m);
        }
        reader.close();
        //NTripleWriter writer = new NTripleWriter()
        //        .output(out);
        //new TurtleReader(base)
        //        .setTripleListener(writer)
        //        .parse(in);
        in.close();
        out.close();
    }

}

