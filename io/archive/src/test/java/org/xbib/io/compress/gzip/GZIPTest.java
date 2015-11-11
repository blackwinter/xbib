
package org.xbib.io.compress.gzip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

public class GZIPTest {

    @Test
    public void testHelloWorld() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zOut = new GZIPOutputStream(out);
        ObjectOutputStream objOut = new ObjectOutputStream(zOut);
        String helloWorld = "Hello World!";
        objOut.writeObject(helloWorld);
        zOut.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        GZIPInputStream zIn = new GZIPInputStream(in);
        ObjectInputStream objIn = new ObjectInputStream(zIn);
        assertEquals("Hello World!", objIn.readObject());
    }

    @Test
    public void testGzipChannel() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zOut = new GZIPOutputStream(out);
        ObjectOutputStream objOut = new ObjectOutputStream(zOut);
        for (int i = 0; i < 10000; i++) {
            String s = "Hello World!";
            objOut.writeObject(s);
        }
        zOut.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        GZIPInputStream gzin = new GZIPInputStream(in);
        ReadableByteChannel chin = Channels.newChannel(gzin);
        out = new ByteArrayOutputStream();
        WritableByteChannel chout = Channels.newChannel(out);
        ByteBuffer buffer = ByteBuffer.allocate(65536);
        while (chin.read(buffer) != -1) {
            buffer.flip();
            chout.write(buffer);
            buffer.clear();
        }
    }

}
