package org.xbib.io.http.client;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;

public class ThreadNameTest extends AbstractBasicTest {

    private static Thread[] getThreads() {
        int count = Thread.activeCount() + 1;
        for (; ; ) {
            Thread[] threads = new Thread[count];
            int filled = Thread.enumerate(threads);
            if (filled < threads.length) {
                return Arrays.copyOf(threads, filled);
            }

            count *= 2;
        }
    }

    @Test(groups = "standalone")
    public void testThreadName() throws Exception {
        String threadPoolName = "ahc-" + (new Random().nextLong() & 0x7fffffffffffffffL);
        try (AsyncHttpClient client = asyncHttpClient(config().setThreadPoolName(threadPoolName))) {
            Future<Response> f = client.prepareGet("http://localhost:" + port1 + "/").execute();
            f.get(3, TimeUnit.SECONDS);

            // We cannot assert that all threads are created with specified name,
            // so we checking that at least one thread is.
            boolean found = false;
            for (Thread thread : getThreads()) {
                if (thread.getName().startsWith(threadPoolName)) {
                    found = true;
                    break;
                }
            }

            Assert.assertTrue(found, "must found threads starting with random string " + threadPoolName);
        }
    }
}
