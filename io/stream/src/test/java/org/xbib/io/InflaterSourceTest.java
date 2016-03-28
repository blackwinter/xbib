package org.xbib.io;

import org.junit.Test;

import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import static org.junit.Assert.assertEquals;
import static org.xbib.io.TestUtil.randomBytes;

public final class InflaterSourceTest {


    @Test
    public void inflatePoorlyCompressed() throws Exception {
        ByteString original = randomBytes(1024 * 1024);
        Buffer deflated = deflate(original);
        Buffer inflated = inflate(deflated);
        assertEquals(original, inflated.readByteString());
    }

    /**
     * Use DeflaterOutputStream to deflate source.
     */
    private Buffer deflate(ByteString source) throws IOException {
        Buffer result = new Buffer();
        Sink sink = IO.sink(new DeflaterOutputStream(result.outputStream()));
        sink.write(new Buffer().write(source), source.size());
        sink.close();
        return result;
    }

    /**
     * Returns a new buffer containing the inflated contents of {@code deflated}.
     */
    private Buffer inflate(Buffer deflated) throws IOException {
        Buffer result = new Buffer();
        InflaterSource source = new InflaterSource(deflated, new Inflater());
        while (source.read(result, Integer.MAX_VALUE) != -1) {
        }
        return result;
    }
}
