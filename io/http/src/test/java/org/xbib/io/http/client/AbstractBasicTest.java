package org.xbib.io.http.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.xbib.io.http.client.test.EchoHandler;

import static org.testng.Assert.fail;
import static org.xbib.io.http.client.test.TestUtils.addHttpConnector;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public abstract class AbstractBasicTest {

    protected final static Logger logger = LogManager.getLogger(AbstractBasicTest.class);

    protected final static int TIMEOUT = 30;

    protected Server server;
    protected int port1;
    protected int port2;

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {

        port1 = findFreePort();
        port2 = findFreePort();

        server = newJettyHttpServer(port1);
        server.setHandler(configureHandler());
        addHttpConnector(server, port2);
        server.start();
    }

    @AfterClass(alwaysRun = true)
    public void tearDownGlobal() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    protected String getTargetUrl() {
        return String.format("http://localhost:%d/foo/test", port1);
    }

    protected String getTargetUrl2() {
        return String.format("https://localhost:%d/foo/test", port2);
    }

    public AbstractHandler configureHandler() throws Exception {
        return new EchoHandler();
    }

    public static class AsyncCompletionHandlerAdapter extends AsyncCompletionHandler<Response> {

        @Override
        public Response onCompleted(Response response) throws Exception {
            return response;
        }

        @Override
        public void onThrowable(Throwable t) {
            t.printStackTrace();
            fail("Unexpected exception: " + t.getMessage(), t);
        }
    }

    public static class AsyncHandlerAdapter implements AsyncHandler<String> {

        @Override
        public void onThrowable(Throwable t) {
            t.printStackTrace();
            fail("Unexpected exception", t);
        }

        @Override
        public State onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
            return State.CONTINUE;
        }

        @Override
        public State onStatusReceived(final HttpResponseStatus responseStatus) throws Exception {
            return State.CONTINUE;
        }

        @Override
        public State onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
            return State.CONTINUE;
        }

        @Override
        public String onCompleted() throws Exception {
            return "";
        }
    }
}
