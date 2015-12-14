package org.xbib.io.http.client;

import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;

public class ListenableFutureTest extends AbstractBasicTest {

    @Test(groups = "standalone")
    public void testListenableFuture() throws Exception {
        final AtomicInteger statusCode = new AtomicInteger(500);
        try (AsyncHttpClient ahc = asyncHttpClient()) {
            final CountDownLatch latch = new CountDownLatch(1);
            final ListenableFuture<Response> future = ahc.prepareGet(getTargetUrl()).execute();
            future.addListener(new Runnable() {

                public void run() {
                    try {
                        statusCode.set(future.get().getStatusCode());
                        latch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }, Executors.newFixedThreadPool(1));

            latch.await(10, TimeUnit.SECONDS);
            assertEquals(statusCode.get(), 200);
        }
    }
}
