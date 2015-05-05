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
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class MABDisketteTest extends StreamTester {

    @Test
    public void testMABDiskette() throws IOException, SAXException {
        InputStream in = getClass().getResource("mgl.txt").openStream();
        File file = File.createTempFile("mgl.", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
            read(new InputStreamReader(in, "cp850"), w);
        }
        assertStream(getClass().getResource("mgl.txt.xml").openStream(),
                new FileInputStream(file));
    }

    private void read(Reader reader, Writer writer) throws IOException, SAXException {
        MABDisketteReader mabDisketteReader = new MABDisketteReader(reader);
        mabDisketteReader.setFormat("MABDiskette");
        MarcXchangeWriter marcXchangeWriter = new MarcXchangeWriter(writer);
        mabDisketteReader.setMarcXchangeListener(marcXchangeWriter);
        marcXchangeWriter.startDocument();
        marcXchangeWriter.beginCollection();
        mabDisketteReader.parse();
        marcXchangeWriter.endCollection();
        marcXchangeWriter.endDocument();
    }
}
