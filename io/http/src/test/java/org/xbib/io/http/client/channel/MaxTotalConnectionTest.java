package org.xbib.io.http.client.channel;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncCompletionHandlerBase;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.ListenableFuture;
import org.xbib.io.http.client.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertNull;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;

public class MaxTotalConnectionTest extends AbstractBasicTest {

    @Test(groups = "online")
    public void testMaxTotalConnectionsExceedingException() throws IOException {
        String[] urls = new String[]{"http://google.com", "http://github.com/"};

        AsyncHttpClientConfig config = config()//
                .setConnectTimeout(1000)//
                .setRequestTimeout(5000)//
                .setKeepAlive(false)//
                .setMaxConnections(1)//
                .setMaxConnectionsPerHost(1)//
                .build();

        try (AsyncHttpClient client = asyncHttpClient(config)) {
            List<ListenableFuture<Response>> futures = new ArrayList<>();
            for (int i = 0; i < urls.length; i++) {
                futures.add(client.prepareGet(urls[i]).execute());
            }

            boolean caughtError = false;
            int i;
            for (i = 0; i < urls.length; i++) {
                try {
                    futures.get(i).get();
                } catch (Exception e) {
                    // assert that 2nd request fails, because
                    // maxTotalConnections=1
                    caughtError = true;
                    break;
                }
            }

            Assert.assertEquals(1, i);
            Assert.assertTrue(caughtError);
        }
    }

    @Test(groups = "online")
    public void testMaxTotalConnections() throws Exception {
        String[] urls = new String[]{"http://google.com", "http://gatling.io"};

        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicReference<Throwable> ex = new AtomicReference<>();
        final AtomicReference<String> failedUrl = new AtomicReference<>();

        AsyncHttpClientConfig config = config()//
                .setConnectTimeout(1000)//
                .setRequestTimeout(5000)//
                .setKeepAlive(false)//
                .setMaxConnections(2)//
                .setMaxConnectionsPerHost(1)//
                .build();

        try (AsyncHttpClient client = asyncHttpClient(config)) {
            for (String url : urls) {
                final String thisUrl = url;
                client.prepareGet(url).execute(new AsyncCompletionHandlerBase() {
                    @Override
                    public Response onCompleted(Response response) throws Exception {
                        Response r = super.onCompleted(response);
                        latch.countDown();
                        return r;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        super.onThrowable(t);
                        ex.set(t);
                        failedUrl.set(thisUrl);
                        latch.countDown();
                    }
                });
            }

            latch.await();
            assertNull(ex.get());
            assertNull(failedUrl.get());
        }
    }
}
