package org.xbib.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;
import java.util.SortedMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CharsetTest extends Assert {

    @Test
    public void listCharsets() throws Exception {
        SortedMap<String, Charset> map = Charset.availableCharsets();
        for (Map.Entry<String, Charset> me : map.entrySet()) {
            me.getKey();
            me.getValue();
        }
    }

    @Test
    public void testMAB2() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap("Éa".getBytes("ISO-8859-1"));
        Charset charset = Charset.forName("MAB2");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cbuf = decoder.decode(buf);
        String output = cbuf.toString();
        assertEquals(output, "ä");
    }

    @Test
    public void testXMAB() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap("Éa".getBytes("ISO-8859-1"));
        Charset charset = Charset.forName("x-MAB");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cbuf = decoder.decode(buf);
        String output = cbuf.toString();
        assertEquals(output, "ä");
    }

    @Test
    public void testPound() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap("\u00A3".getBytes("ISO-8859-1"));
        Charset charset = Charset.forName("x-MAB");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cbuf = decoder.decode(buf);
        String output = cbuf.toString();
        assertEquals(output, "£");
    }

}
