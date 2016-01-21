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
package org.xbib.tools.feed.elasticsearch.zdb.bibdat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.marc.dialects.pica.PicaEntityBuilderState;
import org.xbib.etl.marc.dialects.pica.PicaEntityQueue;
import org.xbib.tools.convert.Converter;
import org.xbib.util.InputService;
import org.xbib.util.KeyValueStreamAdapter;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.dialects.pica.DNBPicaXmlReader;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Index bib addresses into Elasticsearch
 */
public final class BibdatFromPPXML extends Feeder {

    private final static Logger logger = LogManager.getLogger(BibdatFromPPXML.class.getSimpleName());

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new BibdatFromPPXML().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = InputService.getInputStream(uri)) {
            final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<>());
            MyQueue queue = new MyQueue("pica/zdb/bib", settings.getAsInt("pipelines", 1));
            queue.setUnmappedKeyListener((id, key) -> {
                if ((settings.getAsBoolean("detect", false))) {
                    logger.warn("unmapped field {}", key);
                    unmapped.add("\"" + key + "\"");
                }
            });
            queue.execute();
            MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    .setStringTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFKC))
                    .addListener(queue)
                    .addListener(new KeyValueStreamAdapter<FieldList, String>() {
                        @Override
                        public KeyValueStreamAdapter<FieldList, String> begin() {
                            if (logger.isTraceEnabled()) {
                                logger.trace("start object");
                            }
                            return this;
                        }

                        @Override
                        public KeyValueStreamAdapter<FieldList, String> keyValue(FieldList key, String value) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("start field");
                                for (Field f : key) {
                                    logger.trace("tag={} ind={} subf={} data={}",
                                            f.tag(), f.indicator(), f.subfieldId(), f.data());
                                }
                                logger.trace("end field");
                            }
                            return this;
                        }

                        @Override
                        public KeyValueStreamAdapter<FieldList, String> end() {
                            if (logger.isTraceEnabled()) {
                                logger.trace("end object");
                            }
                            return this;
                        }

                    });
            DNBPicaXmlReader reader = new DNBPicaXmlReader(new InputStreamReader(in, "UTF-8"));
            reader.setMarcXchangeListener(kv);
            reader.parse();
            queue.close();
            if (settings.getAsBoolean("detect", false)) {
                logger.info("detected unmapped elements = {}", unmapped);
            }
        }
    }

    class MyQueue extends PicaEntityQueue {

        public MyQueue(String path, int workers) {
            super(path, workers);
        }

        @Override
        public void afterCompletion(PicaEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(indexDefinitionMap.get("bib").getConcreteIndex(),
                    indexDefinitionMap.get("bib").getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordNumber(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
        }
    }

}
