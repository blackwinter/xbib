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
package org.xbib.tools.convert.ezb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.Converter;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Harvest inter library loan codes from EZB web service
 */
public class FromEZBWeb extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(FromEZBWeb.class.getSimpleName());

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromEZBWeb();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("prism", "http://prismstandard.org/namespaces/basic/2.1/");
        SimpleResourceContext resourceContext = new SimpleResourceContext();
        resourceContext.setNamespaceContext(context);

        Reader reader = new InputStreamReader(InputService.getInputStream(uri), "UTF-8");
        Writer writer = new FileWriter(settings.get("output"));

        final TurtleWriter turtle = new TurtleWriter()
                .setContext(context)
                .output(writer);
        Iterator<String> it = readZDBIDs(reader);
        long counter = 0;
        while (it.hasNext()) {
            String zdbid = it.next();
            StringBuilder sb = new StringBuilder();
            sb.append(zdbid).insert(sb.length() - 1, '-');
            URL url = new URL(settings.get("url") + sb.toString());
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            br.readLine(); // ZDB-Id: ...
            br.readLine(); // Treffer: ...
            br.readLine(); // empty line
            Scanner scanner = new Scanner(br);
            scanner.useDelimiter("\t|\n");
            while (scanner.hasNext()) {
                try {
                    String sigel = scanner.next();
                    String isil = scanner.next();
                    String name = scanner.next();
                    String code1 = scanner.next();
                    String code2 = scanner.next();
                    String code3 = scanner.next();
                    String comment = scanner.next();
                    String firstDate = scanner.next();
                    String firstVolume = scanner.next();
                    String firstIssue = scanner.next();
                    String lastDate = scanner.next();
                    String lastVolume = scanner.next();
                    String lastIssue = scanner.next();
                    String movingWall = scanner.next();
                    // skip fake entry
                    if ("AAAAA".equals(sigel)) {
                        continue;
                    }
                    String key = zdbid + "_" + isil + "_" + firstDate + "_" + lastDate + "_" + (movingWall.isEmpty() ? "0" : movingWall);
                    IRI id = IRI.builder().scheme("http")
                            .host("xbib.info")
                            .path("/ezbws/" + key + "/")
                            .build();
                    Resource resource = resourceContext.newResource();
                    resource.id(id)
                            .add("dc:identifier", key)
                            .add("xbib:identifier", zdbid)
                            .add("xbib:isil", isil)
                            .add("xbib:firstDate", firstDate)
                            .add("xbib:firstVolume", firstVolume)
                            .add("xbib:firstIssue", firstIssue)
                            .add("xbib:lastDate", lastDate)
                            .add("xbib:lastVolume", lastVolume)
                            .add("xbib:lastIssue", lastIssue)
                            .add("xbib:interlibraryloanCode",
                                    (code1.isEmpty() ? "x" : code1)
                                            + (code2.isEmpty() ? "x" : code2)
                                            + (code3.isEmpty() ? "x" : code3))
                            .add("xbib:comment", comment);
                    turtle.write(resource);
                    counter++;
                    if (counter % 1000 == 0) {
                        logger.info("{}", counter);
                    }
                } catch (NoSuchElementException e) {
                    logger.info("{}", zdbid);
                    logger.error(e.getMessage(), e);
                }
            }
            br.close();
        }
        turtle.close();
        writer.close();
        reader.close();
    }

    private Iterator<String> readZDBIDs(Reader reader) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> l = mapper.readValue(reader, List.class);
        return l.iterator();
    }

}
