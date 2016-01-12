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
package org.xbib.tools.feed.elasticsearch.oai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.common.unit.TimeValue;
import org.xbib.elasticsearch.helper.client.LongAdderIngestMetric;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.oai.xml.SimpleMetadataHandler;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.ntriple.NTripleContentParams;
import org.xbib.time.chronic.Chronic;
import org.xbib.time.chronic.Span;
import org.xbib.tools.feed.elasticsearch.TimewindowFeeder;
import org.xbib.time.DateUtil;
import org.xbib.util.URIUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Harvest from OAI and feed to Elasticsearch
 */
public abstract class OAIFeeder extends TimewindowFeeder {

    private final static Logger logger = LogManager.getLogger(OAIFeeder.class);

    @Override
    protected void prepareSink() throws IOException {
        if (ingest == null) {
            ingest = createIngest();
            Integer maxbulkactions = settings.getAsInt("maxbulkactions", 1000);
            Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                    Runtime.getRuntime().availableProcessors());
            ingest.maxActionsPerRequest(maxbulkactions)
                    .maxConcurrentRequests(maxconcurrentbulkrequests);
            ingest.init(Settings.settingsBuilder()
                    .put("cluster.name", settings.get("elasticsearch.cluster"))
                    .put("host", settings.get("elasticsearch.host"))
                    .put("port", settings.getAsInt("elasticsearch.port", 9300))
                    .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                    .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                    .build().getAsMap(), new LongAdderIngestMetric());
        }
        super.prepareSink();
    }

    @Override
    protected void prepareSource() throws IOException {
        if (settings.containsSetting("oai")) {
            String fromStr = settings.get("oai.from");
            Span fromSpan = Chronic.parse(fromStr);
            String untilStr = settings.get("oai.until");
            Span untilSpan = Chronic.parse(untilStr);
            TimeValue delta = settings.getAsTime("oai.interval", TimeValue.timeValueHours(24));
            if (fromSpan != null) {
                logger.info("from={}", DateUtil.formatDateISO(fromSpan.getBeginCalendar().getTime()));
                if (untilSpan != null) {
                    logger.info("until={}", DateUtil.formatDateISO(untilSpan.getBeginCalendar().getTime()));
                    long millis = untilSpan.getBeginCalendar().getTime().getTime() - fromSpan.getBeginCalendar().getTime().getTime();
                    delta = settings.getAsTime("oai.interval",
                            TimeValue.parseTimeValue("" + millis + "ms", TimeValue.timeValueMillis(0L)));
                }
                logger.info("delta={}", delta);
                // now get base URI and replace it with concrete URIs
                int counter = settings.getAsInt("oai.counter", 1);
                logger.info("counter={}", counter);
                List<String> uris = new LinkedList<>();
                try {
                    for (int i = 0; i < counter; i++) {
                        URI uriBase = URI.create(settings.get("uri"));
                        uriBase = URIUtil.addParameter(uriBase, "from", DateUtil.formatDateISO(fromSpan.getBeginCalendar().getTime()));
                        fromSpan = fromSpan.add(-delta.seconds());
                        if (untilSpan != null) {
                            uriBase = URIUtil.addParameter(uriBase, "until", DateUtil.formatDateISO(untilSpan.getBeginCalendar().getTime()));
                            untilSpan = untilSpan.add(-delta.seconds());
                        }
                        String s = uriBase.toString();
                        if (!uris.contains(s)) {
                            uris.add(0, s);
                        }
                    }
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
                // update settings with new URI list for super.prepareSource()
                settings = Settings.settingsBuilder()
                        .put(settings)
                        .putArray("uri", uris)
                        .build();
            }
        }
        super.prepareSource();
    }

    @Override
    protected void process(URI uri) throws Exception {
        logger.info("processing URI {} for OAI", uri);
        Map<String, String> params = URIUtil.parseQueryString(uri);
        String server = uri.toString();
        String verb = params.get("verb");
        String metadataPrefix = params.get("metadataPrefix");
        String set = params.get("set");
        Date from = DateUtil.parseDateISO(params.get("from"));
        Date until = DateUtil.parseDateISO(params.get("until"));
        final OAIClient client = OAIClientFactory.newClient(server);
        client.setTimeout(settings.getAsInt("timeout", 60000));
        if (!verb.equals(OAIConstants.LIST_RECORDS)) {
            logger.warn("no verb {}, returning", OAIConstants.LIST_RECORDS);
            return;
        }
        ListRecordsRequest request = client.newListRecordsRequest()
                .setMetadataPrefix(metadataPrefix)
                .setSet(set)
                .setFrom(from, OAIDateResolution.DAY)
                .setUntil(until, OAIDateResolution.DAY);
        do {
            try {
                request.addHandler(newMetadataHandler());
                ListRecordsListener listener = new ListRecordsListener(request);
                logger.info("OAI request: {}", request.getURL());
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    logger.debug("got OAI response");
                    StringWriter w = new StringWriter();
                    listener.getResponse().to(w);
                    logger.debug("{}", w);
                    request = client.resume(request, listener.getResumptionToken());
                } else {
                    logger.debug("no valid OAI response");
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                request = null;
            }
        } while (request != null);
        client.close();
    }

    protected RdfResourceHandler rdfResourceHandler() {
        RdfContentParams params = NTripleContentParams.DEFAULT_PARAMS;
        return new RdfResourceHandler(params);
    }

    protected SimpleMetadataHandler newMetadataHandler() {
        return new OAISimpleMetadataHandler();
    }

    protected String map(String id, String content) throws IOException {
        return content;
    }

    public class OAISimpleMetadataHandler extends SimpleMetadataHandler {

        private final IRINamespaceContext namespaceContext;

        private RdfResourceHandler handler;

        public OAISimpleMetadataHandler() {
            namespaceContext = IRINamespaceContext.newInstance();
            namespaceContext.addNamespace("", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            namespaceContext.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        }

        @Override
        public void startDocument() throws SAXException {
            this.handler = rdfResourceHandler();
            handler.setDefaultNamespace("", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            handler.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            handler.endDocument();
            try {
                RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext,
                        getConcreteIndex(), getType());
                params.setHandler((content, p) -> {
                    content = map(getHeader().getIdentifier(), content);
                    if (settings.getAsBoolean("mock", false)) {
                        logger.info("{}", content);
                    } else {
                        ingest.index(p.getIndex(), p.getType(), getHeader().getIdentifier(), content);
                    }
                });
                RdfContentBuilder builder = routeRdfXContentBuilder(params);
                builder.receive(handler.getResource());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new SAXException(e);
            }
        }

        @Override
        public void startPrefixMapping(String string, String string1) throws SAXException {
            handler.startPrefixMapping(string, string1);
        }

        @Override
        public void endPrefixMapping(String string) throws SAXException {
            handler.endPrefixMapping(string);
        }

        @Override
        public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
            handler.startElement(ns, localname, string2, atrbts);
        }

        @Override
        public void endElement(String ns, String localname, String string2) throws SAXException {
            handler.endElement(ns, localname, string2);
        }

        @Override
        public void characters(char[] chars, int i, int i1) throws SAXException {
            handler.characters(chars, i, i1);
        }
    }

}
