package org.xbib.marc.xml.mapper;

import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.xml.stream.MarcXchangeWriter;
import org.xbib.marc.xml.stream.mapper.MarcXchangeFieldMapperReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNull;

public class MarcXchangeFieldMapperEventConsumerTest extends StreamTester {

    private final XMLInputFactory factory = XMLInputFactory.newInstance();

    @Test
    public void testMarcXchangeCleaner() throws Exception {
        File file = File.createTempFile("HT016424175-clean.", ".xml");
        FileWriter sw = new FileWriter(file);
        MarcXchangeWriter writer = new MarcXchangeWriter(sw);
        writer.setFormat("AlephXML").setType("Bibliographic");
        writer.startDocument();
        writer.beginCollection();

        MarcXchangeFieldMapperReader consumer = new MarcXchangeFieldMapperReader()
                .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                .setMarcXchangeListener(writer);
        try (InputStream in = getClass().getResourceAsStream("HT016424175.xml")) {
            XMLEventReader xmlReader = factory.createXMLEventReader(in);
            while (xmlReader.hasNext()) {
                XMLEvent event = xmlReader.nextEvent();
                consumer.add(event);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        writer.endCollection();
        writer.endDocument();
        sw.close();
        assertNull(writer.getException());

        assertStream(getClass().getResource("HT016424175-clean.xml").openStream(),
                new FileInputStream(file));
    }

    @Test
    public void testMarcXchangeFieldMapperEventConsumer() throws Exception {
        File file = File.createTempFile("HT016424175-event-fieldmapper.", ".xml");
        FileWriter sw = new FileWriter(file);
        MarcXchangeWriter writer = new MarcXchangeWriter(sw);
        writer.setFormat("AlephXML").setType("Bibliographic");
        writer.startDocument();
        writer.beginCollection();

        Map<String,Object> subfields = new HashMap();
        subfields.put("", "245$01");
        subfields.put("a", "245$01$a");
        Map<String,Object> indicators = new HashMap();
        indicators.put(" 1", subfields);
        Map<String,Object> fields = new HashMap();
        fields.put("331", indicators);

        MarcXchangeFieldMapperReader consumer = new org.xbib.marc.xml.stream.mapper.MarcXchangeFieldMapperReader()
                .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                .setMarcXchangeListener(writer)
                .addFieldMap("test", fields);

        try (InputStream in = getClass().getResourceAsStream("HT016424175.xml")) {
            XMLEventReader xmlReader = factory.createXMLEventReader(in);
            while (xmlReader.hasNext()) {
                XMLEvent event = xmlReader.nextEvent();
                consumer.add(event);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        writer.endCollection();
        writer.endDocument();
        sw.close();
        assertNull(writer.getException());

        assertStream(getClass().getResource("HT016424175-event-fieldmapper.xml").openStream(),
                new FileInputStream(file));
    }
}
