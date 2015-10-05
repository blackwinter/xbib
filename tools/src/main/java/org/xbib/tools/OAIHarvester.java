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
package org.xbib.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.Connection;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.io.archive.tar2.TarConnectionFactory;
import org.xbib.io.archive.tar2.TarSession;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.rdf.RdfSimpleMetadataHandler;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.oai.xml.SimpleMetadataHandler;
import org.xbib.oai.xml.XmlSimpleMetadataHandler;
import org.xbib.pipeline.URIPipelineRequest;
import org.xbib.rdf.RdfContentParams;
import org.xbib.util.DateUtil;
import org.xbib.util.URIUtil;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.text.Normalizer;
import java.util.Date;
import java.util.Map;

import static org.xbib.rdf.RdfContentFactory.ntripleBuilder;
import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

public abstract class OAIHarvester extends Converter {

    private final static Logger logger = LogManager.getLogger(OAIHarvester.class.getSimpleName());

    protected static Session<StringPacket> session;

    @Override
    public void prepareSink() throws IOException {
        try {
            String[] inputs = settings.getAsArray("input");

            if (inputs == null) {
                throw new IllegalArgumentException("no input given");
            }
            for (String uri : inputs) {
                URIPipelineRequest element = new URIPipelineRequest();
                element.set(URI.create(uri));
                getQueue().put(element);
            }
            String output = settings.get("output");
            TarConnectionFactory factory = new TarConnectionFactory();
            Connection<TarSession> connection = factory.getConnection(URI.create(output));
            session = connection.createSession();
            session.open(Session.Mode.WRITE);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void process(URI uri) throws Exception {
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

    @Override
    public OAIHarvester cleanup() throws IOException {
        super.cleanup();
        session.close();
        return this;
    }

    protected SimpleMetadataHandler newMetadataHandler() throws IOException {
        switch (settings.get("handler", "xml")) {
            case "xml" : return xmlMetadataHandler();
            case "turtle" : return turtleMetadataHandler();
            case "ntriples" : return ntripleMetadataHandler();
        }
        return xmlMetadataHandler();
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
        RdfContentParams params = RdfContentParams.EMPTY;
        return new RdfResourceHandler(params);
    }

    protected class XmlPacketHandlerSimple extends XmlSimpleMetadataHandler {

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

}
