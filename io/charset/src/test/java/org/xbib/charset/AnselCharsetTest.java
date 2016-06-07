package org.xbib.charset;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.SortedMap;

public class AnselCharsetTest extends Assert {

    @Test
    public void listCharsets() throws Exception {
        SortedMap<String, Charset> map = Charset.availableCharsets();
        assertTrue(map.keySet().contains("Z3947"));
    }

    @Test
    public void testAnsel() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap("\u00e8\u0075".getBytes("ISO-8859-1"));
        Charset charset = Charset.forName("ANSEL");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cbuf = decoder.decode(buf);
        String output = cbuf.toString();
        assertEquals("\u0075\u0308", output);
    }

}
