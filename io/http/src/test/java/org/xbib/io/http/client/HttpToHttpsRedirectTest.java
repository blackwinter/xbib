package org.xbib.io.http.client;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.TestUtils.TEXT_HTML_CONTENT_TYPE_WITH_UTF_8_CHARSET;
import static org.xbib.io.http.client.test.TestUtils.addHttpsConnector;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public class HttpToHttpsRedirectTest extends AbstractBasicTest {

    // FIXME super NOT threadsafe!!!
    private final AtomicBoolean redirectDone = new AtomicBoolean(false);

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        port2 = findFreePort();

        server = newJettyHttpServer(port1);
        addHttpsConnector(server, port2);
        server.setHandler(new Relative302Handler());
        server.start();
        logger.info("Local HTTP server started successfully");
    }

    @Test(groups = "standalone")
    // FIXME find a way to make this threadsafe, other, set @Test(singleThreaded = true)
    public void runAllSequentiallyBecauseNotThreadSafe() throws Exception {
        httpToHttpsRedirect();
        httpToHttpsProperConfig();
        relativeLocationUrl();
    }

    // @Test(groups = "standalone")
    public void httpToHttpsRedirect() throws Exception {
        redirectDone.getAndSet(false);

        AsyncHttpClientConfig cg = config()//
                .setMaxRedirects(5)//
                .setFollowRedirect(true)//
                .setAcceptAnyCertificate(true)//
                .build();
        try (AsyncHttpClient c = asyncHttpClient(cg)) {
            Response response = c.prepareGet(getTargetUrl()).setHeader("X-redirect", getTargetUrl2()).execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getHeader("X-httpToHttps"), "PASS");
        }
    }

    // @Test(groups = "standalone")
    public void httpToHttpsProperConfig() throws Exception {
        redirectDone.getAndSet(false);

        AsyncHttpClientConfig cg = config()//
                .setMaxRedirects(5)//
                .setFollowRedirect(true)//
                .setAcceptAnyCertificate(true)//
                .build();
        try (AsyncHttpClient c = asyncHttpClient(cg)) {
            Response response = c.prepareGet(getTargetUrl()).setHeader("X-redirect", getTargetUrl2() + "/test2").execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getHeader("X-httpToHttps"), "PASS");

            // Test if the internal channel is downgraded to clean http.
            response = c.prepareGet(getTargetUrl()).setHeader("X-redirect", getTargetUrl2() + "/foo2").execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getHeader("X-httpToHttps"), "PASS");
        }
    }

    // @Test(groups = "standalone")
    public void relativeLocationUrl() throws Exception {
        redirectDone.getAndSet(false);

        AsyncHttpClientConfig cg = config()//
                .setMaxRedirects(5)//
                .setFollowRedirect(true)//
                .setAcceptAnyCertificate(true)//
                .build();
        try (AsyncHttpClient c = asyncHttpClient(cg)) {
            Response response = c.prepareGet(getTargetUrl()).setHeader("X-redirect", "/foo/test").execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getUri().toString(), getTargetUrl());
        }
    }

    private class Relative302Handler extends AbstractHandler {

        public void handle(String s, Request r, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            String param;
            httpResponse.setContentType(TEXT_HTML_CONTENT_TYPE_WITH_UTF_8_CHARSET);
            Enumeration<?> e = httpRequest.getHeaderNames();
            while (e.hasMoreElements()) {
                param = e.nextElement().toString();

                if (param.startsWith("X-redirect") && !redirectDone.getAndSet(true)) {
                    httpResponse.addHeader("Location", httpRequest.getHeader(param));
                    httpResponse.setStatus(302);
                    httpResponse.getOutputStream().flush();
                    httpResponse.getOutputStream().close();
                    return;
                }
            }

            if (r.getScheme().equalsIgnoreCase("https")) {
                httpResponse.addHeader("X-httpToHttps", "PASS");
                redirectDone.getAndSet(false);
            }

            httpResponse.setStatus(200);
            httpResponse.getOutputStream().flush();
            httpResponse.getOutputStream().close();
        }
    }
}
