
package org.xbib.io.compress.bzip2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BZip2Test extends Assert {

    @Test
    public void testBZip2HelloWorld() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bzip2OutputStream zOut = new Bzip2OutputStream(out);
        ObjectOutputStream objOut = new ObjectOutputStream(zOut);
        String helloWorld = "Hello World!";
        objOut.writeObject(helloWorld);
        zOut.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Bzip2InputStream zIn = new Bzip2InputStream(in);
        ObjectInputStream objIn = new ObjectInputStream(zIn);
        assertEquals("Hello World!", objIn.readObject());
    }

}
