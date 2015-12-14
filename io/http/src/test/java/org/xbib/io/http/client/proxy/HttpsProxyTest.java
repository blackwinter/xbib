package org.xbib.io.http.client.proxy;

import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.RequestBuilder;
import org.xbib.io.http.client.Response;
import org.xbib.io.http.client.test.EchoHandler;

import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.Dsl.get;
import static org.xbib.io.http.client.Dsl.proxyServer;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpsServer;

public class HttpsProxyTest extends AbstractBasicTest {

    private Server server2;

    public AbstractHandler configureHandler() throws Exception {
        return new ConnectHandler();
    }

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        server = newJettyHttpServer(port1);
        server.setHandler(configureHandler());
        server.start();

        port2 = findFreePort();

        server2 = newJettyHttpsServer(port2);
        server2.setHandler(new EchoHandler());
        server2.start();

        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDownGlobal() throws Exception {
        server.stop();
        server2.stop();
    }

    @Test(groups = "standalone")
    public void testRequestProxy() throws Exception {

        try (AsyncHttpClient asyncHttpClient = asyncHttpClient(config().setFollowRedirect(true).setAcceptAnyCertificate(true))) {
            RequestBuilder rb = get(getTargetUrl2()).setProxyServer(proxyServer("localhost", port1));
            Response r = asyncHttpClient.executeRequest(rb.build()).get();
            assertEquals(r.getStatusCode(), 200);
        }
    }

    @Test(groups = "standalone")
    public void testConfigProxy() throws Exception {
        AsyncHttpClientConfig config = config()//
                .setFollowRedirect(true)//
                .setProxyServer(proxyServer("localhost", port1).build())//
                .setAcceptAnyCertificate(true)//
                .build();
        try (AsyncHttpClient asyncHttpClient = asyncHttpClient(config)) {
            Response r = asyncHttpClient.executeRequest(get(getTargetUrl2())).get();
            assertEquals(r.getStatusCode(), 200);
        }
    }

    @Test(groups = "standalone")
    public void testPooledConnectionsWithProxy() throws Exception {

        try (AsyncHttpClient asyncHttpClient = asyncHttpClient(config().setFollowRedirect(true).setAcceptAnyCertificate(true).setKeepAlive(true))) {
            RequestBuilder rb = get(getTargetUrl2()).setProxyServer(proxyServer("localhost", port1));

            Response r1 = asyncHttpClient.executeRequest(rb.build()).get();
            assertEquals(r1.getStatusCode(), 200);

            Response r2 = asyncHttpClient.executeRequest(rb.build()).get();
            assertEquals(r2.getStatusCode(), 200);
        }
    }
}
