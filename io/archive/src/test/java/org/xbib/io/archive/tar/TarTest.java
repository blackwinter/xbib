package org.xbib.io.archive.tar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;

public class TarTest {

    @Test
    public void readFromTar() throws Exception {
        Path fromPath = Paths.get("src/test/resources/test.tar.bz2");
        TarConnection from = new TarConnection();
        from.setPath(fromPath, StandardOpenOption.READ);
        TarSession session = from.createSession();
        session.open(Session.Mode.READ);
        StringPacket message;
        while ((message = session.read()) != null) {
            //logger.info("name = {} object = {}", message.name(), message.packet());
        }
        session.close();
        from.close();
    }

    @Test
    public void writeToTar() throws Exception {
        Path fromPath = Paths.get("src/test/resources/test.tar.bz2");
        TarConnection from = new TarConnection();
        from.setPath(fromPath, StandardOpenOption.READ);
        TarSession fromSession = from.createSession();
        fromSession.open(Session.Mode.READ);
        Path toPath = Paths.get("build/test.tar.bz2");
        TarConnection to = new TarConnection();
        to.setPath(toPath, StandardOpenOption.CREATE);
        TarSession toSession = to.createSession();
        toSession.open(Session.Mode.WRITE);
        StringPacket message;
        while ((message = fromSession.read()) != null) {
            //logger.info("name = {} object = {}", message.name(), message.packet());
            toSession.write(message);
        }
        fromSession.close();
        from.close();
        toSession.close();
        to.close();
    }
}
