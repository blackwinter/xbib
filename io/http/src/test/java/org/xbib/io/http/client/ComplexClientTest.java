package org.xbib.io.http.client;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;

public class ComplexClientTest extends AbstractBasicTest {

    @Test(groups = "standalone")
    public void multipleRequestsTest() throws Exception {
        try (AsyncHttpClient c = asyncHttpClient()) {
            String body = "hello there";

            // once
            Response response = c.preparePost(getTargetUrl()).setBody(body).setHeader("Content-Type", "text/html").execute().get(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(response.getResponseBody(), body);

            // twice
            response = c.preparePost(getTargetUrl()).setBody(body).setHeader("Content-Type", "text/html").execute().get(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(response.getResponseBody(), body);
        }
    }

    @Test(groups = "standalone")
    public void urlWithoutSlashTest() throws Exception {
        try (AsyncHttpClient c = asyncHttpClient()) {
            String body = "hello there";
            Response response = c.preparePost(String.format("http://localhost:%d/foo/test", port1)).setBody(body).setHeader("Content-Type", "text/html").execute().get(TIMEOUT, TimeUnit.SECONDS);
            assertEquals(response.getResponseBody(), body);
        }
    }
}
