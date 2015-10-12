package org.xbib.io.redis;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

public class TimeTest {
    RedisClient client = new RedisClient();

    @BeforeClass
    public void setUp() throws Exception {
        client.setDefaultTimeout(15, TimeUnit.SECONDS);
    }

    @Test
    public void testTime() throws Exception {
        assertEquals(15000, client.makeTimeout());

    }
}
