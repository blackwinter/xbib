package org.xbib.io.redis;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


public class TimeTest {
    static RedisClient client = new RedisClient();

    @BeforeClass
    public static void setUp() throws Exception {
        client.setDefaultTimeout(15, TimeUnit.SECONDS);
    }

    @Test
    public void testTime() throws Exception {
        assertEquals(15000, client.makeTimeout());

    }
}
