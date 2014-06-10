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
package org.xbib.tools.convert.zdb;

import org.xbib.io.Connection;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.io.archivers.tar.TarConnectionFactory;
import org.xbib.io.archivers.tar.TarSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.listrecords.ListRecordsListener;
import org.xbib.oai.listrecords.ListRecordsRequest;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.tools.Converter;
import org.xbib.util.DateUtil;
import org.xbib.util.URIUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fetch OAI result from ZDB OAI service.
 * Output is archived as strings in a single TAR archive.
 */
public class FromOAI extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(FromOAI.class.getName());

    private static Session<StringPacket> session;

    private final static AtomicLong counter = new AtomicLong();

    private OAIClient client;

    public FromOAI() {
    }

    public FromOAI(boolean b) {
        client = OAIClientFactory.newClient();
    }

    @Override
    protected FromOAI prepare() throws IOException {
        // open output TAR archive
        TarConnectionFactory factory = new TarConnectionFactory();
        Connection<TarSession> connection = factory.getConnection(URI.create(settings.get("output")));
        session = connection.createSession();
        if (session == null) {
            throw new IOException("can not open " + settings.get("output") + " for output");
        }
        session.open(Session.Mode.WRITE);

        // create input URLs
        input = new ConcurrentLinkedQueue<>();
        for (String uri : settings.getAsArray("input")) {
            input.add(URI.create(uri));
        }
        logger.info("uris = {}", input.size());
        return this;
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromOAI(true);
            }
        };
    }

    @Override
    protected void process(URI uri) throws Exception {
        Map<String, String> params = URIUtil.parseQueryString(uri);
        final OAIClient client = OAIClientFactory.newClient().setURL(uri);
        client.setTimeout(settings.getAsInt("timeout", 60000));
        ListRecordsRequest request = client.newListRecordsRequest()
                .setMetadataPrefix(params.get("metadataPrefix"))
                .setSet(params.get("set"))
                .setFrom(DateUtil.parseDateISO(params.get("from")), OAIDateResolution.DAY)
                .setUntil(DateUtil.parseDateISO(params.get("until")), OAIDateResolution.DAY);
        try {
            do {
                ListRecordsListener listener = new ListRecordsListener(request);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    StringWriter writer = new StringWriter();
                    listener.getResponse().to(writer);
                    StringPacket packet = new StringPacket();
                    packet.name(Long.toString(counter.incrementAndGet()));
                    packet.packet(writer.toString());
                    session.write(packet);
                    request = client.resume(request, listener.getResumptionToken());
                }
            } while (request != null);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        client.close();
    }

    public void run() throws Exception {
        super.run();
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected FromOAI cleanup() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

}
