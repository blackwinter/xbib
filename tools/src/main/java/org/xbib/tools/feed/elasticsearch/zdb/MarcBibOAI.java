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
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.xml.MarcXchangeReader;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.util.RecordHeader;
import org.xbib.oai.xml.MetadataHandler;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.oai.OAIFeeder;
import org.xbib.util.URIUtil;
import org.xbib.util.concurrent.WorkerProvider;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Fetch OAI result from OAI service and ingest into ES.
 */
public class MarcBibOAI extends OAIFeeder {

    private final static Logger logger = LogManager.getLogger(MarcBibOAI.class);

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new MarcBibOAI().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        // set prefix (ISIL)
        Map<String,Object> params = new HashMap<>();
        if (settings.containsSetting("catalogid")) {
            params.put("catalogid", settings.get("catalogid"));
            params.put("_prefix", "(" + settings.get("catalogid") + ")");
        }
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<>());
        final MARCEntityQueue queue = createQueue(params);
        queue.setUnmappedKeyListener((id,key) -> {
            if ((settings.getAsBoolean("detect-unknown", false))) {
                logger.warn("record {} unmapped field {}", id, key);
                unmapped.add("\"" + key + "\"");
            }
        });
        queue.execute();
        Map<String, String> oaiparams = URIUtil.parseQueryString(uri);
        String server = uri.toString();
        String verb = oaiparams.get("verb");
        String metadataPrefix = oaiparams.get("metadataPrefix");
        String set = oaiparams.get("set");
        Date from = Date.from(Instant.parse(oaiparams.get("from")));
        Date until = Date.from(Instant.parse(oaiparams.get("until")));
        // compute interval
        long interval = ChronoUnit.DAYS.between(from.toInstant(), until.toInstant());
        long count = settings.getAsLong("count", 1L);
        if (!verb.equals(OAIConstants.LIST_RECORDS)) {
            logger.error("only verb {} is valid, not {}", OAIConstants.LIST_RECORDS);
            return;
        }
        do {
            final OAIClient client = OAIClientFactory.newClient(server);
            client.setTimeout(settings.getAsInt("timeout", 60000));
            if (settings.get("proxyhost") != null) {
                client.setProxy(settings.get("proxyhost"), settings.getAsInt("proxyport", 3128));
            }
            ListRecordsRequest request = client.newListRecordsRequest()
                    .setMetadataPrefix(metadataPrefix)
                    .setSet(set)
                    .setFrom(from, OAIDateResolution.DAY)
                    .setUntil(until, OAIDateResolution.DAY);
            do {
                try {
                    final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                            .addListener(queue);
                    MarcMetadataHandler handler = new MarcMetadataHandler(kv);
                    request.addHandler(handler);
                    ListRecordsListener listener = new ListRecordsListener(request);
                    request.prepare().execute(listener).waitFor();
                    if (listener.getResponse() != null) {
                        StringWriter w = new StringWriter();
                        listener.getResponse().to(w);
                        request = client.resume(request, listener.getResumptionToken());
                    } else {
                        logger.debug("invalid OAI response");
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    request = null;
                }
            } while (request != null);
            client.close();
            // switch to next request
            LocalDateTime ldt = LocalDateTime.ofInstant(from.toInstant(), ZoneOffset.UTC).plusDays(-interval);
            from = Date.from(ldt.toInstant(ZoneOffset.UTC));
            ldt = LocalDateTime.ofInstant(until.toInstant(), ZoneOffset.UTC).plusDays(-interval);
            until = Date.from(ldt.toInstant(ZoneOffset.UTC));
        } while (count-- > 0L);
        queue.close();
        if (settings.getAsBoolean("detect-unknown", false)) {
            logger.info("unknown keys = {}", unmapped);
        }
    }

    protected MARCEntityQueue createQueue(Map<String,Object> params) {
        return new MyQueue(params);
    }

    class MyQueue extends MARCEntityQueue {

        public MyQueue(Map<String,Object> params) {
            super(settings.get("package", "org.xbib.analyzer.marc.bib"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements", "/org/xbib/analyzer/marc/bib.json")
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            // write bib resource
            RouteRdfXContentParams params = new RouteRdfXContentParams(indexDefinitionMap.get("bib").getConcreteIndex(),
                    indexDefinitionMap.get("bib").getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordNumber(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            getMetric().mark();
            if (settings.getAsBoolean("mock", false)) {
                logger.info("{}", builder.string());
            }
        }
    }

    static class MarcMetadataHandler implements MetadataHandler {

        private final MarcXchangeReader reader;

        RecordHeader header;

        MarcMetadataHandler(MarcXchange2KeyValue kv) {
            this.reader = new MarcXchangeReader((Reader)null);
            reader.addListener("Bibliographic", kv);
        }

        public MarcXchangeReader getReader() {
            return reader;
        }

        @Override
        public MetadataHandler setHeader(RecordHeader header) {
            this.header = header;
            return this;
        }

        @Override
        public RecordHeader getHeader() {
            return header;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            reader.setDocumentLocator(locator);
        }

        @Override
        public void startDocument() throws SAXException {
            reader.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            reader.endDocument();
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            reader.startPrefixMapping(prefix, uri);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            reader.endPrefixMapping(prefix);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            reader.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            reader.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            reader.characters(ch, start, length);
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            reader.ignorableWhitespace(ch, start, length);
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            reader.processingInstruction(target, data);
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
            reader.skippedEntity(name);
        }
    }
}
