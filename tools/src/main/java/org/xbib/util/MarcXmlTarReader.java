package org.xbib.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.archive.tar.TarConnection;
import org.xbib.io.archive.tar.TarSession;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.xml.stream.MarcXchangeReader;
import org.xbib.util.concurrent.AbstractWorker;
import org.xbib.util.concurrent.LongWorkerRequest;
import org.xbib.util.concurrent.Pipeline;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

public class MarcXmlTarReader extends AbstractWorker<Pipeline,LongWorkerRequest> {

    private final static Logger logger = LogManager.getLogger(MarcXmlTarReader.class.getName());

    private final static XMLInputFactory factory = XMLInputFactory.newInstance();

    private MarcXchangeListener listener;

    private MarcXchangeReader consumer;

    private final LongWorkerRequest counter = new LongWorkerRequest().set(new AtomicLong(0L));

    protected URI uri;

    private TarConnection connection;

    private TarSession session;

    protected Packet packet;

    private boolean prepared;

    @Override
    protected void processRequest(LongWorkerRequest request) {
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


    @Override
    public void close() throws IOException {
        if (session != null) {
            session.close();
            logger.info("session closed");
        }
        if (connection != null) {
            connection.close();
            logger.info("connection closed");
        }
    }

    private boolean prepareRead() throws IOException {
        try {
            if (prepared) {
                return true;
            }
            if (session == null) {
                connection = new TarConnection();
                connection.setPath(Paths.get(uri.getSchemeSpecificPart()), StandardOpenOption.READ);
                session = connection.createSession();
                session.open(Session.Mode.READ);
                if (!session.isOpen()) {
                    throw new IOException("session could not be opened");
                }
            }
            this.packet = session.read();
            this.prepared = packet != null;
            return prepared;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    private LongWorkerRequest nextRead() {
        if (prepared) {
            prepared = false;
        }
        try {
            process(packet);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        counter.get().incrementAndGet();
        return counter;
    }

    public MarcXmlTarReader setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    public MarcXmlTarReader setListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    public MarcXmlTarReader setEventConsumer(MarcXchangeReader consumer) {
        this.consumer = consumer;
        return this;
    }

    protected void process(Packet packet) throws IOException {
        MarcXchangeReader reader = new MarcXchangeReader((Reader)null);
        reader.setMarcXchangeListener(listener);
        StringReader sr = new StringReader(packet.toString());
        try {
            XMLEventReader xmlReader = factory.createXMLEventReader(sr);
            while (xmlReader.hasNext()) {
                reader.add(xmlReader.nextEvent());
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

}