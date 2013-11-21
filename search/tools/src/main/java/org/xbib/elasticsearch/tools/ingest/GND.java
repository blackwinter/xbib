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
package org.xbib.elasticsearch.tools.ingest;

import org.xbib.elasticsearch.ResourceSink;
import org.xbib.elasticsearch.support.client.IngestClient;
import org.xbib.elasticsearch.support.client.MockIngestClient;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.turtle.TurtleReader;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.options.OptionParser;
import org.xbib.options.OptionSet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Elasticsearch GND indexer
 *
 */
public class GND {

    private static final Logger logger = LoggerFactory.getLogger(GND.class.getName());

    private final static String lf = System.getProperty("line.separator");

    public static void main(String[] args) {

        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(4 * Runtime.getRuntime().availableProcessors());
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("gndfile").withRequiredArg().ofType(String.class).required();
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + EZB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --gndfile <uri>        GND file" + lf
                );
                System.exit(1);
            }
            final String elasticsearch = (String) options.valueOf("elasticsearch");
            final String index = (String) options.valueOf("index");
            final String type = (String) options.valueOf("type");
            boolean mock = (Boolean)options.valueOf("mock");

            final String uriStr = (String) options.valueOf("gndfile");
            URI uri = URI.create(uriStr);
            InputStream in = InputService.getInputStream(uri);
            if (in == null) {
                throw new IOException("file not found: " + uriStr);
            }
            final ElasticBuilder builder = new ElasticBuilder(elasticsearch, index, type, mock);

            IRI id = IRI.builder().scheme("http").host("d-nb.info").path("/gnd/").build();
            TurtleReader reader = new TurtleReader(id);
            reader.setTripleListener(builder);
            reader.parse(in);
            builder.close();
            logger.info("done, indexed resources have " + builder.getTripleCounter() + " triples.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private static class ElasticBuilder implements TripleListener {

        private final ResourceSink sink;

        private final ResourceContext context = new SimpleResourceContext();

        private long triplecounter;

        private Resource resource;

        ElasticBuilder(String esURI, String index, String type, boolean mock) throws IOException {
            final IngestClient es = mock ? new MockIngestClient() : new IngestClient();
            es.newClient(URI.create(esURI))
                    .setIndex(index)
                    .setType(type);
            sink = new ResourceSink(es);
            resource = context.newResource();
        }

        public void close() throws IOException {
            flush();
            sink.flush();
        }

        @Override
        public TripleListener startPrefixMapping(String prefix, String uri) {
            return this;
        }

        @Override
        public TripleListener endPrefixMapping(String prefix) {
            return this;
        }

        @Override
        public ElasticBuilder newIdentifier(IRI uri) {
            flush();
            resource.id(uri);
            return this;
        }

        @Override
        public ElasticBuilder triple(Triple triple) {
            resource.add(triple);
            triplecounter++;
            return this;
        }

        public long getTripleCounter() {
            return triplecounter;
        }

        private void flush() {
            try {
                sink.output(context, context.contentBuilder());
            } catch (IOException e) {
                logger.error("flush failed: {}", e.getMessage(), e);
            }
            context.reset();
            resource = context.newResource();
        }

    }
}
