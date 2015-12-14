package org.xbib.io.http.client.request.body;

import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.RequestBuilder;
import org.xbib.io.http.client.Response;
import org.xbib.io.http.client.request.body.generator.InputStreamBodyGenerator;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Future;

import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.Dsl.post;

public class BodyChunkTest extends AbstractBasicTest {

    private static final String MY_MESSAGE = "my message";

    @Test(groups = "standalone")
    public void negativeContentTypeTest() throws Exception {

        AsyncHttpClientConfig config = config()//
                .setConnectTimeout(100)//
                .setMaxConnections(50)//
                .setRequestTimeout(5 * 60 * 1000) // 5 minutes
                .build();

        try (AsyncHttpClient client = asyncHttpClient(config)) {
            RequestBuilder requestBuilder = post(getTargetUrl())//
                    .setHeader("Content-Type", "message/rfc822")//
                    .setBody(new InputStreamBodyGenerator(new ByteArrayInputStream(MY_MESSAGE.getBytes())));

            Future<Response> future = client.executeRequest(requestBuilder.build());

            System.out.println("waiting for response");
            Response response = future.get();
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getResponseBody(), MY_MESSAGE);
        }
    }
}
