
package org.xbib.io.compress.xz;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class XZTest extends Assert {

    @Test
    public void testHelloWorld() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XZOutputStream zOut = new XZOutputStream(out, new LZMA2Options());
        ObjectOutputStream objOut = new ObjectOutputStream(zOut);
        String helloWorld = "Hello World!";
        objOut.writeObject(helloWorld);
        zOut.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        XZInputStream zIn = new XZInputStream(in);
        ObjectInputStream objIn = new ObjectInputStream(zIn);
        assertEquals("Hello World!", objIn.readObject());
    }
    
}
