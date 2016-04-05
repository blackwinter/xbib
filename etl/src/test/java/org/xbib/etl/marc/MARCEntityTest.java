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
package org.xbib.etl.marc;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.iri.IRI;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class MARCEntityTest extends Assert {


    @Test
    public void testSetup() throws Exception {
        File file = File.createTempFile("marc-bib-entities.", ".json");
        file.deleteOnExit();
        Writer writer = new FileWriter(file);
        MyQueue queue = new MyQueue();
        queue.getSpecification().dump(/*"org/xbib/analyzer/marc/bib.json",*/ writer);
        writer.close();
        queue.execute();
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(queue);
        Iso2709Reader reader = new Iso2709Reader(null)
                .setMarcXchangeListener(kv);
        queue.close();
    }

    @Test
    public void testStbBonn() throws Exception {
        InputStream in = getClass().getResourceAsStream("stb-bonn.mrc");
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        MyQueue queue = new MyQueue();
        queue.setUnmappedKeyListener((id, key) -> unmapped.add(key.toString()));
        queue.execute();
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(queue);
        Iso2709Reader reader = new Iso2709Reader(in, "UTF-8").setMarcXchangeListener(kv);
        reader.setFormat("MARC21");
        reader.setType("Bibliographic");
        reader.parse();
        queue.close();
        //logger.info("unmapped = {}", unmapped);
        //logger.info("count = {}", queue.getCounter());
        assertEquals(8676, queue.getCounter());
    }

    class MyQueue extends MARCEntityQueue {

        final AtomicInteger counter = new AtomicInteger();

        public MyQueue() throws Exception {
            super("org.xbib.analyzer.marc.bib", 1, "org/xbib/analyzer/marc/bib.json");
        }

        @Override
        public void beforeCompletion(MARCEntityBuilderState context) throws IOException {
            IRI iri = IRI.builder().scheme("http")
                    .host("dummy")
                    .query("dummy")
                    .fragment(Long.toString(counter.getAndIncrement())).build();
            context.getResource().setId(iri);
        }

        public long getCounter() {
            return counter.longValue();
        }

    }

}
