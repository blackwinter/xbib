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
package org.xbib.marc.xml.mapper;

import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.xml.stream.MarcXchangeWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNull;

public class MarcXchangeMappedOAITest extends StreamTester {

    @Test
    public void testMarcXMLFromOAI() throws Exception {
        final StringBuilder sb = new StringBuilder();
        File file = File.createTempFile("zdb-oai-marc-fieldmapper.", ".txt");
        FileWriter fw = new FileWriter(file);
        MarcXchangeWriter writer = new MarcXchangeWriter(fw);

        writer.setMarcXchangeListener(new MarcXchangeListener() {

            @Override
            public void beginCollection() {
            }

            @Override
            public void endCollection() {
            }

            @Override
            public void beginRecord(String format, String type) {
                sb.append("beginRecord").append("\n");
                sb.append(format).append("\n");
                sb.append(type).append("\n");
            }

            @Override
            public void leader(String label) {
                sb.append("leader").append("\n");
                sb.append(label).append("\n");
            }

            @Override
            public void beginControlField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void endControlField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void beginDataField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void endDataField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void beginSubField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void endSubField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void endRecord() {
                sb.append("endRecord").append("\n");
            }

        });

        String s = "zdb-oai-marc.xml";
        InputStream in = getClass().getResourceAsStream(s);
        MarcXchangeFieldMapperReader reader = new MarcXchangeFieldMapperReader(in);
        reader.setMarcXchangeListener(writer);

        // just for fun: 084->085
        Map<String,Object> indicators = new HashMap<String, Object>();
        indicators.put("  ", "085$  ");
        Map<String,Object> fields = new HashMap<String, Object>();
        fields.put("084", indicators);
        reader.addFieldMap("test", fields);

        writer.startDocument();
        writer.beginCollection();
        reader.parse();
        in.close();
        writer.endCollection();
        writer.endDocument();
        fw.close();

        assertNull(writer.getException());

        assertStream(s, getClass().getResource("zdb-oai-marc-fieldmapper-keyvalue.txt").openStream(),
                new FileInputStream(file));

    }
}
