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
package org.xbib.tools.feed.elasticsearch.xref;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.csv.CSVParser;
import org.xbib.grouping.bibliographic.endeavor.PublishedJournal;
import org.xbib.iri.IRI;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.io.turtle.TurtleContentParams;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.tools.input.FileInput;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

/**
 * Import serials list
 */
public class SerialsDB {

    private final static Logger logger = LogManager.getLogger(SerialsDB.class);

    private final static IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    static {
        namespaceContext.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        namespaceContext.addNamespace("prism", "http://prismstandard.org/namespaces/basic/3.0/");
    }

    private final static Map<String, Resource> serials = new ConcurrentHashMap<>();

    public void process(Settings settings, URI uri) throws IOException {
        try (InputStream in = FileInput.getInputStream(uri)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            CSVParser parser = new CSVParser(reader);
            int i = 0;
            while (true) {
                String journalTitle = parser.nextToken().trim();
                journalTitle = journalTitle
                        .replaceAll("\\p{C}", "")
                        .replaceAll("\\p{Space}", "")
                        .replaceAll("\\p{Punct}", "");
                String publisher = parser.nextToken().trim();
                String subjects = parser.nextToken();
                String issn = parser.nextToken().trim();
                String doi = parser.nextToken().trim();
                String publicationStructure = parser.nextToken();
                String[] issnArr = issn.split("\\|");
                // skip fake titles
                if ("xxxx".equals(journalTitle)) {
                    continue;
                }
                if (i++ == 0) {
                    // skip head line
                    continue;
                }
                Resource resource = new MemoryResource();
                String issn1 = buildISSN(issnArr, 0, true);
                String issn2 = buildISSN(issnArr, 1, true);
                if (issn1 != null && issn1.equals(issn2)) {
                    issn2 = null;
                }
                String key = new PublishedJournal()
                        .journalName(journalTitle)
                        .publisherName(publisher)
                        .createIdentifier();
                IRI id = IRI.builder().scheme("http")
                        .host("xbib.info")
                        .path("/endeavors/" + key + "/")
                        .build();
                resource.id(id)
                        .add("dc:identifier", key)
                        .add("dc:publisher", publisher.isEmpty() ? null : publisher)
                        .add("dc:title", journalTitle)
                        .add("prism:issn", issn1)
                        .add("prism:issn", issn2)
                        .add("prism:doi", doi.isEmpty() ? null : doi);

                if (!serials.containsKey(journalTitle)) {
                    TurtleContentParams params = new TurtleContentParams(namespaceContext, false);
                    RdfContentBuilder builder = turtleBuilder(params);
                    builder.receive(resource);
                    serials.put(journalTitle, resource);
                } else {
                    logger.info("ignoring double serial title: {}", journalTitle);
                }
            }
        } catch (EOFException e) {
            // ignore
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        if (settings.get("output") != null) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(settings.get("output") + ".txt"), Charset.forName("UTF-8")))) {
                for (Map.Entry<String,Resource> entry : serials.entrySet()) {
                    writer.write(entry.getKey() + "|" + entry.getValue());
                    writer.write("\n");
                }
            }
        }
        logger.info("{} size={}", this, getMap().size());
    }

    public Map<String, Resource> getMap() {
        return serials;
    }

    private String buildISSN(String[] issnArr, int i, boolean hyphen) {
        return issnArr.length > i && !issnArr[i].isEmpty() ?
                (hyphen ? new StringBuilder(issnArr[i].toLowerCase()).insert(4, '-').toString() : issnArr[i].toLowerCase()) :
                null;
    }

}
