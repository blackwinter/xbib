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
import org.xbib.util.KeyValueStreamAdapter;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.event.FieldEventLogger;
import org.xbib.marc.xml.stream.MarcXchangeWriter;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class DE468MABTest extends StreamTester {

    /**
     * DE-468 delivers "MAB in MARC" with UTF-8, subfield delimiter "$$", but subfield code length 2 (not 3).
     *
     * This is also known as "Aleph 500 $$ delimited subfields export"
     *
     * @throws IOException
     * @throws SAXException
     */

    public void testDE468() throws IOException, SAXException {
        String s = "aleph500-subfields.mrc";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile("DE-468.", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        Iso2709Reader reader = new Iso2709Reader(in, "UTF-8");
        reader.setFormat("MAB");
        // Set delimiter. Will be automatically quoted before used as split pattern.
        reader.setSubfieldDelimiter("$$");
        reader.setSubfieldCodeLength(2);
        reader.setScrubData(true);
        MarcXchangeWriter writer = new MarcXchangeWriter(out);
        writer.setFormat("MAB").setType("Bibliographic");
        reader.setMarcXchangeListener(writer);
        writer.startDocument();
        writer.beginCollection();
        reader.parse();
        writer.endCollection();
        writer.endDocument();
        out.close();
        assertStream(s, getClass().getResource("DE-468.xml").openStream(),
                new FileInputStream(file));
    }

    @Test
    public void testMappedDE468() throws IOException, SAXException {
        String s = "aleph500-subfields.mrc";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile("DE-468-mapped.", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        Iso2709Reader reader = new Iso2709Reader(in, "UTF-8");
        reader.setFormat("MAB");
        // custom subfield delimiter. Will be automatically quoted before used as split pattern.
        reader.setSubfieldDelimiter("$$");
        // fix subfield code length
        reader.setSubfieldCodeLength(2);

        // 902$ $ 9 -> 689$00$a0
        Map<String,Object> subfields = new HashMap();
        subfields.put("", ">689$0{r}");
        subfields.put(" ", "-689$0{r}$a");
        subfields.put("a", "-689$0{r}$a");
        subfields.put("9", "-689$0{r}$0");
        subfields.put("h", "-689$0{r}$h");
        Map<String,Object> indicators = new HashMap();
        indicators.put(" ", subfields);
        Map<String,Object> fields = new HashMap();
        fields.put("902", indicators);

        reader.addFieldMap("test", fields); // --> selects field mapper

        reader.setFieldEventListener(new FieldEventLogger("info"));

        MarcXchangeWriter writer = new MarcXchangeWriter(out);
        writer.setFormat("MARC21").setType("Bibliographic");
        reader.setMarcXchangeListener(writer);
        writer.startDocument();
        writer.beginCollection();
        reader.parse();
        writer.endCollection();
        writer.endDocument();
        out.close();
        assertStream(s, getClass().getResource("DE-468-mapped.xml").openStream(),
                new FileInputStream(file));
    }


    public void testKeyValueDE468MAB() throws Exception {
        String s = "DE-468-keyvalue.";
        File file = File.createTempFile(s, ".txt");
        StringWriter sw = new StringWriter();
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(new KeyValueStreamAdapter<FieldList, String>() {
                    @Override
                    public KeyValueStreamAdapter<FieldList, String> begin() {
                        sw.write("begin object\n");
                        return this;
                    }

                    @Override
                    public KeyValueStreamAdapter<FieldList, String> keyValue(FieldList fields, String value) {
                        sw.write("begin\n");
                        for (Field f : fields) {
                            sw.write(String.format("tag=%s indicator=%s subfield=%s data=%s\n",
                                    f.tag(), f.indicator(), f.subfieldId(), f.data()));
                        }
                        sw.write("end\n");
                        return this;
                    }

                    @Override
                    public KeyValueStreamAdapter<FieldList, String> end() {
                        sw.write("end object\n");
                        return this;
                    }

            });
        InputStream in = getClass().getResource("aleph500-subfields.mrc").openStream();
        Iso2709Reader reader = new Iso2709Reader(in, "UTF-8");
        reader.setFormat("MAB");
        reader.setSubfieldDelimiter("$$");
        reader.setSubfieldCodeLength(2);
        reader.setMarcXchangeListener(kv);
        reader.parse();
        in.close();
        sw.close();
        FileWriter fw = new FileWriter(file);
        fw.write(sw.toString());
        fw.close();
        assertStream(s, getClass().getResource("DE-468-keyvalue.txt").openStream(),
                new FileInputStream(file));
    }


    public void testFieldKillerDE468() throws IOException, SAXException {
        String s = "aleph500-subfields.mrc";
        InputStream in = getClass().getResource(s).openStream();
                File file = File.createTempFile("DE-468-killed-fields.", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        Iso2709Reader reader = new Iso2709Reader(in, "UTF-8");
        reader.setFormat("MAB");
        // Set delimiter. Will be automatically quoted before used as split pattern.
        reader.setSubfieldDelimiter("$$");
        reader.setSubfieldCodeLength(2);

        // drop all "700$g" fields
        Map<String,Object> subf = new HashMap();
        subf.put("", null);
        subf.put(" ", null);
        subf.put("a", null);
        Map<String,Object> indicators = new HashMap();
        indicators.put("g", null);
        Map<String,Object> fields = new HashMap();
        fields.put("700", indicators);

        reader.addFieldMap("dropper", fields);

        reader.setFieldEventListener(new FieldEventLogger("info"));

        MarcXchangeWriter writer = new MarcXchangeWriter(out);
        writer.setFormat("MARC21").setType("Bibliographic");
        reader.setMarcXchangeListener(writer);
        writer.startDocument();
        writer.beginCollection();
        reader.parse();
        writer.endCollection();
        writer.endDocument();
        out.close();

        assertStream(s, getClass().getResource("DE-468-killed-fields.xml").openStream(),
                new FileInputStream(file));
    }

}
