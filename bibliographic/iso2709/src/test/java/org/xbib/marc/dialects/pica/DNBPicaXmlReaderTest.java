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
package org.xbib.marc.dialects.pica;

import org.testng.annotations.Test;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeListener;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DNBPicaXmlReaderTest {

    @Test
    public void testZDBBIBFromOAI() throws Exception {
        InputStream in = getClass().getResourceAsStream("zdb-oai-bib.xml");
        StringBuilder sb = new StringBuilder();
        MarcXchangeListener listener = new MarcXchangeListener() {
            @Override
            public void beginCollection() {
            }

            @Override
            public void endCollection() {
            }

            @Override
            public void leader(String label) {
                sb.append("leader=").append(label).append("\n");
            }

            @Override
            public void beginRecord(String format, String type) {
                sb.append("beginRecord format=").append(format).append(" type=").append(type).append("\n");
            }

            @Override
            public void beginControlField(Field field) {
                sb.append("beginControlField field=").append(field).append("\n");
            }

            @Override
            public void endControlField(Field field) {
                sb.append("endControlField field=").append(field).append("\n");
            }

            @Override
            public void beginDataField(Field field) {
                sb.append("beginDataField field=").append(field).append("\n");
            }

            @Override
            public void endDataField(Field field) {
                sb.append("endDataField field=").append(field).append("\n");
            }

            @Override
            public void beginSubField(Field field) {
                sb.append("beginSubField field=").append(field).append("\n");
            }

            @Override
            public void endSubField(Field field) {
                sb.append("endsubField field=").append(field).append("\n");
            }

            @Override
            public void endRecord() {
                sb.append("endRecord\n");
            }

        };
        DNBPicaXmlReader reader = new DNBPicaXmlReader(new InputStreamReader(in, "UTF-8"));
        reader.setMarcXchangeListener(listener);
        reader.parse();

        FileWriter fw = new FileWriter("zdb-oai-bib-keyvalue.txt");
        fw.write(sb.toString());
        fw.close();
    }

}
