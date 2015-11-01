
package org.xbib.io;

import java.io.FileInputStream;
import java.net.URI;
import java.util.zip.GZIPInputStream;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Test packet write
 *
 */
public class PacketTest extends Assert {

    @Test
    public void testPacketWrite() throws Exception {
        URI uri = URI.create("file:target/packetdemo.gz");
        ConnectionService<Session<StringPacket>> service = ConnectionService.getInstance();
        Connection<Session<StringPacket>> c = service
                .getConnectionFactory(uri)
                .getConnection(uri);
        Session<StringPacket> session = c.createSession();
        session.open(Session.Mode.APPEND);
        StringPacket data = session.newPacket();
        data.name("demopacket");
        data.packet("Hello World");
        session.write(data);
        session.close();
        // check file
        FileInputStream in = new FileInputStream("target/packetdemo.gz");
        GZIPInputStream gz = new GZIPInputStream(in);
        byte[] buf = new byte[11];
        gz.read(buf);
        gz.close();
        assertEquals(new String(buf), "Hello World");
    }

}
