package org.xbib.io.http.client.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public final class StringUtils {

    private StringUtils() {
        // unused
    }

    public static ByteBuffer charSequence2ByteBuffer(CharSequence cs, Charset charset) {
        return charset.encode(CharBuffer.wrap(cs));
    }

    public static byte[] byteBuffer2ByteArray(ByteBuffer bb) {
        byte[] rawBase = new byte[bb.remaining()];
        bb.get(rawBase);
        return rawBase;
    }

    public static byte[] charSequence2Bytes(CharSequence sb, Charset charset) {
        ByteBuffer bb = charSequence2ByteBuffer(sb, charset);
        return byteBuffer2ByteArray(bb);
    }
}
