package org.xbib.template.handlebars.internal;

import org.junit.Test;

import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertEquals;

public class FastStringWriterTest {

    @Test
    public void writeCharArray() throws IOException {
        Writer writer = new FastStringWriter();
        writer.write(new char[]{'a', 'b', 'c'});
        assertEquals("abc", writer.toString());
    }

    @Test
    public void writeInt() throws IOException {
        Writer writer = new FastStringWriter();
        writer.write(55);
        assertEquals("7", writer.toString());
    }

    @Test
    public void writeString() throws IOException {
        Writer writer = new FastStringWriter();
        writer.write("7");
        assertEquals("7", writer.toString());
    }

    @Test
    public void writeStringWithOffsetAndLength() throws IOException {
        Writer writer = new FastStringWriter();
        writer.write("hello", 1, 3);
        assertEquals("ell", writer.toString());
    }

    @Test
    public void writeCharArrayWithOffsetAndLength() throws IOException {
        Writer writer = new FastStringWriter();
        writer.write(new char[]{'h', 'e', 'l', 'l', 'o'}, 1, 3);
        assertEquals("ell", writer.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void writeCharArrayWithBadOffsetAndLength() throws IOException {
        Writer writer = new FastStringWriter();
        writer.write(new char[]{'h', 'e', 'l', 'l', 'o'}, -1, 3);
    }

    @Test
    public void writeCharArrayWithOffsetAndZeroLength() throws IOException {
        Writer writer = new FastStringWriter();
        writer.write(new char[]{'h', 'e', 'l', 'l', 'o'}, 1, 0);
        assertEquals("", writer.toString());
    }

    @Test
    public void flush() throws IOException {
        Writer writer = new FastStringWriter();
        writer.flush();
    }

    @Test
    public void close() throws IOException {
        Writer writer = new FastStringWriter();
        writer.append("hello");
        assertEquals("hello", writer.toString());
        writer.close();
        assertEquals("", writer.toString());
    }
}
