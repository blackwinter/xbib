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
package org.xbib.tools.feed.elasticsearch.zdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.io.http.HttpRequest;
import org.xbib.marc.MarcXchangeStream;
import org.xbib.marc.xml.sax.MarcXchangeContentHandler;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.sru.client.DefaultSRUClient;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.searchretrieve.SearchRetrieveListener;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponseAdapter;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.concurrent.WorkerProvider;
import org.xbib.xml.stream.SaxEventConsumer;

import javax.xml.stream.util.XMLEventConsumer;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public class SRU extends Feeder {

    private final static Logger logger = LogManager.getLogger(SRU.class.getName());

    @Override
    @SuppressWarnings("unchecked")
    protected WorkerProvider<Converter> provider() {
        return p -> new SRU().setPipeline(p);
    }

    /*@Override
    protected void prepareInput() throws IOException, InterruptedException {
        if (settings.get("numbers") != null) {
            // fetch from SRU by number file, each line is an ID
            FileInputStream in = new FileInputStream(settings.get("numbers"));
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, UTF8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    URIWorkerRequest request = new URIWorkerRequest();
                    request.set(URI.create(String.format(settings.get("uri"), line)));
                    getQueue().put(request);
                }
            }
        } else {
            super.prepareInput();
        }
    }*/

    @Override
    public void process(URI uri) throws Exception {
        final Set<String> unmappedbib = Collections.synchronizedSet(new TreeSet<>());
        final MyBibQueue bibqueue = new MyBibQueue("marc/zdb/bib", settings.getAsInt("pipelines", 1));
        bibqueue.setUnmappedKeyListener((id,key) -> {
            if ((settings.getAsBoolean("detect", false))) {
                logger.warn("unmapped field {}", key);
                unmappedbib.add("\"" + key + "\"");
            }
        });

        final Set<String> unmappedhol = Collections.synchronizedSet(new TreeSet<>());
        final MyHolQueue holqueue = new MyHolQueue("marc/zdb/hol", settings.getAsInt("pipelines", 1));
        holqueue.setUnmappedKeyListener((id,key) -> {
            if ((settings.getAsBoolean("detect", false))) {
                logger.warn("unmapped field {}", key);
                unmappedhol.add("\"" + key + "\"");
            }
        });


        final MarcXchangeStream bib = new MarcXchangeStream()
                .setStringTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC))
                .add(bibqueue);

        final MarcXchangeStream hol = new MarcXchangeStream()
                .setStringTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC))
                .add(holqueue);

        final MarcXchangeContentHandler handler = new MarcXchangeContentHandler()
                .addListener("Bibliographic", bib)
                .addListener("Holdings", hol);

        final SearchRetrieveListener listener = new SearchRetrieveResponseAdapter() {

            @Override
            public void onConnect(HttpRequest request) {
                logger.info("connect, request = " + request);
            }

            @Override
            public void version(String version) {
                logger.info("version = " + version);
            }

            @Override
            public void numberOfRecords(long numberOfRecords) {
                logger.info("numberOfRecords = " + numberOfRecords);
            }

            @Override
            public void beginRecord() {
                logger.info("startStream record");
            }

            @Override
            public void recordSchema(String recordSchema) {
                logger.info("got record schema:" + recordSchema);
            }

            @Override
            public void recordPacking(String recordPacking) {
                logger.info("got recordPacking: " + recordPacking);
            }

            @Override
            public void recordIdentifier(String recordIdentifier) {
                logger.info("got recordIdentifier=" + recordIdentifier);
            }

            @Override
            public void recordPosition(int recordPosition) {
                logger.info("got recordPosition=" + recordPosition);
            }

            @Override
            public XMLEventConsumer recordData() {
                // parse MarcXchange here
                return new SaxEventConsumer(handler);
            }

            @Override
            public XMLEventConsumer extraRecordData() {
                // ignore extra data
                return null;
            }

            @Override
            public void endRecord() {
            }

            @Override
            public void onDisconnect(HttpRequest request) {
                logger.info("disconnect, request = " + request);
            }
        };

        StringWriter w = new StringWriter();
        SRUClient client = new DefaultSRUClient();

        SearchRetrieveRequest request = client.newSearchRetrieveRequest(uri.toURL())
                .addListener(listener);
        client.searchRetrieve(request).to(w);

        logger.info("w={}", w);
    }

    class MyBibQueue extends MARCEntityQueue {

        public MyBibQueue(String path, int workers) throws Exception {
            super(path, workers);
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(
                    settings.get("bib-index", "zdb"),
                    settings.get("bib-type", "title"));
            params.setIdPredicate("identifierZDB");
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
        }
    }

    class MyHolQueue extends MARCEntityQueue {

        public MyHolQueue(String path, int workers) throws Exception {
            super(path, workers);
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(
                    settings.get("hol-index", "zdbholdings"),
                    settings.get("hol-type", "holdings"));
            params.setIdPredicate("identifierZDB");
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
        }
    }
}
