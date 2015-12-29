package org.xbib.io.archive.classpath;

import org.junit.Test;
import org.xbib.io.ConnectionService;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class ClasspathTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testClasspathResource() throws IOException {
        URI uri = URI.create("classpath:test.txt");
        ClasspathSession<StringPacket> session = (ClasspathSession<StringPacket>) ConnectionService.getInstance()
                .getConnectionFactory(uri)
                .getConnection(uri)
                .createSession();
        session.open(Session.Mode.READ);
        StringPacket p = session.read();
        assertEquals("Hello World", p.packet());
    }
}
