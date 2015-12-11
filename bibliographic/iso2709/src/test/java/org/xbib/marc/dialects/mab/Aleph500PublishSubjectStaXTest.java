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
import org.xbib.marc.event.FieldEventLogger;
import org.xbib.marc.xml.stream.MarcXchangeWriter;
import org.xbib.marc.xml.stream.mapper.MarcXchangeFieldMapperReader;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Aleph500PublishSubjectStaXTest extends StreamTester {

    @Test
    public void testDE605Subject() throws IOException, SAXException {
        String s = "DE-605-aleph500-publish-subject.xml";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile("DE-605-publish-subject.", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        MarcXchangeFieldMapperReader reader = new MarcXchangeFieldMapperReader(in)
            .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");

        Map<String,Object> fields = new HashMap();

        // transform MAB to MARC21
        Map<String,Object> subfields = new HashMap();
        subfields.put("0", "-689$0{r}$0");
        subfields.put("9", "-689$0{r}$0");
        subfields.put("a", ">689$0{r}$a");
        subfields.put("b", "-689$0{r}$b");
        subfields.put("c", "-689$0{r}$c");
        subfields.put("d", "-689$0{r}$d");
        subfields.put("e", ">689$0{r}$e");
        subfields.put("f", ">689$0{r}$f");
        subfields.put("g", ">689$0{r}$g");
        subfields.put("H", "-689$0{r}$H");
        subfields.put("h", "-689$0{r}$h");
        subfields.put("k", ">689$0{r}$k");
        subfields.put("n", "-689$0{r}$n");
        subfields.put("p", ">689$0{r}$p");
        subfields.put("s", ">689$0{r}$s");
        subfields.put("t", ">689$0{r}$t");
        subfields.put("u", "-689$0{r}$u");
        subfields.put("x", "-689$0{r}$x");
        subfields.put("z", ">689$0{r}$z");
        Map<String,Object> indicators = new HashMap();
        indicators.put(" 1", subfields);
        fields.put("902", indicators);

        Map<String,Object> subfields1 = new HashMap();
        subfields1.put("0", "-689$1{r}$0");
        subfields1.put("9", "-689$1{r}$0");
        subfields1.put("a", ">689$1{r}$a");
        subfields1.put("b", "-689$1{r}$b");
        subfields1.put("c", "-689$1{r}$c");
        subfields1.put("d", "-689$1{r}$d");
        subfields1.put("e", ">689$1{r}$e");
        subfields1.put("f", ">689$1{r}$f");
        subfields1.put("g", ">689$1{r}$g");
        subfields1.put("H", "-689$1{r}$H");
        subfields1.put("h", "-689$1{r}$h");
        subfields1.put("k", ">689$1{r}$k");
        subfields1.put("n", "-689$1{r}$n");
        subfields1.put("p", ">689$1{r}$p");
        subfields1.put("s", ">689$1{r}$s");
        subfields1.put("t", ">689$1{r}$t");
        subfields1.put("u", "-689$1{r}$u");
        subfields1.put("x", "-689$1{r}$x");
        subfields1.put("z", ">689$1{r}$z");
        Map<String,Object> indicators1 = new HashMap();
        indicators1.put(" 1", subfields1);
        fields.put("907", indicators1);

        Map<String,Object> subfields2 = new HashMap();
        subfields2.put("0", "-689$2{r}$0");
        subfields2.put("9", "-689$2{r}$0");
        subfields2.put("a", ">689$2{r}$a");
        subfields2.put("b", "-689$2{r}$b");
        subfields2.put("c", "-689$2{r}$c");
        subfields2.put("d", "-689$2{r}$d");
        subfields2.put("e", ">689$2{r}$e");
        subfields2.put("f", ">689$2{r}$f");
        subfields2.put("g", ">689$2{r}$g");
        subfields2.put("H", "-689$2{r}$H");
        subfields2.put("h", "-689$2{r}$h");
        subfields2.put("k", ">689$2{r}$k");
        subfields2.put("n", "-689$2{r}$n");
        subfields2.put("p", ">689$2{r}$p");
        subfields2.put("s", ">689$2{r}$s");
        subfields2.put("t", ">689$2{r}$t");
        subfields2.put("u", "-689$2{r}$u");
        subfields2.put("x", "-689$2{r}$x");
        subfields2.put("z", ">689$2{r}$z");
        Map<String,Object> indicators2 = new HashMap();
        indicators2.put(" 1", subfields2);
        fields.put("912", indicators2);

        // "Permutationsmuster" -> drop
        Map<String,Object> subjInd = new HashMap();
        subjInd.put(" 1", null);
        fields.put("903", subjInd);
        fields.put("908", subjInd);
        fields.put("913", subjInd);

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
        in.close();
        assertStream(s, getClass().getResource("DE-605-aleph500-publish-subject-result.xml").openStream(),
                new FileInputStream(file));
    }

}
