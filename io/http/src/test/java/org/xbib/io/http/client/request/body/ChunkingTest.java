package org.xbib.io.http.client.request.body;

import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.DefaultAsyncHttpClientConfig;
import org.xbib.io.http.client.ListenableFuture;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.RequestBuilder;
import org.xbib.io.http.client.Response;
import org.xbib.io.http.client.request.body.generator.FeedableBodyGenerator;
import org.xbib.io.http.client.request.body.generator.InputStreamBodyGenerator;
import org.xbib.io.http.client.request.body.generator.UnboundedQueueFeedableBodyGenerator;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.Dsl.post;
import static org.xbib.io.http.client.test.TestUtils.LARGE_IMAGE_BYTES;
import static org.xbib.io.http.client.test.TestUtils.LARGE_IMAGE_FILE;

public class ChunkingTest extends AbstractBasicTest {

    // So we can just test the returned data is the image,
    // and doesn't contain the chunked delimeters.
    @Test(groups = "standalone")
    public void testBufferLargerThanFileWithStreamBodyGenerator() throws Throwable {
        doTestWithInputStreamBodyGenerator(new BufferedInputStream(new FileInputStream(LARGE_IMAGE_FILE), 400000));
    }

    @Test(groups = "standalone")
    public void testBufferSmallThanFileWithStreamBodyGenerator() throws Throwable {
        doTestWithInputStreamBodyGenerator(new BufferedInputStream(new FileInputStream(LARGE_IMAGE_FILE)));
    }

    @Test(groups = "standalone")
    public void testDirectFileWithStreamBodyGenerator() throws Throwable {
        doTestWithInputStreamBodyGenerator(new FileInputStream(LARGE_IMAGE_FILE));
    }

    @Test(groups = "standalone")
    public void testDirectFileWithFeedableBodyGenerator() throws Throwable {
        doTestWithFeedableBodyGenerator(new FileInputStream(LARGE_IMAGE_FILE));
    }

    public void doTestWithInputStreamBodyGenerator(InputStream is) throws Throwable {
        try (AsyncHttpClient c = asyncHttpClient(httpClientBuilder())) {

            RequestBuilder builder = post(getTargetUrl()).setBody(new InputStreamBodyGenerator(is));

            Request r = builder.build();

            final ListenableFuture<Response> responseFuture = c.executeRequest(r);
            waitForAndAssertResponse(responseFuture);
        }
    }

    public void doTestWithFeedableBodyGenerator(InputStream is) throws Throwable {
        try (AsyncHttpClient c = asyncHttpClient(httpClientBuilder())) {

            final FeedableBodyGenerator feedableBodyGenerator = new UnboundedQueueFeedableBodyGenerator();
            Request r = post(getTargetUrl()).setBody(feedableBodyGenerator).build();

            ListenableFuture<Response> responseFuture = c.executeRequest(r);

            feed(feedableBodyGenerator, is);

            waitForAndAssertResponse(responseFuture);
        }
    }

    private void feed(FeedableBodyGenerator feedableBodyGenerator, InputStream is) throws Exception {
        try (InputStream inputStream = is) {
            byte[] buffer = new byte[512];
            for (int i = 0; (i = inputStream.read(buffer)) > -1; ) {
                byte[] chunk = new byte[i];
                System.arraycopy(buffer, 0, chunk, 0, i);
                feedableBodyGenerator.feed(ByteBuffer.wrap(chunk), false);
            }
        }
        feedableBodyGenerator.feed(ByteBuffer.allocate(0), true);

    }

    private DefaultAsyncHttpClientConfig.Builder httpClientBuilder() {
        return config()//
                .setKeepAlive(true)//
                .setMaxConnectionsPerHost(1)//
                .setMaxConnections(1)//
                .setConnectTimeout(1000)//
                .setRequestTimeout(1000)//
                .setFollowRedirect(true);
    }

    private void waitForAndAssertResponse(ListenableFuture<Response> responseFuture) throws InterruptedException, java.util.concurrent.ExecutionException, IOException {
        Response response = responseFuture.get();
        if (500 == response.getStatusCode()) {
            StringBuilder sb = new StringBuilder();
            sb.append("==============\n");
            sb.append("500 response from call\n");
            sb.append("Headers:" + response.getHeaders() + "\n");
            sb.append("==============\n");
            logger.debug(sb.toString());
            assertEquals(response.getStatusCode(), 500, "Should have 500 status code");
            assertTrue(response.getHeader("X-Exception").contains("invalid.chunk.length"), "Should have failed due to chunking");
            fail("HARD Failing the test due to provided InputStreamBodyGenerator, chunking incorrectly:" + response.getHeader("X-Exception"));
        } else {
            assertEquals(response.getResponseBodyAsBytes(), LARGE_IMAGE_BYTES);
        }
    }
}
