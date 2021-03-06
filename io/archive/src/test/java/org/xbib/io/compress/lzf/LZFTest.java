
package org.xbib.io.compress.lzf;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LZFTest extends Assert {

    @Test
    public void testHelloWorld() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LZFOutputStream zOut = new LZFOutputStream(out);
        ObjectOutputStream objOut = new ObjectOutputStream(zOut);
        String helloWorld = "Hello World!";
        objOut.writeObject(helloWorld);
        zOut.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        LZFInputStream zIn = new LZFInputStream(in);
        ObjectInputStream objIn = new ObjectInputStream(zIn);
        assertEquals("Hello World!", objIn.readObject());
    }
    
}
