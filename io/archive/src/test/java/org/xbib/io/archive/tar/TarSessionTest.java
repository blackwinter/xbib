package org.xbib.io.archive.tar;

import java.net.URL;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;

public class TarSessionTest {

    private static final Logger logger = LogManager.getLogger(TarSessionTest.class.getName());

    public void readFromTar() throws Exception {
        TarConnection c = new TarConnection(new URL("tar:test.tar.bz2"));
        TarSession session = c.createSession();
        session.open(Session.Mode.READ);
        StringPacket message;
        while ((message = session.read()) != null) {
            logger.info("name = {} object = {}", message.name(), message.packet());
        }
        session.close();
        c.close();
    }

    public void writeToTar() throws Exception {
        TarConnection from = new TarConnection(new URL("src/test/resources/test.tar.bz2"));
        TarSession fromSession = from.createSession();
        fromSession.open(Session.Mode.READ);
        TarConnection to = new TarConnection(new URL("file:test.tar.bz2"));
        TarSession toSession = to.createSession();
        toSession.open(Session.Mode.WRITE);
        StringPacket message;
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
