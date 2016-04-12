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
package org.xbib.tools.feed.elasticsearch.marc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.etl.marc.MARCDirectQueue;
import org.xbib.tools.convert.Converter;
import org.xbib.marc.MarcXchangeStream;
import org.xbib.marc.xml.sax.MarcXchangeReader;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Indexing MARCXML files (B3KAT)
 */
public final class MarcXML extends Feeder {

    private final static Logger logger = LogManager.getLogger(MarcXML.class.getName());

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new MarcXML().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            InputStreamReader r = new InputStreamReader(in, StandardCharsets.ISO_8859_1);
            final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<>());
            final MARCEntityQueue queue = settings.getAsBoolean("direct", false) ?
                    new MyDirectQueue(settings.get("elements"), settings.getAsInt("pipelines", 1)) :
                    new MyEntityQueue(settings.get("elements"), settings.getAsInt("pipelines", 1));
            queue.setUnmappedKeyListener((id, key) -> {
                if ((settings.getAsBoolean("detect", false))) {
                    logger.warn("unmapped field {}", key);
                    unmapped.add("\"" + key + "\"");
                }
            });
            queue.execute();
            final MarcXchangeStream marcXchangeStream = new MarcXchangeStream()
                    .setStringTransformer(value ->
                            Normalizer.normalize(new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8),
                                    Normalizer.Form.NFKC))
                    .add(queue);
            MarcXchangeReader reader = new MarcXchangeReader(r)
                    .setMarcXchangeListener(marcXchangeStream);
            reader.parse();
            r.close();
            queue.close();
            if (settings.getAsBoolean("detect-unknown", false)) {
                logger.info("unknown keys={}", unmapped);
            }
        }
    }

    class MyEntityQueue extends MARCEntityQueue {

        public MyEntityQueue(String path, int workers) throws Exception {
            super(path, workers);
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(indexDefinitionMap.get("bib").getConcreteIndex(),
                    indexDefinitionMap.get("bib").getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
        }
    }

    class MyDirectQueue extends MARCDirectQueue {

        public MyDirectQueue(String path, int workers) throws Exception {
            super(path, workers);
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(indexDefinitionMap.get("bib").getConcreteIndex(),
                    indexDefinitionMap.get("bib").getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
        }
    }
}
