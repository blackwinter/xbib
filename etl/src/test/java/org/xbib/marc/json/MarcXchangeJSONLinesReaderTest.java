package org.xbib.marc.json;

import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.Field;
import org.xbib.marc.xml.MarcXchangeContentHandler;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;

public class MarcXchangeJSONLinesReaderTest extends StreamTester {

    @Test
    public void testZDBJSONLines() throws Exception {
        final StringBuilder sb = new StringBuilder();
        MarcXchangeContentHandler handler = new MarcXchangeContentHandler() {
            @Override
            public void beginCollection() {
            }

            @Override
            public void endCollection() {
            }

            @Override
            public void beginRecord(String format, String type) {
                sb.append("beginRecord").append("\n");
                sb.append("format=").append(format).append("\n");
                sb.append("type=").append(type).append("\n");
            }

            @Override
            public void leader(String label) {
                sb.append("leader").append("=").append(label).append("\n");
            }

            @Override
            public void beginControlField(Field field) {
                sb.append("begin controlfield=").append(field).append("\n");
            }

            @Override
            public void endControlField(Field field) {
                sb.append("end controlfield=").append(field).append("\n");
            }

            @Override
            public void beginDataField(Field field) {
                sb.append("begin datafield=").append(field).append("\n");
            }

            @Override
            public void endDataField(Field field) {
                sb.append("end datafield=").append(field).append("\n");
            }

            @Override
            public void beginSubField(Field field) {
                sb.append("begin subfield=").append(field).append("\n");
            }

            @Override
            public void endSubField(Field field) {
                sb.append("end subfield=").append(field).append("\n");
            }

            @Override
            public void endRecord() {
                sb.append("endRecord").append("\n");
            }

        };

        InputStream in = getClass().getResource("zdb-marc.json").openStream();
        if (in == null) {
            throw new FileNotFoundException();
        }
        MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, handler);
        reader.parse();
        in.close();
        assertStream(getClass().getResource("zdb-marc-json.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));

    }

    @Test
    public void testHBZ() throws Exception {
        final StringBuilder sb = new StringBuilder();
        MarcXchangeContentHandler handler = new MarcXchangeContentHandler() {
            @Override
            public void beginCollection() {
            }

            @Override
            public void endCollection() {
            }

            @Override
            public void beginRecord(String format, String type) {
                sb.append("beginRecord").append("\n");
                sb.append("format=").append(format).append("\n");
                sb.append("type=").append(type).append("\n");
            }

            @Override
            public void leader(String label) {
                sb.append("leader").append("=").append(label).append("\n");
            }

            @Override
            public void beginControlField(Field field) {
                sb.append("start controlfield=").append(field).append("\n");
            }

            @Override
            public void endControlField(Field field) {
                sb.append("end controlfield=").append(field).append("\n");
            }

            @Override
            public void beginDataField(Field field) {
                sb.append("start datafield=").append(field).append("\n");
            }

            @Override
            public void endDataField(Field field) {
                sb.append("end datafield=").append(field).append("\n");
            }

            @Override
            public void beginSubField(Field field) {
                sb.append("start subfield=").append(field).append("\n");
            }

            @Override
            public void endSubField(Field field) {
                sb.append("end subfield=").append(field).append("\n");
            }

            @Override
            public void endRecord() {
                sb.append("endRecord").append("\n");
            }

        };

        InputStream in = getClass().getResource("hbz.json").openStream();
        if (in == null) {
            throw new FileNotFoundException();
        }
        MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, handler);
        reader.parse();
        in.close();

        //System.err.println(sb.toString());

        //assertStream(getClass().getResource("zdb-marc-json.txt").openStream(),
        //        new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));

    }

}
