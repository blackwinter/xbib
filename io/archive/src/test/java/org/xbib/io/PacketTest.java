
package org.xbib.io;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.io.archive.file.FileConnection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public class PacketTest extends Assert {

    @Test
    public void testPacketWrite() throws Exception {
        Path path = Paths.get("build/packetdemo.gz");
        FileConnection c = new FileConnection(path.toUri().toURL());
        Session<StringPacket> session = c.createSession();
        session.open(Session.Mode.APPEND);
        StringPacket data = session.newPacket();
        data.name("demopacket");
        data.packet("Hello World");
        session.write(data);
        session.close();
        // check file
        GZIPInputStream gz = new GZIPInputStream(Files.newInputStream(path));
        byte[] buf = new byte[11];
        int i = gz.read(buf);
        gz.close();
        assertEquals(new String(buf), "Hello World");
    }

}
