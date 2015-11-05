package org.xbib.marc.dialects.sisis;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.xml.stream.MarcXchangeWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static org.testng.Assert.assertNull;

public class SisisTest extends StreamTester {

    @Test
    public void testSisis() throws Exception {
        String s = "unloaddipl";
        InputStream in = getClass().getResourceAsStream(s);
        File file = File.createTempFile("DE-A96-" + s + "-sisis.", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        try (InputStreamReader r = new InputStreamReader(in, "UTF-8")) {
            SisisReader reader = new SisisReader(r);
            MarcXchangeWriter writer = new MarcXchangeWriter(out);
            writer.setFormat(MarcXchangeConstants.MARCXCHANGE);
            reader.setMarcXchangeListener(writer);
            writer.startDocument();
            writer.beginCollection();
            reader.parse();
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
        }
        out.close();
        assertStream(s, getClass().getResource("DE-A96-unloaddipl-sisis.xml").openStream(),
                new FileInputStream(file));
    }

    @Test
    public void testSisisMapped() throws Exception {
        String s = "testTit.tit";
        InputStream in = getClass().getResourceAsStream(s);
        File file = File.createTempFile("DE-836-" + s + "-sisis.", ".xml");
        FileOutputStream out = new FileOutputStream(file);
        Map<String, Object> translateMap = new ObjectMapper()
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .readValue(getClass().getResourceAsStream("/org/xbib/marc/dialects/sisis/translate.json"), Map.class);

        try (InputStreamReader r = new InputStreamReader(in, "UTF-8")) {
            SisisReader reader = new SisisReader(r)
                    .addFieldMap("translate", translateMap);
            MarcXchangeWriter writer = new MarcXchangeWriter(out);
            writer.setFormat(MarcXchangeConstants.MARCXCHANGE);
            reader.setMarcXchangeListener(writer);
            writer.startDocument();
            writer.beginCollection();
            reader.parse();
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
        }
        out.close();
        assertStream(s, getClass().getResource("DE-836-testTit.tit-sisis.xml").openStream(),
                new FileInputStream(file));
    }


}
