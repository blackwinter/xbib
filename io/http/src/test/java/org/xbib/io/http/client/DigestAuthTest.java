package org.xbib.io.http.client;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.digestAuthRealm;
import static org.xbib.io.http.client.test.TestUtils.ADMIN;
import static org.xbib.io.http.client.test.TestUtils.USER;
import static org.xbib.io.http.client.test.TestUtils.addDigestAuthHandler;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public class DigestAuthTest extends AbstractBasicTest {

    @BeforeClass(alwaysRun = true)
    @Override
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();

        server = newJettyHttpServer(port1);
        addDigestAuthHandler(server, configureHandler());
        server.start();
        logger.info("Local HTTP server started successfully");
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new SimpleHandler();
    }

    @Test(groups = "standalone")
    public void digestAuthTest() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        try (AsyncHttpClient client = asyncHttpClient()) {
            Future<Response> f = client.prepareGet("http://localhost:" + port1 + "/")//
                    .setRealm(digestAuthRealm(USER, ADMIN).setRealmName("MyRealm").build())//
                    .execute();
            Response resp = f.get(60, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertNotNull(resp.getHeader("X-Auth"));
        }
    }

    @Test(groups = "standalone")
    public void digestAuthTestWithoutScheme() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        try (AsyncHttpClient client = asyncHttpClient()) {
            Future<Response> f = client.prepareGet("http://localhost:" + port1 + "/")//
                    .setRealm(digestAuthRealm(USER, ADMIN).setRealmName("MyRealm").build())//
                    .execute();
            Response resp = f.get(60, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertNotNull(resp.getHeader("X-Auth"));
        }
    }

    @Test(groups = "standalone")
    public void digestAuthNegativeTest() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        try (AsyncHttpClient client = asyncHttpClient()) {
            Future<Response> f = client.prepareGet("http://localhost:" + port1 + "/")//
                    .setRealm(digestAuthRealm("fake", ADMIN).build())//
                    .execute();
            Response resp = f.get(20, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), 401);
        }
    }

    private static class SimpleHandler extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

            response.addHeader("X-Auth", request.getHeader("Authorization"));
            response.setStatus(200);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }
}
