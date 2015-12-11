package org.xbib.marc.xml.stream;

import org.junit.Test;
import org.xbib.helper.StreamTester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class MarcXchangeEventConsumerTest extends StreamTester {

    @Test
    public void testMarcXchangeEventConsumer() throws Exception {
        String s = "HT016424175-event.";
        File file = File.createTempFile(s, ".xml");
        file.deleteOnExit();
        FileWriter sw = new FileWriter(file);
        MarcXchangeWriter writer = new MarcXchangeWriter(sw);
        writer.setFormat("AlephXML").setType("Bibliographic");
        writer.startDocument();
        writer.beginCollection();
        try (InputStream in = getClass().getResourceAsStream("HT016424175.xml")) {
            MarcXchangeReader consumer = new MarcXchangeReader(in)
                    .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                    .setMarcXchangeListener(writer);
            consumer.parse();
        } catch (Exception e) {
            throw new IOException(e);
        }
        writer.endCollection();
        writer.endDocument();
        sw.close();
        assertNull(writer.getException());
        sw.close();
        assertStream(s, getClass().getResource("HT016424175-event.xml").openStream(),
                new FileInputStream(file));
    }
}
