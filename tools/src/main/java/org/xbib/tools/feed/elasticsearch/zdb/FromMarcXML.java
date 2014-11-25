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

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.entities.marc.MARCEntityBuilderState;
import org.xbib.entities.marc.MARCEntityQueue;
import org.xbib.io.InputService;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.Feeder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Zeitschriftendatenbank (ZDB) MARC ISO2709 files
 */
public final class FromMarcXML extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(FromMarcXML.class.getName());

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final static Charset ISO88591 = Charset.forName("ISO-8859-1");

    @Override
    public String getName() {
        return "zdb-marc-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return FromMarcXML::new;
    }

    @Override
    protected Feeder beforeIndexCreation(Ingest ingest) throws IOException {
        ingest.mapping("title", FromMarcXML.class.getResourceAsStream("mapping-title.json"));
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        final MyQueue queue = new MyQueue(settings.get("elements"), settings.getAsInt("pipelines", 1));
                queue.setUnmappedKeyListener(key -> {
                    if ((settings.getAsBoolean("detect", false))) {
                        logger.warn("unmapped field {}", key);
                        unmapped.add("\"" + key + "\"");
                    }
                });
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(value -> Normalizer.normalize(new String(value.getBytes(ISO88591), UTF8), Normalizer.Form.NFKC))
                .addListener(queue);
        final Iso2709Reader reader = new Iso2709Reader()
                .setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MARC21");
        reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
        if ("marc/hol".equals(settings.get("elements")) || "marc/zdb/hol".equals(settings.get("elements"))) {
            reader.setProperty(Iso2709Reader.TYPE, "Holdings");
        }
        reader.setProperty(Iso2709Reader.FATAL_ERRORS, false);
        InputStreamReader r = new InputStreamReader(InputService.getInputStream(uri), ISO88591);
        reader.parse(r);
        r.close();
        queue.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown keys={}", unmapped);
        }
    }

    class MyQueue extends MARCEntityQueue {

        public MyQueue(String path, int workers) {
            super(path, workers);
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(IRINamespaceContext.getInstance(),
                    settings.get("index"), settings.get("type"));
            params.setIdPredicate("identifierZDB");
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.resource(state.getResource());
        }
    }

}