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
package org.xbib.marc.dialects.mab;

import org.junit.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.xml.sax.MarcXchangeReader;
import org.xbib.marc.xml.stream.MarcXchangeWriter;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class MABTest extends StreamTester {


    @Test
    public void testZDBMAB() throws IOException, SAXException {
        String s = "1217zdbtit.dat";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile("zdb.", ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
            read(new InputStreamReader(in, "x-MAB"), w);
        }
        in.close();
        assertStream(s, getClass().getResource("1217zdbtit.dat-out.xml").openStream(),
                new FileInputStream(file));
    }

    @Test
    public void testCreateMABXML() throws IOException, SAXException {
        String s ="test.groupstream";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile("test.groupstream.", ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
            read(new InputStreamReader(in, "x-MAB"), w);
        }
        in.close();
        assertStream(s, getClass().getResource("test.groupstream-out.xml").openStream(),
                new FileInputStream(file));
    }

    @Test
    public void testReplacement() throws IOException, SAXException {
        String s = "DE-605-aleph500-publish.xml";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile("DE-605-aleph500-publish-out.", ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        MarcXchangeReader reader = new MarcXchangeReader(in)
                .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
        reader.setTransformer("088$  $a", value -> "Das ist ein Test").setTransform(true);
        MarcXchangeWriter writer = new MarcXchangeWriter(out);
        reader.setMarcXchangeListener(writer);
        writer.startDocument();
        writer.beginCollection();
        reader.parse();
        writer.endCollection();
        writer.endDocument();
        out.close();
        in.close();
        assertStream(s, getClass().getResource("DE-605-aleph500-publish-out.xml").openStream(),
                new FileInputStream(file));
    }

    private void read(Reader reader, Writer writer) throws IOException, SAXException {
        Iso2709Reader iso2709Reader = new Iso2709Reader(reader);
        iso2709Reader.setFormat("MAB");
        iso2709Reader.setType("Titel");
        MarcXchangeWriter marcXchangeWriter = new MarcXchangeWriter(writer);
        iso2709Reader.setMarcXchangeListener(marcXchangeWriter);
        marcXchangeWriter.startDocument();
        marcXchangeWriter.beginCollection();
        iso2709Reader.parse();
        marcXchangeWriter.endCollection();
        marcXchangeWriter.endDocument();
    }
}
