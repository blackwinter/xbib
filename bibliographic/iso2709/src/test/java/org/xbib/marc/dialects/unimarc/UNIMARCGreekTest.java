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
package org.xbib.marc.dialects.unimarc;

import org.testng.annotations.Test;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.xml.stream.MarcXchangeWriter;
import org.xbib.xml.XMLUtil;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static org.testng.Assert.assertNull;

public class UNIMARCGreekTest {

    private final static Charset ISO88591 = Charset.forName("ISO-8859-1"); // 8 bit

    private final static Charset UTF8 = Charset.forName("UTF-8");

    @Test
    public void testUNIMARC() throws IOException, SAXException {
        InputStream in = getClass().getResourceAsStream("serres.mrc");
        try (InputStreamReader r = new InputStreamReader(in, ISO88591)) {
            final Iso2709Reader reader = new Iso2709Reader(r)
                    .setStringTransformer(value -> XMLUtil.sanitizeXml10(new String(value.getBytes(ISO88591), UTF8)).toString());
            File file = File.createTempFile("serres.", ".xml");
            FileWriter w = new FileWriter(file);
            MarcXchangeWriter writer = new MarcXchangeWriter(w);
            reader.setFormat("UNIMARC").setType("Bibliographic");
            reader.setMarcXchangeListener(writer);
            writer.startDocument();
            writer.beginCollection();
            reader.parse();
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
            w.close();
        }
    }

}
