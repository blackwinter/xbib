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
package org.xbib.tools.feed.elasticsearch.zdb.bib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.entities.marc.MARCEntityBuilderState;
import org.xbib.entities.marc.MARCEntityQueue;
import org.xbib.io.Request;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.element.URIPipelineElement;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.client.SRUClientFactory;
import org.xbib.sru.searchretrieve.SearchRetrieveListener;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.sru.searchretrieve.SearchRetrieveResponseAdapter;
import org.xbib.tools.Feeder;
import org.xbib.xml.stream.SaxEventConsumer;

import javax.xml.stream.util.XMLEventConsumer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.common.settings.Settings.settingsBuilder;
import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public class MarcSRU extends Feeder {

    private final static Logger logger = LogManager.getLogger(MarcSRU.class.getName());

    private SRUClient client;

    MarcSRU() {
        super();
    }

    MarcSRU(boolean b) {
        client = SRUClientFactory.newClient();
    }

    @Override
    public String getName() {
        return "zdb-sru-elasticsearch";
    }

    @Override
    public void prepareSource() throws IOException {
        try {
            prepareInput();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        prepareOutput();
    }

    protected void prepareInput() throws IOException, InterruptedException {
        // define input: fetch from SRU by number file, each line is an ID
        if (settings.get("numbers") != null) {
            FileInputStream in = new FileInputStream(settings.get("numbers"));
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = r.readLine()) != null) {
                URIPipelineElement element = new URIPipelineElement();
                element.set(URI.create(String.format(settings.get("uri"), line)));
                queue.put(element);
            }
            in.close();
        } else {
            URIPipelineElement element = new URIPipelineElement();
            element.set(URI.create(settings.get("uri")));
            queue.put(element);
        }
        logger.info("uris = {}", queue.size());
    }

    protected void prepareOutput() throws IOException {
        String index = settings.get("index");
        Integer shards = settings.getAsInt("shards", 1);
        Integer replica = settings.getAsInt("replica", 0);
        Integer maxbulkactions = settings.getAsInt("maxbulkactions", 100);
        Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                Runtime.getRuntime().availableProcessors());
        ingest = createIngest();
        beforeIndexCreation(ingest);
        ingest.maxActionsPerRequest(maxbulkactions)
                .maxConcurrentRequests(maxconcurrentbulkrequests)
                .init(settingsBuilder()
                        .put("cluster.name", settings.get("elasticsearch.cluster"))
                        .put("host", settings.get("elasticsearch.host"))
                        .put("port", settings.getAsInt("elasticsearch.port", 9300))
                        .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                        .build().getAsMap());
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        ingest.newIndex(index);
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return () -> new MarcSRU(true);
    }

    @Override
    public void process(URI uri) throws Exception {
        final Set<String> unmappedbib = Collections.synchronizedSet(new TreeSet<String>());
        final MyBibQueue bibqueue = new MyBibQueue("marc/zdb/bib", settings.getAsInt("pipelines", 1));
        bibqueue.setUnmappedKeyListener((id,key) -> {
            if ((settings.getAsBoolean("detect", false))) {
                logger.warn("unmapped field {}", key);
                unmappedbib.add("\"" + key + "\"");
            }
        });

        final Set<String> unmappedhol = Collections.synchronizedSet(new TreeSet<String>());
        final MyHolQueue holqueue = new MyHolQueue("marc/zdb/hol", settings.getAsInt("pipelines", 1));
        holqueue.setUnmappedKeyListener((id,key) -> {
            if ((settings.getAsBoolean("detect", false))) {
                logger.warn("unmapped field {}", key);
                unmappedhol.add("\"" + key + "\"");
            }
        });


        final MarcXchange2KeyValue bib = new MarcXchange2KeyValue()
                .setStringTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC))
                .addListener(bibqueue);

        final MarcXchange2KeyValue hol = new MarcXchange2KeyValue()
                .setStringTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC))
                .addListener(holqueue);

        final MarcXchangeContentHandler handler = new MarcXchangeContentHandler()
                .addListener("Bibliographic", bib)
                .addListener("Holdings", hol);

        final SearchRetrieveListener listener = new SearchRetrieveResponseAdapter() {

            @Override
            public void onConnect(Request request) {
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
            public void onDisconnect(Request request) {
                logger.info("disconnect, request = " + request);
            }
        };

        StringWriter w = new StringWriter();
        SearchRetrieveRequest request = client.newSearchRetrieveRequest()
                .setURI(uri)
                .addListener(listener);
        SearchRetrieveResponse response = client.searchRetrieve(request).to(w);

    }

    @Override
    public MarcSRU cleanup() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    class MyBibQueue extends MARCEntityQueue {

        public MyBibQueue(String path, int workers) {
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

        public MyHolQueue(String path, int workers) {
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
