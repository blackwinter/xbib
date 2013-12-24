package org.mapdb;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class StoreTest {

    @Test
    public void compression() {
        Store s = (Store) DBMaker.newMemoryDB()
                .cacheDisable()
                .transactionDisable()
                .compressionEnable()
                .makeEngine();

        long size = s.getCurrSize();
        long recid = s.put(new byte[10000], Serializer.BYTE_ARRAY);
        assertTrue(s.getCurrSize() - size < 200);
        assertArrayEquals(new byte[10000], s.get(recid, Serializer.BYTE_ARRAY));
    }


    @Test
    public void compression_random() {
        Random r = new Random();

        for (int i = 100; i < 100000; i = i * 2) {
            Store s = (Store) DBMaker.newMemoryDB()
                    .cacheDisable()
                    .transactionDisable()
                    .compressionEnable()
                    .makeEngine();

            long size = s.getCurrSize();
            byte[] b = new byte[i];
            r.nextBytes(b);
            //grow so there is something to compress
            b = Arrays.copyOfRange(b, 0, i);
            b = Arrays.copyOf(b, i * 5);
            long recid = s.put(b, Serializer.BYTE_ARRAY);
            assertTrue(s.getCurrSize() - size < i * 2 + 100);
            assertArrayEquals(b, s.get(recid, Serializer.BYTE_ARRAY));
        }
    }


}
