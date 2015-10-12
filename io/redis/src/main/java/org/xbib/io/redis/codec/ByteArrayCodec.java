package org.xbib.io.redis.codec;

import java.nio.ByteBuffer;

/**
 * A {@link RedisCodec} that uses plain byte arrays.
 */
public class ByteArrayCodec extends RedisCodec<byte[], byte[]> {

    /**
     * Static held instance ready to use. The {@link ByteArrayCodec} is thread-safe.
     */
    public final static ByteArrayCodec INSTANCE = new ByteArrayCodec();

    private static byte[] getBytes(ByteBuffer buffer) {
        byte[] b = new byte[buffer.remaining()];
        buffer.get(b);
        return b;
    }

    @Override
    public byte[] decodeKey(ByteBuffer bytes) {
        return getBytes(bytes);
    }

    @Override
    public byte[] decodeValue(ByteBuffer bytes) {
        return getBytes(bytes);
    }

    @Override
    public byte[] encodeKey(byte[] key) {
        return key;
    }

    @Override
    public byte[] encodeValue(byte[] value) {
        return value;
    }
}
