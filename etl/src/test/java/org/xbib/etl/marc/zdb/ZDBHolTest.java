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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.iri.IRI;
import org.xbib.util.KeyValueStreamAdapter;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class ZDBHolTest extends Assert {

    private static final Logger logger = LogManager.getLogger(ZDBHolTest.class.getName());

    private final static Charset ISO88591 = Charset.forName("ISO-8859-1"); // 8 bit

    private final static Charset UTF8 = Charset.forName("UTF-8");

    @Test
    public void testZDBElements() throws Exception {
        final InputStream in = getClass().getResource("zdblokutf8.mrc").openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, ISO88591));
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        MyQueue queue = new MyQueue();
        queue.setUnmappedKeyListener((id, key) -> {
            unmapped.add(key.toString());
            logger.warn("record {}: unknown key: {}", id, key);
        });
        queue.execute();
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(value -> Normalizer.normalize(
                        new String(value.getBytes(ISO88591), UTF8), Normalizer.Form.NFKC))
                .addListener(queue)
                .addListener(new KeyValueStreamAdapter<FieldList, String>() {
                    @Override
                    public KeyValueStreamAdapter<FieldList, String> keyValue(FieldList key, String value) {
                        logger.debug("startStream");
                        for (Field f : key) {
                            logger.debug("tag={} ind={} subf={} data={}",
                                    f.tag(), f.indicator(), f.subfieldId(), f.data());
                        }
                        logger.debug("endStream");
                        return this;
                    }

                });
        Iso2709Reader reader = new Iso2709Reader(br)
                .setMarcXchangeListener("Holdings", kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MARC");
        reader.setProperty(Iso2709Reader.TYPE, "Holdings");
        reader.parse();
        queue.close();
        logger.info("zdb holdings counter = {}", queue.getCounter());
        logger.info("zdb holdings unknown keys = {}", unmapped);
        br.close();
        assertEquals(queue.getCounter(), 293);
    }

    class MyQueue extends MARCEntityQueue {

        final AtomicInteger counter = new AtomicInteger();

        public MyQueue() {
            super("org.xbib.analyzer.marc.zdb.hol", 1,
                    "org/xbib/analyzer/marc/zdb/hol.json");
        }

        @Override
        public void beforeCompletion(MARCEntityBuilderState state) throws IOException {
            IRI iri = IRI.builder().scheme("http")
                    .host("zdb")
                    .query("holdings")
                    .fragment(Long.toString(counter.getAndIncrement())).build();
            state.getResource().id(iri);
        }

        public long getCounter() {
            return counter.longValue();
        }
    }

}
