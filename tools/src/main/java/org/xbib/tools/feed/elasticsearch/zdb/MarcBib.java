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

import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.marc.BibliographicFeeder;
import org.xbib.util.concurrent.WorkerProvider;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/**
 * Index Zeitschriftendatenbank (ZDB) MARC Bibliographic ISO2709 files
 */
public class MarcBib extends BibliographicFeeder {

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new MarcBib().setPipeline(p);
    }

    @Override
    protected void process(InputStream in, MARCEntityQueue queue) throws IOException {
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(value ->
                        Normalizer.normalize(new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8),
                                Normalizer.Form.NFKC))
                .addListener(queue);
        InputStreamReader r = new InputStreamReader(in, StandardCharsets.ISO_8859_1);
        try {
            final Iso2709Reader reader = new Iso2709Reader(r)
                    .setMarcXchangeListener(kv);
            reader.setProperty(Iso2709Reader.FORMAT, "MARC21");
            reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
            reader.setProperty(Iso2709Reader.FATAL_ERRORS, false);
            reader.parse();
            r.close();
        } catch (SAXNotSupportedException | SAXNotRecognizedException e) {
            throw new IOException(e);
        }
    }

}
