
package org.xbib.io.redis;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.Test;

import org.xbib.io.redis.codec.ByteArrayCodec;
import org.xbib.io.redis.codec.CompressionCodec;
import org.xbib.io.redis.codec.RedisCodec;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomCodecTest extends AbstractCommandTest {
    @Test
    public void test() throws Exception {
        RedisConnection<String, Object> connection = client.connect(new SerializedObjectCodec());
        List<String> list = list("one", "two");
        connection.set(key, list);
        assertThat(connection.get(key)).isEqualTo(list);

        connection.close();
    }

    @Test
    public void testDeflateCompressedJavaSerializer() throws Exception {
        RedisConnection<String, Object> connection = client.connect(CompressionCodec.valueCompressor(
                new SerializedObjectCodec(), CompressionCodec.CompressionType.DEFLATE));
        List<String> list = list("one", "two");
        connection.set(key, list);
        assertThat(connection.get(key)).isEqualTo(list);

        connection.close();
    }

    @Test
    public void testGzipompressedJavaSerializer() throws Exception {
        RedisConnection<String, Object> connection = client.connect(CompressionCodec.valueCompressor(
                new SerializedObjectCodec(), CompressionCodec.CompressionType.GZIP));
        List<String> list = list("one", "two");
        connection.set(key, list);
        assertThat(connection.get(key)).isEqualTo(list);

        connection.close();
    }

    @Test
    public void testByteCodec() throws Exception {
        RedisConnection<byte[], byte[]> connection = client.connect(new ByteArrayCodec());
        String value = "üöäü+#";
        connection.set(key.getBytes(), value.getBytes());
        assertThat(connection.get(key.getBytes())).isEqualTo(value.getBytes());

        List<byte[]> keys = connection.keys(key.getBytes());
        assertThat(keys).contains(key.getBytes());
    }

    public class SerializedObjectCodec extends RedisCodec<String, Object> {
        private Charset charset = Charset.forName("UTF-8");

        @Override
        public String decodeKey(ByteBuffer bytes) {
            return charset.decode(bytes).toString();
        }

        @Override
        public Object decodeValue(ByteBuffer bytes) {
            try {
                byte[] array = new byte[bytes.remaining()];
                bytes.get(array);
                ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(array));
                return is.readObject();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public byte[] encodeKey(String key) {
            return charset.encode(key).array();
        }

        @Override
        public byte[] encodeValue(Object value) {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(bytes);
                os.writeObject(value);
                return bytes.toByteArray();
            } catch (IOException e) {
                return null;
            }
        }
    }
}
