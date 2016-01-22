package org.xbib.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.archive.tar.TarSession;
import org.xbib.util.concurrent.AbstractWorker;
import org.xbib.util.concurrent.LongWorkerRequest;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.Worker;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class reads from a TAR archive, without knowing of the concrete content type.
 * Processing TAR packets are delegated to an implementing class.
 */
public abstract class AbstractTarReader extends AbstractWorker<Pipeline,LongWorkerRequest> {

    private final static Logger logger = LogManager.getLogger(AbstractTarReader.class.getName());

    private final ConnectionService<TarSession> service = ConnectionService.getInstance();

    private final LongWorkerRequest counter = new LongWorkerRequest().set(new AtomicLong(0L));

    protected URI uri;

    private Connection<TarSession> connection;

    private TarSession session;

    protected Packet packet;

    private boolean prepared;

    public AbstractTarReader setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    /*@Override
    public boolean hasNext() {
        try {
            return prepareRead();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public LongPipelineElement next() {
        return nextRead();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }
*/
    @Override
    public void newRequest(Worker<Pipeline, LongWorkerRequest> worker, LongWorkerRequest request) {

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
                createSession();
            }
            this.packet = read(session);
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

    private void createSession() throws IOException {
        this.connection = service
                .getConnectionFactory(uri)
                .getConnection(uri);
        this.session = connection.createSession();
        session.open(Session.Mode.READ);
        if (!session.isOpen()) {
            throw new IOException("session could not be opened");
        }
    }

    private Packet read(Session session) throws IOException {
        return session.read();
    }

    protected abstract void process(Packet packet) throws IOException;
}