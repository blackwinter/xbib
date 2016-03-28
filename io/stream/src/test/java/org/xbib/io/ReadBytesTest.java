package org.xbib.io;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public final class ReadBytesTest {
    @Parameterized.Parameter
    public Factory factory;
    private Buffer data;
    private BufferedSource source;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{new Factory() {
                    @Override
                    public BufferedSource create(Buffer data) {
                        return data;
                    }

                    @Override
                    public String toString() {
                        return "Buffer";
                    }
                }},
                new Object[]{new Factory() {
                    @Override
                    public BufferedSource create(Buffer data) {
                        return new RealBufferedSource(data);
                    }

                    @Override
                    public String toString() {
                        return "ReadBytes";
                    }
                }},
                new Object[]{new Factory() {
                    @Override
                    public BufferedSource create(Buffer data) {
                        return new RealBufferedSource(new ForwardingSource(data) {
                            @Override
                            public long read(Buffer sink, long byteCount) throws IOException {
                                return super.read(sink, Math.min(1, byteCount));
                            }
                        });
                    }

                    @Override
                    public String toString() {
                        return "Slow ReadBytes";
                    }
                }}
        );
    }

    @Before
    public void setUp() {
        data = new Buffer();
        source = factory.create(data);
    }

    public void readLines() throws IOException {
        data.writeUtf8("abc\ndef\n");
        //System.err.println(source.indexOf((byte)'\n'));
        //assertEquals("abc", source.readUtf8Line());
        //assertEquals("def", source.readUtf8Line());

        assertEquals("abc", new String(source.readToDelimiter((byte)'\n')));
        assertEquals("def", new String(source.readToDelimiter((byte)'\n')));
        try {
            source.readUtf8LineStrict();
            fail();
        } catch (EOFException expected) {
            assertEquals("newline not found: size=0 content=...", expected.getMessage());
        }
    }


    public void eofExceptionProvidesLimitedContent() throws IOException {
        data.writeUtf8("aaaaaaaabbbbbbbbccccccccdddddddde");
        try {
            source.readUtf8LineStrict();
            fail();
        } catch (EOFException expected) {
            assertEquals("newline not found: size=33 content=616161616161616162626262626262626363636363636363"
                    + "6464646464646464...", expected.getMessage());
        }
    }


    public void emptyLines() throws IOException {
        data.writeUtf8("\n\n\n");
        assertEquals("", source.readUtf8LineStrict());
        assertEquals("", source.readUtf8LineStrict());
        assertEquals("", source.readUtf8LineStrict());
        assertTrue(source.exhausted());
    }


    public void crDroppedPrecedingLf() throws IOException {
        data.writeUtf8("abc\r\ndef\r\nghi\rjkl\r\n");
        assertEquals("abc", source.readUtf8LineStrict());
        assertEquals("def", source.readUtf8LineStrict());
        assertEquals("ghi\rjkl", source.readUtf8LineStrict());
    }


    public void bufferedReaderCompatible() throws IOException {
        data.writeUtf8("abc\ndef");
        assertEquals("abc", source.readUtf8Line());
        assertEquals("def", source.readUtf8Line());
        assertEquals(null, source.readUtf8Line());
    }


    public void bufferedReaderCompatibleWithTrailingNewline() throws IOException {
        data.writeUtf8("abc\ndef\n");
        assertEquals("abc", source.readUtf8Line());
        assertEquals("def", source.readUtf8Line());
        assertEquals(null, source.readUtf8Line());
    }

    private interface Factory {
        BufferedSource create(Buffer data);
    }
}
