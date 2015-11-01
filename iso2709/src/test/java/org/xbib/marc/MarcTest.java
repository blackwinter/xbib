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
package org.xbib.marc;

import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.xml.stream.MarcXchangeWriter;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.testng.Assert.assertNull;

public class MarcTest extends StreamTester {

    @Test
    public void testProperMarc() throws IOException, SAXException {
        for (String s : new String[]{
                // TODO why do they fail?
//                "brkrtest.mrc",
//                "makrtest.mrc",
                "chabon-loc.mrc",
                "chabon.mrc",
                "diacritic4.mrc",
                "summerland.mrc"
                }) {
            InputStream in = getClass().getResource(s).openStream();
            File file = File.createTempFile(s + ".", ".xml");
            FileOutputStream out = new FileOutputStream(file);
            try (InputStreamReader r = new InputStreamReader(in, "ANSEL")) {
                Iso2709Reader reader = new Iso2709Reader(r);
                reader.setFormat(MarcXchangeConstants.MARC21);
                MarcXchangeWriter writer = new MarcXchangeWriter(out);
                reader.setMarcXchangeListener(writer);
                writer.startDocument();
                writer.beginCollection();
                reader.parse();
                writer.endCollection();
                writer.endDocument();
                assertNull(writer.getException());
                in.close();
                out.close();
                // Expecting same data at offset 24022 expected:<10> but was:<32>
                assertStream(s, getClass().getResource(s + ".xml").openStream(),
                        new FileInputStream(file));
            }
        }
    }

    @Test
    public void testFaultyMarc() throws IOException, SAXException {
        for (String s : new String[]{
                     "error.mrc"
                }) {
            InputStream in = getClass().getResource(s).openStream();
            File file = File.createTempFile(s +".", ".xml");
            FileOutputStream out = new FileOutputStream(file);
            try (InputStreamReader r = new InputStreamReader(in, "ANSEL")) {
                Iso2709Reader reader = new Iso2709Reader(r);
                reader.setFormat(MarcXchangeConstants.MARC21);
                MarcXchangeWriter writer = new MarcXchangeWriter(out);
                reader.setMarcXchangeListener(writer);
                writer.startDocument();
                writer.beginCollection();
                reader.parse();
                writer.endCollection();
                writer.endDocument();
                assertNull(writer.getException());
            }
            in.close();
            out.close();
            assertStream(s, getClass().getResource(s + ".xml").openStream(),
                    new FileInputStream(file));
        }
    }

    /**
     * Here we have a true treasure, old ANSEL US-MARC.
     *
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void testAMS() throws IOException, SAXException {
        String s = "amstransactions.mrc";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile(s +".", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        try (InputStreamReader r = new InputStreamReader(in, "ANSEL")) {
            Iso2709Reader reader = new Iso2709Reader(r);
            reader.setFormat(MarcXchangeConstants.MARC21);
            MarcXchangeWriter writer = new MarcXchangeWriter(out);
            reader.setMarcXchangeListener(writer);
            writer.startDocument();
            writer.beginCollection();
            reader.parse();
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
        }
        in.close();
        out.close();
        assertStream(s, getClass().getResource(s + ".xml").openStream(),
                new FileInputStream(file));
    }
    
    @Test
    public void testZDBLok() throws IOException, SAXException {
        String s = "zdblokutf8.mrc";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile(s +".", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        try (InputStreamReader r = new InputStreamReader(in, "UTF-8")) {
            Iso2709Reader reader = new Iso2709Reader(r);
            reader.setFormat(MarcXchangeConstants.MARC21);
            MarcXchangeWriter writer = new MarcXchangeWriter(out);
            reader.setMarcXchangeListener(writer);
            writer.startDocument();
            writer.beginCollection();
            reader.parse();
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
        }
        in.close();
        out.close();
        assertStream(s, getClass().getResource(s + ".xml").openStream(),
                new FileInputStream(file));
    }
}
