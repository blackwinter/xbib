package org.xbib.io.http.client;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public class RC10KTest extends AbstractBasicTest {
    private static final int C10K = 1000;
    private static final String ARG_HEADER = "Arg";
    private static final int SRV_COUNT = 10;
    protected List<Server> servers = new ArrayList<>(SRV_COUNT);
    private int[] ports;

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        ports = new int[SRV_COUNT];
        for (int i = 0; i < SRV_COUNT; i++) {
            ports[i] = createServer();
        }
        logger.info("Local HTTP servers started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDownGlobal() throws Exception {
        for (Server srv : servers) {
            srv.stop();
        }
    }

    private int createServer() throws Exception {
        int port = findFreePort();
        Server srv = newJettyHttpServer(port);
        srv.setHandler(configureHandler());
        srv.start();
        servers.add(srv);
        return port;
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new AbstractHandler() {
            public void handle(String s, org.eclipse.jetty.server.Request r, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
                resp.setContentType("text/plain");
                String arg = s.substring(1);
                resp.setHeader(ARG_HEADER, arg);
                resp.setStatus(200);
                resp.getOutputStream().print(arg);
                resp.getOutputStream().flush();
                resp.getOutputStream().close();
            }
        };
    }

    @Test(timeOut = 10 * 60 * 1000, groups = "scalability")
    public void rc10kProblem() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        try (AsyncHttpClient ahc = asyncHttpClient(config().setMaxConnectionsPerHost(C10K).setKeepAlive(true))) {
            List<Future<Integer>> resps = new ArrayList<>(C10K);
            int i = 0;
            while (i < C10K) {
                resps.add(ahc.prepareGet(String.format("http://localhost:%d/%d", ports[i % SRV_COUNT], i)).execute(new MyAsyncHandler(i++)));
            }
            i = 0;
            for (Future<Integer> fResp : resps) {
                Integer resp = fResp.get();
                assertNotNull(resp);
                assertEquals(resp.intValue(), i++);
            }
        }
    }

    private class MyAsyncHandler implements AsyncHandler<Integer> {
        private String arg;
        private AtomicInteger result = new AtomicInteger(-1);

        public MyAsyncHandler(int i) {
            arg = String.format("%d", i);
        }

        public void onThrowable(Throwable t) {
            logger.warn("onThrowable called.", t);
        }

        public State onBodyPartReceived(HttpResponseBodyPart event) throws Exception {
            String s = new String(event.getBodyPartBytes());
            result.compareAndSet(-1, new Integer(s.trim().equals("") ? "-1" : s));
            return State.CONTINUE;
        }

        public State onStatusReceived(HttpResponseStatus event) throws Exception {
            assertEquals(event.getStatusCode(), 200);
            return State.CONTINUE;
        }

        public State onHeadersReceived(HttpResponseHeaders event) throws Exception {
            assertEquals(event.getHeaders().get(ARG_HEADER), arg);
            return State.CONTINUE;
        }

        public Integer onCompleted() throws Exception {
            return result.get();
        }
    }
}
