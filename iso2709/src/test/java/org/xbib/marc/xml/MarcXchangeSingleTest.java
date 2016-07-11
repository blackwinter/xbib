package org.xbib.marc.xml;

import org.junit.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.xml.sax.MarcXchangeContentHandler;
import org.xbib.marc.xml.sax.MarcXchangeReader;
import org.xbib.marc.xml.stream.MarcXchangeWriter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;

public class MarcXchangeSingleTest extends StreamTester {

    @Test
    public void testMarcXchangeListener() throws Exception {
        final StringBuilder sb = new StringBuilder();
        MarcXchangeListener listener = new MarcXchangeContentHandler() {
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

        };

        File file = File.createTempFile("HT016424175-out.", ".xml");
        file.deleteOnExit();
        FileWriter sw = new FileWriter(file);
        MarcXchangeWriter writer = new MarcXchangeWriter(sw);
        writer.setFormat("AlephXML").setType("Bibliographic");
        writer.setMarcXchangeListener(listener);

        writer.startDocument();
        writer.beginCollection();

        // write one MARC record twice
        String s = "HT016424175.xml";
        InputStream in = getClass().getResourceAsStream(s);
        MarcXchangeReader reader = new MarcXchangeReader(in);
        reader.setFormat("AlephXML").setType("Bibliographic");
        reader.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
        reader.setMarcXchangeListener(writer);
        reader.parse();
        in.close();

        in = getClass().getResourceAsStream(s);
        reader = new MarcXchangeReader(in);
        reader.setFormat("AlephXML").setType("Bibliographic");
        reader.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
        reader.setMarcXchangeListener(writer);
        reader.parse();
        in.close();

        writer.endCollection();
        writer.endDocument();
        sw.close();

        assertNull(writer.getException());

        assertStream(s, getClass().getResource("HT016424175-out.xml").openStream(),
                new FileInputStream(file));
        assertStream(s, getClass().getResource("HT016424175-keyvalue.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
    }
}
