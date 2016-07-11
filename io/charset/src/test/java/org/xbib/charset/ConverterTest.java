package org.xbib.charset;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class ConverterTest extends Assert {

    @Test
    public void test() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap("\u00e8\u0075".getBytes("ISO-8859-1"));
        byte[] b = buf.array();
        char[] ch = new char[2];
        ByteToCharUSM94 byteToCharUSM94 = new ByteToCharUSM94();
        byteToCharUSM94.convert(b, 0, b.length, ch, 0, ch.length);
        String s = new String(ch);
        assertEquals("\u0075\u0308", s);
    }

}
