package org.xbib.io.http.client.ws;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public abstract class AbstractBasicTest extends org.xbib.io.http.client.AbstractBasicTest {

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {

        port1 = findFreePort();
        server = newJettyHttpServer(port1);
        server.setHandler(getWebSocketHandler());

        server.start();
        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDownGlobal() throws Exception {
        server.stop();
    }

    protected String getTargetUrl() {
        return String.format("ws://localhost:%d/", port1);
    }

    public abstract WebSocketHandler getWebSocketHandler();
}
