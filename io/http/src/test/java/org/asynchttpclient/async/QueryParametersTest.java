package org.asynchttpclient.async;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.asynchttpclient.util.MiscUtil.isNonEmpty;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Testing query parameters support.
 * 
 */
public abstract class QueryParametersTest extends AbstractBasicTest {
    private class QueryStringHandler extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                String qs = request.getQueryString();
                if (isNonEmpty(qs)) {
                    for (String qnv : qs.split("&")) {
                        String nv[] = qnv.split("=");
                        response.addHeader(nv[0], nv[1]);
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                }
            } else { // this handler is to handle POST request
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            r.setHandled(true);
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new QueryStringHandler();
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testQueryParameters() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        AsyncHttpClient client = getAsyncHttpClient(null);
        try {
            Future<Response> f = client.prepareGet("http://127.0.0.1:" + port1).addQueryParameter("a", "1").addQueryParameter("b", "2").execute();
            Response resp = f.get(3, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getHeader("a"), "1");
            assertEquals(resp.getHeader("b"), "2");
        } finally {
            client.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testUrlRequestParametersEncoding() throws IOException, ExecutionException, InterruptedException {
        String URL = getTargetUrl() + "?q=";
        String REQUEST_PARAM = "github github \ngithub";

        AsyncHttpClient client = getAsyncHttpClient(null);
        try {
            String requestUrl2 = URL + URLEncoder.encode(REQUEST_PARAM, "UTF-8");
            LoggerFactory.getLogger(QueryParametersTest.class).info("Executing request [{}] ...", requestUrl2);
            Response response = client.prepareGet(requestUrl2).execute().get();
            String s = URLDecoder.decode(response.getHeader("q"), "UTF-8");
            assertEquals(s, REQUEST_PARAM);
        } finally {
            client.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void urlWithColonTest_Netty() throws Exception {
        AsyncHttpClient c = getAsyncHttpClient(null);
        try {
            String query = "test:colon:";
            Response response = c.prepareGet(String.format("http://127.0.0.1:%d/foo/test/colon?q=%s", port1, query)).setHeader("Content-Type", "text/html").execute().get(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(response.getHeader("q"), URLEncoder.encode(query, "UTF-8"));
        } finally {
            c.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void urlWithColonTest_JDK() throws Exception {
        AsyncHttpClient c = getAsyncHttpClient(null);
        try {
            String query = "test:colon:";
            Response response = c.prepareGet(String.format("http://127.0.0.1:%d/foo/test/colon?q=%s", port1, query)).setHeader("Content-Type", "text/html").execute().get(TIMEOUT, TimeUnit.SECONDS);

            assertEquals(response.getHeader("q"), URLEncoder.encode(query, "UTF-8"));
        } finally {
            c.close();
        }
    }
}