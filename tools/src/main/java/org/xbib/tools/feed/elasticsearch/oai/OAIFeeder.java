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
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.io.archive.tar.TarConnection;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.client.identify.IdentifyRequest;
import org.xbib.oai.client.identify.IdentifyResponseListener;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.oai.rdf.RdfSimpleMetadataHandler;
import org.xbib.oai.xml.SimpleMetadataHandler;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.oai.xml.XmlSimpleMetadataHandler;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.ntriple.NTripleContentParams;
import org.xbib.time.chronic.Chronic;
import org.xbib.time.chronic.Span;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.URIBuilder;
import org.xbib.util.concurrent.URIWorkerRequest;
import org.xbib.util.concurrent.WorkerProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.xbib.rdf.RdfContentFactory.ntripleBuilder;
import static org.xbib.rdf.RdfContentFactory.turtleBuilder;
import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Harvest from OAI and feed to Elasticsearch
 */
public class OAIFeeder extends Feeder {

    private final static Logger logger = LogManager.getLogger(OAIFeeder.class);

    private TarConnection connection;

    private Session<StringPacket> session;

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new OAIFeeder().setPipeline(p);
    }

    @Override
    protected void prepareInput() throws IOException, InterruptedException {
        Map<String,Settings> inputSettingsMap = settings.getGroups("input");
        Settings oaiSettings = inputSettingsMap.get("oai");
        if (oaiSettings != null) {
            String granularity = oaiSettings.containsSetting("granularity") ? oaiSettings.get("granularity") : null;
            String fromStr = oaiSettings.get("from");
            String untilStr = oaiSettings.get("until");
            Span fromSpan;
            Span untilSpan;
            try {
                fromSpan = Chronic.parse(fromStr);
                untilSpan = Chronic.parse(untilStr);
            } catch (ParseException e) {
                throw new IOException(e);
            }
            TimeValue delta = oaiSettings.getAsTime("interval", TimeValue.timeValueHours(24));
            if (fromSpan != null) {
                logger.info("from={}", DateTimeFormatter.ISO_INSTANT.format(fromSpan.getBeginCalendar()));
                if (untilSpan != null) {
                    logger.info("until={}", DateTimeFormatter.ISO_INSTANT.format(untilSpan.getBeginCalendar()));
                    long secs = ChronoUnit.SECONDS.between(fromSpan.getBeginCalendar(), untilSpan.getBeginCalendar());
                    delta = oaiSettings.getAsTime("interval",
                            TimeValue.parseTimeValue("" + secs + "s", TimeValue.timeValueMillis(0L)));
                }
                logger.info("delta={}", delta);
                // now get base URI and replace it with concrete URIs
                int counter = oaiSettings.getAsInt("counter", 1);
                logger.info("counter={}", counter);
                List<URIWorkerRequest> list = new LinkedList<>();
                for (int i = 0; i < counter; i++) {
                    URIBuilder builder = new URIBuilder(oaiSettings.get("base"))
                        .addParameter("granularity", granularity)
                        .addParameter("from", fromSpan.getBeginCalendar().toInstant().toString());
                    fromSpan = fromSpan.add(-delta.seconds());
                    if (untilSpan != null) {
                        builder.addParameter("until", untilSpan.getBeginCalendar().toInstant().toString());
                        untilSpan = untilSpan.add(-delta.seconds());
                    }
                    URIWorkerRequest uriWorkerRequest = new URIWorkerRequest();
                    uriWorkerRequest.set(builder.build());
                    // add to front
                    list.add(0, uriWorkerRequest);
                }
                for (URIWorkerRequest uriWorkerRequest : list) {
                    getPipeline().getQueue().put(uriWorkerRequest);
                }
            }
        }
        super.prepareInput();
    }

    @Override
    public void prepareOutput() throws IOException {
        super.prepareOutput();
        if (fileOutput.getMap().containsKey("tar")) {
            Path path = fileOutput.getMap().get("tar").getPath();
            connection = new TarConnection();
            connection.setPath(path, StandardOpenOption.CREATE);
            session = connection.createSession();
            session.open(Session.Mode.WRITE);
        }
    }

    @Override
    protected void process(URI uri) throws Exception {
        Map<String, String> params = URIBuilder.parseQueryString(uri);
        String server = uri.toString();
        String verb = params.get("verb");
        String metadataPrefix = params.get("metadataPrefix");
        String set = params.get("set");
        String granularity = params.get("granularity");
        Instant from = Instant.parse(params.get("from"));
        Instant until = Instant.parse(params.get("until"));
        final OAIClient client = OAIClientFactory.newClient(server);
        client.setTimeout(settings.getAsInt("timeout", 60000));

        if (granularity == null) {
            // fetch from Identify
            IdentifyRequest identifyRequest = client.newIdentifyRequest();
            IdentifyResponseListener identifyResponseListener = new IdentifyResponseListener(identifyRequest);
            identifyRequest.prepare().execute(identifyResponseListener).waitFor();
            granularity = identifyResponseListener.getResponse().getGranularity();
        }
        if (granularity == null) {
            granularity = "YYYY-MM-DD";
        }
        DateTimeFormatter dateTimeFormatter;
        switch (granularity) {
            case "yyyy-MM-dd'T'HH:mm:ss'Z'":
            case "yyyy-MM-ddTHH:mm:ssZ" :
                dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("GMT"));
                break;
            case "YYYY-MM-DD" :
                dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("GMT"));
                break;
            default:
                dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("GMT"));
                break;
        }

        if (!verb.equals(OAIConstants.LIST_RECORDS)) {
            logger.warn("no verb {}, returning", OAIConstants.LIST_RECORDS);
            return;
        }
        ListRecordsRequest request = client.newListRecordsRequest()
                .setDateTimeFormatter(dateTimeFormatter)
                .setMetadataPrefix(metadataPrefix)
                .setSet(set)
                .setFrom(from)
                .setUntil(until);
        do {
            try {
                request.addHandler(newMetadataHandler());
                ListRecordsListener listener = new ListRecordsListener(request);
                logger.info("OAI request: {}", request);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    logger.debug("got OAI response");
                    StringWriter w = new StringWriter();
                    listener.getResponse().to(w);
                    append(w.toString());
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

    protected void append(String s) {
        logger.debug("{}", s);
        fileOutput.getMap().entrySet().stream().forEach(entry -> {
            try {
                if (entry.getKey().startsWith("file")) {
                    entry.getValue().getOut().write(s.getBytes(StandardCharsets.UTF_8));
                } else if (entry.getKey().equals("tar") && session != null) {
                    session.write(new StringPacket().packet(s));
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    @Override
    protected void disposeOutput() throws IOException {
        if (session != null) {
            session.close();
        }
        if (connection != null) {
            connection.close();
        }
        super.disposeOutput();
    }

    protected SimpleMetadataHandler newMetadataHandler() throws IOException {
        if (settings.containsSetting("handler")) {
            switch (settings.get("handler")) {
                case "xml":
                    return xmlMetadataHandler();
                case "turtle":
                    return turtleMetadataHandler();
                case "ntriples":
                    return ntripleMetadataHandler();
            }
        }
        return new OAISimpleMetadataHandler();
    }

    protected SimpleMetadataHandler xmlMetadataHandler() {
        return new XmlPacketHandlerSimple().setWriter(new StringWriter());
    }

    protected SimpleMetadataHandler turtleMetadataHandler() throws IOException {
        final RdfSimpleMetadataHandler metadataHandler = new RdfSimpleMetadataHandler();
        final RdfResourceHandler resourceHandler = rdfResourceHandler();
        metadataHandler.setHandler(resourceHandler)
                .setBuilder(turtleBuilder());
        return metadataHandler;
    }

    protected SimpleMetadataHandler ntripleMetadataHandler() throws IOException {
        final RdfSimpleMetadataHandler metadataHandler = new RdfSimpleMetadataHandler();
        final RdfResourceHandler resourceHandler = rdfResourceHandler();
        metadataHandler.setHandler(resourceHandler)
                .setBuilder(ntripleBuilder());
        return metadataHandler;
    }

    protected RdfResourceHandler rdfResourceHandler() {
        RdfContentParams params = NTripleContentParams.DEFAULT_PARAMS;
        return new RdfResourceHandler(params);
    }

    protected String map(String id, String content) throws IOException {
        return content;
    }

    class XmlPacketHandlerSimple extends XmlSimpleMetadataHandler {

        public void endDocument() throws SAXException {
            super.endDocument();
            logger.debug("got XML document {}", getIdentifier());
            try {
                StringPacket p = session.newPacket();
                p.name(getIdentifier());
                String s = getWriter().toString();
                // for Unicode in non-canonical form, normalize it here
                s = Normalizer.normalize(s, Normalizer.Form.NFC);
                p.packet(s);
                session.write(p);
            } catch (IOException e) {
                throw new SAXException(e);
            }
            setWriter(new StringWriter());
        }
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
                        indexDefinitionMap.get("bib").getConcreteIndex(),
                        indexDefinitionMap.get("bib").getType());
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
