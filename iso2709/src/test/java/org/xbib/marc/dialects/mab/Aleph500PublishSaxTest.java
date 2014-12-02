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
import org.xbib.marc.event.FieldEventLogger;
import org.xbib.marc.xml.stream.MarcXchangeWriter;
import org.xbib.marc.xml.mapper.MarcXchangeFieldMapperReader;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Aleph500PublishSaxTest extends StreamTester {

    @Test
    public void testDE605() throws IOException, SAXException {
        InputStream in = getClass().getResource("DE-605-aleph500-publish.xml").openStream();
        File file = File.createTempFile("DE-605-sax-result.", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        MarcXchangeFieldMapperReader reader = new MarcXchangeFieldMapperReader(in)
            .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");

        // transform MAB to MARC21
        Map<String,Object> subfields = new HashMap();
        subfields.put(" ", ">689$0{r}$a");
        subfields.put("a", ">689$0{r}$a");
        subfields.put("s", ">689$0{r}$s");
        subfields.put("0", "-689$0{r}$0");
        // 902$ $ 9 -> 689$00$a0
        subfields.put("9", "-689$0{r}$0");
        subfields.put("h", "-689$0{r}$h");
        Map<String,Object> indicators = new HashMap();
        indicators.put(" 1", subfields);
        Map<String,Object> fields = new HashMap();
        fields.put("902", indicators);

        // "Permutationsmuster" -> drop
        Map<String,Object> subjsubf = new HashMap();
        subjsubf.put("a", null);
        Map<String,Object> subjInd = new HashMap();
        subjInd.put(" 1", subjsubf);
        fields.put("903", subjInd);


        // transform MAB "Gesamttitel" to MARC21 "Series added entry - uniform title"
        Map<String,Object> serSubf = new HashMap();
        serSubf.put("a", ">830$ 0$t");
        Map<String,Object> serInd = new HashMap();
        serInd.put(" 1", serSubf);
        fields.put("451", serInd);
        fields.put("461", serInd);

        Map<String,Object> serSubf2 = new HashMap();
        serSubf2.put("a", "-830$ 0$w");
        Map<String,Object> serInd2 = new HashMap();
        serInd2.put(" 1", serSubf2);
        fields.put("453", serInd2);
        fields.put("463", serInd2);

        Map<String,Object> serSubf3 = new HashMap();
        serSubf3.put("a", "-830$ 0$v");
        Map<String,Object> serInd3 = new HashMap();
        serInd3.put(" 1", serSubf3);
        fields.put("455", serInd3);
        fields.put("465", serInd3);

        Map<String,Object> serSubf4 = new HashMap();
        serSubf4.put("a", "-830$ 0$n");
        Map<String,Object> serInd4 = new HashMap();
        serInd4.put(" 1", serSubf4);
        fields.put("456", serInd4);
        fields.put("466", serInd4);

        reader.addFieldMap("transform", fields);

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
        assertStream(getClass().getResource("DE-605-sax-result.xml").openStream(),
                new FileInputStream(file));
    }

}
