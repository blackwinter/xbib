package org.xbib.io.http.client;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xbib.io.http.client.uri.Uri;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.TestUtils.TEXT_HTML_CONTENT_TYPE_WITH_UTF_8_CHARSET;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public class Relative302Test extends AbstractBasicTest {
    private final AtomicBoolean isSet = new AtomicBoolean(false);

    private static int getPort(Uri uri) {
        int port = uri.getPort();
        if (port == -1) {
            port = uri.getScheme().equals("http") ? 80 : 443;
        }
        return port;
    }

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        port2 = findFreePort();
        server = newJettyHttpServer(port1);
        server.setHandler(new Relative302Handler());
        server.start();
        logger.info("Local HTTP server started successfully");
    }

    @Test(groups = "online")
    public void testAllSequentiallyBecauseNotThreadSafe() throws Exception {
        redirected302Test();
        redirected302InvalidTest();
        absolutePathRedirectTest();
        relativePathRedirectTest();
    }

    // @Test(groups = "online")
    public void redirected302Test() throws Exception {
        isSet.getAndSet(false);

        try (AsyncHttpClient c = asyncHttpClient(config().setFollowRedirect(true))) {
            Response response = c.prepareGet(getTargetUrl()).setHeader("X-redirect", "http://www.google.com/").execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);

            String baseUrl = getBaseUrl(response.getUri());
            assertTrue(baseUrl.startsWith("http://www.google."), "response does not show redirection to a google subdomain, got " + baseUrl);
        }
    }

    // @Test(groups = "standalone")
    public void redirected302InvalidTest() throws Exception {
        isSet.getAndSet(false);

        // If the test hit a proxy, no ConnectException will be thrown and instead of 404 will be returned.
        try (AsyncHttpClient c = asyncHttpClient(config().setFollowRedirect(true))) {
            Response response = c.prepareGet(getTargetUrl()).setHeader("X-redirect", String.format("http://localhost:%d/", port2)).execute().get();

            assertNotNull(response);
            assertEquals(response.getStatusCode(), 404);
        } catch (ExecutionException ex) {
            assertEquals(ex.getCause().getClass(), ConnectException.class);
        }
    }

    // @Test(groups = "standalone")
    public void absolutePathRedirectTest() throws Exception {
        isSet.getAndSet(false);

        try (AsyncHttpClient c = asyncHttpClient(config().setFollowRedirect(true))) {
            String redirectTarget = "/bar/test";
            String destinationUrl = new URI(getTargetUrl()).resolve(redirectTarget).toString();

            Response response = c.prepareGet(getTargetUrl()).setHeader("X-redirect", redirectTarget).execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getUri().toString(), destinationUrl);

            logger.debug("{} was redirected to {}", redirectTarget, destinationUrl);
        }
    }

    // @Test(groups = "standalone")
    public void relativePathRedirectTest() throws Exception {
        isSet.getAndSet(false);

        try (AsyncHttpClient c = asyncHttpClient(config().setFollowRedirect(true))) {
            String redirectTarget = "bar/test1";
            String destinationUrl = new URI(getTargetUrl()).resolve(redirectTarget).toString();

            Response response = c.prepareGet(getTargetUrl()).setHeader("X-redirect", redirectTarget).execute().get();
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.getUri().toString(), destinationUrl);

            logger.debug("{} was redirected to {}", redirectTarget, destinationUrl);
        }
    }

    private String getBaseUrl(Uri uri) {
        String url = uri.toString();
        int port = uri.getPort();
        if (port == -1) {
            port = getPort(uri);
            url = url.substring(0, url.length() - 1) + ":" + port;
        }
        return url.substring(0, url.lastIndexOf(":") + String.valueOf(port).length() + 1);
    }

    private class Relative302Handler extends AbstractHandler {

        public void handle(String s, Request r, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            String param;
            httpResponse.setStatus(200);
            httpResponse.setContentType(TEXT_HTML_CONTENT_TYPE_WITH_UTF_8_CHARSET);
            Enumeration<?> e = httpRequest.getHeaderNames();
            while (e.hasMoreElements()) {
                param = e.nextElement().toString();

                if (param.startsWith("X-redirect") && !isSet.getAndSet(true)) {
                    httpResponse.addHeader("Location", httpRequest.getHeader(param));
                    httpResponse.setStatus(302);
                    break;
                }
            }
            httpResponse.setContentLength(0);
            httpResponse.getOutputStream().flush();
            httpResponse.getOutputStream().close();
        }
    }
}
