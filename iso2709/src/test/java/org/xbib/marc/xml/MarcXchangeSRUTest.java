package org.xbib.marc.xml;

import org.testng.annotations.Test;
import org.xbib.StreamUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import static org.testng.Assert.assertEquals;

public class MarcXchangeSRUTest {

    private final Logger logger = LoggerFactory.getLogger(MarcXchangeSRUTest.class.getName());

    @Test
    public void testMarcXchangeListener() throws Exception {
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
                logger.debug("beginRecord format="+format + " type="+type);
                sb.append("beginRecord").append("\n");
                sb.append(format).append("\n");
                sb.append(type).append("\n");
            }

            @Override
            public void leader(String label) {
                logger.debug("leader="+label);
                sb.append("leader").append("\n");
                sb.append(label).append("\n");
            }

            @Override
            public void beginControlField(Field field) {
                logger.debug("beginControlField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endControlField(Field field) {
                logger.debug("endControlField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void beginDataField(Field field) {
                logger.debug("beginDataField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endDataField(Field field) {
                logger.debug("endDataField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void beginSubField(Field field) {
                logger.debug("beginSubField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endSubField(Field field) {
                logger.debug("endsubField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endRecord() {
                logger.debug("endRecord");
                sb.append("endRecord").append("\n");
            }

        };

        InputStream in = getClass().getResource("zdb-sru-marcxmlplus.xml").openStream();
        MarcXchangeReader reader = new MarcXchangeReader();
        reader.setContentHandler(handler);
        reader.parse(in);
        in.close();

        InputStreamReader r = new InputStreamReader(getClass().getResource("zdb-sru-marcxmlplus.txt").openStream());
        StringWriter w = new StringWriter();
        StreamUtil.copy(r, w);
        assertEquals(sb.toString(), w.toString());
        r.close();

    }
}