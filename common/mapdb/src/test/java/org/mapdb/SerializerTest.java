package org.mapdb;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializerTest {

    @Test
    public void UUID2() {
        UUID u = UUID.randomUUID();
        assertEquals(u, Utils.clone(u, Serializer.UUID));
    }

    @Test
    public void string_ascii() {
        String s = "adas9 asd9009asd";
        assertEquals(s, Utils.clone(s, Serializer.STRING_ASCII));
        s = "";
        assertEquals(s, Utils.clone(s, Serializer.STRING_ASCII));
        s = "    ";
        assertEquals(s, Utils.clone(s, Serializer.STRING_ASCII));
    }

    @Test
    public void compression_wrapper() throws IOException {
        byte[] b = new byte[100];
        new Random().nextBytes(b);
        Serializer<byte[]> ser = new Serializer.CompressionWrapper(Serializer.BYTE_ARRAY);
        assertArrayEquals(b, Utils.clone(b, ser));

        b = Arrays.copyOf(b, 10000);
        assertArrayEquals(b, Utils.clone(b, ser));

        DataOutput2 out = new DataOutput2();
        ser.serialize(out, b);
        assertTrue(out.pos < 1000);
    }
}
