package org.xbib.io.http.client;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;

public class NoNullResponseTest extends AbstractBasicTest {
    private static final String GOOGLE_HTTPS_URL = "https://www.google.com";

    @Test(groups = "online", invocationCount = 4)
    public void multipleSslRequestsWithDelayAndKeepAlive() throws Exception {

        AsyncHttpClientConfig config = config()//
                .setFollowRedirect(true)//
                .setKeepAlive(true)//
                .setConnectTimeout(10000)//
                .setPooledConnectionIdleTimeout(60000)//
                .setRequestTimeout(10000)//
                .setMaxConnectionsPerHost(-1)//
                .setMaxConnections(-1)//
                .build();

        try (AsyncHttpClient client = asyncHttpClient(config)) {
            final BoundRequestBuilder builder = client.prepareGet(GOOGLE_HTTPS_URL);
            final Response response1 = builder.execute().get();
            Thread.sleep(4000);
            final Response response2 = builder.execute().get();
            assertNotNull(response1);
            assertNotNull(response2);
        }
    }
}
