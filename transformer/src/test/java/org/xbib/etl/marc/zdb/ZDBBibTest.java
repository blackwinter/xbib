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
package org.xbib.etl.marc.zdb;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.iri.IRI;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchangeStream;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.marc.xml.sax.MarcXchangeReader;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RdfXContentParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;

public class ZDBBibTest extends Assert {

    @Test
    public void testZDBBib() throws Exception {
        InputStream in = getClass().getResourceAsStream("zdbtitutf8.mrc");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        MyQueue queue = new MyQueue();
                queue.setUnmappedKeyListener((id, key) -> unmapped.add(key.toString()));
        queue.execute();
        MarcXchangeStream kv = new MarcXchangeStream()
                .setStringTransformer(value ->
                        Normalizer.normalize(new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8),
                                Normalizer.Form.NFKC))
                .add(queue);
        Iso2709Reader reader = new Iso2709Reader(br).setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MARC");
        reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
        reader.parse();
        br.close();
        queue.close();
        //logger.info("unknown keys = {}", unmapped);
        //logger.info("counter = {}", queue.getCounter());
        assertEquals(queue.getCounter(), 8);
    }

    @Test
    public void testZDBOAI() throws Exception {
        InputStream in = getClass().getResourceAsStream("zdb-oai-marc.xml");
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        MyQueue queue = new MyQueue();
        queue.setUnmappedKeyListener((id, key) -> unmapped.add(key.toString()));
        queue.execute();
        MarcXchangeStream kv = new MarcXchangeStream()
                .setStringTransformer(new OurTransformer())
                .add(queue);
        MarcXchangeReader reader = new MarcXchangeReader(in).setMarcXchangeListener(kv);
        reader.parse();
        in.close();
        queue.close();
        //logger.info("unknown keys = {}", unmapped);
        //logger.info("counter = {}", queue.getCounter());
        assertEquals(queue.getCounter(), 50);
    }

    class MyQueue extends MARCEntityQueue {

        final AtomicInteger counter = new AtomicInteger();

        public MyQueue() throws Exception {
            super("org.xbib.analyzer.marc.zdb.bib",
                    Runtime.getRuntime().availableProcessors(),
                    MyQueue.class.getClassLoader().getResource("org/xbib/analyzer/marc/zdb/bib.json"));
        }

        @Override
        public void beforeCompletion(MARCEntityBuilderState state) throws IOException {
            IRI iri = IRI.builder().scheme("http")
                    .host("zdb")
                    .query("title")
                    .fragment(Long.toString(counter.getAndIncrement())).build();
            state.getResource().setId(iri);

            RdfXContentParams params = new RdfXContentParams();
            RdfContentBuilder builder = rdfXContentBuilder(params);
            builder.receive(state.getResource());
            String result = params.getGenerator().get();
            //logger.info(result);
        }

        public long getCounter() {
            return counter.longValue();
        }

    }

    class OurTransformer implements StringTransformer {
        @Override
        public String transform(String value) {
            return Normalizer.normalize(value, Normalizer.Form.NFKC);
        }
    }

}
