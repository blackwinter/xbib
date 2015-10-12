
package org.xbib.io.redis;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.xbib.io.redis.codec.Utf8StringCodec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BitCommandTest extends AbstractCommandTest {
    protected RedisConnection<String, String> bitstring;

    @Before
    public final void openBitStringConnection() throws Exception {
        bitstring = client.connect(new BitStringCodec());
    }

    @After
    public final void closeBitStringConnection() throws Exception {
        bitstring.close();
    }

    @Test
    public void bitcount() throws Exception {
        assertThat(redis.bitcount(key), equalTo(0));
        redis.setbit(key, 0, 1);
        redis.setbit(key, 1, 1);
        redis.setbit(key, 2, 1);
        assertThat((long) redis.bitcount(key), equalTo(3));
        // assertThat(1).isEqualTo(2, (long) redis.bitcount(key, offset(3)));
        assertThat(redis.bitcount(key, 3, -1), equalTo(0));
    }

    @Test
    public void bitpos() throws Exception {
        assertThat(redis.bitcount(key), equalTo(0));
        redis.setbit(key, 0, 0);
        redis.setbit(key, 1, 1);
        assertThat(bitstring.get(key), equalTo("00000010"));
        assertThat(redis.bitpos(key, true), equalTo(1));
    }

    @Test
    public void bitposOffset() throws Exception {
        assertThat(redis.bitcount(key), equalTo(0));
        redis.setbit(key, 0, 1);
        redis.setbit(key, 1, 1);
        redis.setbit(key, 2, 0);
        redis.setbit(key, 3, 0);
        redis.setbit(key, 4, 0);
        redis.setbit(key, 5, 1);
        assertThat(bitstring.getbit(key, 1), equalTo(1));
        assertThat(bitstring.getbit(key, 4), equalTo(0));
        assertThat(bitstring.getbit(key, 5), equalTo(1));
        assertThat(bitstring.get(key), equalTo("00100011"));
        assertThat(redis.bitpos(key, false, 0, 0), equalTo(2));
    }

    @Test
    public void bitopAnd() throws Exception {
        redis.setbit("foo", 0, 1);
        redis.setbit("bar", 1, 1);
        redis.setbit("baz", 2, 1);
        assertThat(redis.bitopAnd(key, "foo", "bar", "baz"), equalTo(1));
        assertThat((long) redis.bitcount(key), equalTo(0));
        assertThat(bitstring.get(key), equalTo("00000000"));
    }

    @Test
    public void bitopNot() throws Exception {
        redis.setbit("foo", 0, 1);
        redis.setbit("foo", 2, 1);

        assertThat(redis.bitopNot(key, "foo"), equalTo(1));
        assertThat((long) redis.bitcount(key), equalTo(6));
        assertThat(bitstring.get(key), equalTo("11111010"));
    }

    @Test
    public void bitopOr() throws Exception {
        redis.setbit("foo", 0, 1);
        redis.setbit("bar", 1, 1);
        redis.setbit("baz", 2, 1);
        assertThat(redis.bitopOr(key, "foo", "bar", "baz"), equalTo(1));
        assertThat(bitstring.get(key), equalTo("00000111"));
    }

    @Test
    public void bitopXor() throws Exception {
        redis.setbit("foo", 0, 1);
        redis.setbit("bar", 0, 1);
        redis.setbit("baz", 2, 1);
        assertThat(redis.bitopXor(key, "foo", "bar", "baz"), equalTo(1));
        assertThat(bitstring.get(key), equalTo("00000100"));
    }

    @Test
    public void getbit() throws Exception {
        assertThat(redis.getbit(key, 0), equalTo(0));
        redis.setbit(key, 0, 1);
        assertThat(redis.getbit(key, 0), equalTo(1));
    }

    @Test
    public void setbit() throws Exception {
        assertThat(redis.setbit(key, 0, 1), equalTo(0));
        assertThat(redis.setbit(key, 0, 0), equalTo(1));
    }

    static class BitStringCodec extends Utf8StringCodec {
        @Override
        public String decodeValue(ByteBuffer bytes) {
            StringBuilder bits = new StringBuilder(bytes.remaining() * 8);
            while (bytes.remaining() > 0) {
                byte b = bytes.get();
                for (int i = 0; i < 8; i++) {
                    bits.append(Integer.valueOf(b >>> i & 1));
                }
            }
            return bits.toString();
        }
    }
}
