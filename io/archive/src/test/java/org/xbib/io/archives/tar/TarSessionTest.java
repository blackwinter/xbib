package org.xbib.io.archives.tar;

import java.io.File;
import java.net.URI;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.testng.annotations.Test;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.Session;
import org.xbib.io.ObjectPacket;

public class TarSessionTest {

    private static final Logger logger = LogManager.getLogger(TarSessionTest.class.getName());

    public void readFromTar() throws Exception {
        URI uri = URI.create("resource:test.tar.bz2");
        Connection<Session> c = ConnectionService.getInstance()
                .getConnectionFactory(uri)
                .getConnection(uri);
        Session<ObjectPacket> session = c.createSession();
        session.open(Session.Mode.READ);
        ObjectPacket message;
        while ((message = session.read()) != null) {
            logger.info("name = {} object = {}", message.name(), message.packet());
        }
        session.close();
        c.close();
    }

    public void writeToTar() throws Exception {
        URI fromUri = URI.create("file:src/test/resources/test.tar.bz2");
        URI toUri = URI.create("file:test.tar.bz2");
        Connection<Session> from = ConnectionService.getInstance()
                .getConnectionFactory(fromUri)
                .getConnection(fromUri);
        Connection<Session> to = ConnectionService.getInstance()
                .getConnectionFactory(toUri)
                .getConnection(toUri);
        Session<ObjectPacket> fromSession = from.createSession();
        fromSession.open(Session.Mode.READ);
        Session<ObjectPacket> toSession = to.createSession();
        toSession.open(Session.Mode.WRITE);
        ObjectPacket message;
        while ((message = fromSession.read()) != null) {
            logger.info("name = {} object = {}", message.name(), message.packet());
            toSession.write(message);
        }
        fromSession.close();
        from.close();
        toSession.close();
        to.close();
    }
}
