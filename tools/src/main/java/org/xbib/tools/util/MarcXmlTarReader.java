package org.xbib.tools.util;

import org.xbib.io.Packet;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.xml.stream.MarcXchangeReader;
import org.xbib.util.concurrent.LongWorkerRequest;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.Worker;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;

public class MarcXmlTarReader extends AbstractTarReader {

    private final static XMLInputFactory factory = XMLInputFactory.newInstance();

    private MarcXchangeListener listener;

    private MarcXchangeReader consumer;

    public MarcXmlTarReader setURI(URI uri) {
        super.setURI(uri);
        return this;
    }

    public MarcXmlTarReader setListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void process(Packet packet) throws IOException {
        MarcXchangeReader consumer = new MarcXchangeReader((Reader)null);
        consumer.setMarcXchangeListener(listener);
        StringReader sr = new StringReader(packet.toString());
        try {
            XMLEventReader xmlReader = factory.createXMLEventReader(sr);
            while (xmlReader.hasNext()) {
                consumer.add(xmlReader.nextEvent());
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    public MarcXmlTarReader setEventConsumer(MarcXchangeReader consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public void newRequest(Worker<Pipeline, LongWorkerRequest> pipeline, LongWorkerRequest request) {
        try {
            StringReader sr = new StringReader(packet.toString());
            XMLEventReader xmlReader = factory.createXMLEventReader(sr);
            while (xmlReader.hasNext()) {
                XMLEvent event = xmlReader.nextEvent();
                if (consumer != null) {
                    consumer.add(event);
                }
            }
        } catch (XMLStreamException e) {
            // ignore
        }
    }
}