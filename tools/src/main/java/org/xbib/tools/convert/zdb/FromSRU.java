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
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.client.SRUClientFactory;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.tools.Converter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fetch SRU result from ZDB SRU service.
 * Output is archived as strings in a single TAR archive.
 */
public class FromSRU extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(FromSRU.class.getName());

    private static Session<StringPacket> session;

    private final static AtomicLong counter = new AtomicLong();

    private SRUClient client;

    public FromSRU() {}

    public FromSRU(boolean b) {
        client = SRUClientFactory.newClient();
    }

    @Override
    protected FromSRU prepare() throws IOException {

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
        if (settings.get("numbers") != null) {
            FileInputStream in = new FileInputStream(settings.get("numbers"));
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = r.readLine()) != null) {
                input.add(URI.create(String.format(settings.get("uri"), line)));
            }
            in.close();
        } else {
            input.add(URI.create(settings.get("uri")));
        }
        logger.info("uris = {}", input.size());
        return this;
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromSRU(true);
            }
        };
    }

    @Override
    protected void process(URI uri) throws Exception {
        StringWriter w = new StringWriter();
        SearchRetrieveRequest request = client.newSearchRetrieveRequest()
                .setURI(uri);
        SearchRetrieveResponse response = client.searchRetrieve(request).to(w);
        if (response.httpStatus() == 200 && w.toString().length() > 0) {
            StringPacket packet = new StringPacket();
            packet.name(Long.toString(counter.incrementAndGet()));
            packet.packet(w.toString());
            session.write(packet);
        }
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

    protected FromSRU cleanup() {
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
