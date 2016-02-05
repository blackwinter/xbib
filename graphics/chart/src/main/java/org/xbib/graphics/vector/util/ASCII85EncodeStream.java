package org.xbib.graphics.vector.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class ASCII85EncodeStream extends FilterOutputStream {
    private static final int BASE = 85;
    private static final int[] POW_85 =
            {BASE * BASE * BASE * BASE, BASE * BASE * BASE, BASE * BASE, BASE, 1};
    private static final char[] CHAR_MAP =
            "!\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstu"
                    .toCharArray();
    private final byte[] data;
    private final byte[] prefixBytes;
    private final byte[] suffixBytes;
    private final byte[] encoded;
    private boolean closed;
    private int dataSize;
    private boolean prefixDone;

    public ASCII85EncodeStream(OutputStream out, String prefix, String suffix) {
        super(out);
        prefixBytes = (prefix != null) ? prefix.getBytes() : "".getBytes();
        suffixBytes = (suffix != null) ? suffix.getBytes() : "".getBytes();
        data = new byte[4];
        encoded = new byte[5];
    }

    public ASCII85EncodeStream(OutputStream out) {
        this(out, "", "~>");
    }

    private static long toUInt32(byte[] bytes, int size) {
        long uint32 = 0L;
        for (int i = 0; i < 4 && i < size; i++) {
            uint32 |= (bytes[i] & 0xff) << (3 - i) * 8;
        }
        return toUnsignedInt(uint32);
    }

    private static long toUnsignedInt(long x) {
        return x & 0x00000000ffffffffL;
    }

    @Override
    public void write(int b) throws IOException {
        if (closed) {
            return;
        }
        if (!prefixDone) {
            out.write(prefixBytes);
            prefixDone = true;
        }
        if (dataSize == data.length) {
            writeChunk();
            dataSize = 0;
        }
        data[dataSize++] = (byte) (b & 0xff);
    }

    private void writeChunk() throws IOException {
        if (dataSize == 0) {
            return;
        }
        long uint32 = toUInt32(data, dataSize);
        int padByteCount = data.length - dataSize;
        int encodedSize = encodeChunk(uint32, padByteCount);
        out.write(encoded, 0, encodedSize);
    }

    private int encodeChunk(long uint32, int padByteCount) {
        Arrays.fill(encoded, (byte) 0);
        if (uint32 == 0L && padByteCount == 0) {
            encoded[0] = 'z';
            return 1;
        }
        int size = encoded.length - padByteCount;
        for (int i = 0; i < size; i++) {
            encoded[i] = (byte) CHAR_MAP[(int) (uint32 / POW_85[i] % BASE)];
        }
        return size;
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }

        writeChunk();

        out.write(suffixBytes);

        super.close();
        closed = true;
    }
}

