package org.xbib.io.http.client;

import org.testng.annotations.BeforeClass;

import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpsServer;

public abstract class AbstractBasicHttpsTest extends AbstractBasicTest {

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        server = newJettyHttpsServer(port1);
        server.setHandler(configureHandler());
        server.start();
        logger.info("Local HTTP server started successfully");
    }
}
