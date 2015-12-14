package org.xbib.io.http.client;

import io.netty.handler.codec.http.HttpHeaders;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xbib.io.http.client.exception.RemotelyClosedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.basicAuthRealm;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.TestUtils.ADMIN;
import static org.xbib.io.http.client.test.TestUtils.USER;
import static org.xbib.io.http.client.test.TestUtils.addBasicAuthHandler;
import static org.xbib.io.http.client.test.TestUtils.addDigestAuthHandler;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public class AuthTimeoutTest extends AbstractBasicTest {

    private Server server2;

    @BeforeClass(alwaysRun = true)
    @Override
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        port2 = findFreePort();

        server = newJettyHttpServer(port1);
        addBasicAuthHandler(server, configureHandler());
        server.start();

        server2 = newJettyHttpServer(port2);
        addDigestAuthHandler(server2, configureHandler());
        server2.start();

        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDownGlobal() throws Exception {
        super.tearDownGlobal();
        server2.stop();
    }

    @Test(groups = "standalone", enabled = false)
    public void basicAuthTimeoutTest() throws Exception {
        try (AsyncHttpClient client = newClient()) {
            Future<Response> f = execute(client, server, false);
            f.get();
            fail("expected timeout");
        } catch (Exception e) {
            inspectException(e);
        }
    }

    @Test(groups = "standalone", enabled = false)
    public void basicPreemptiveAuthTimeoutTest() throws Exception {
        try (AsyncHttpClient client = newClient()) {
            Future<Response> f = execute(client, server, true);
            f.get();
            fail("expected timeout");
        } catch (Exception e) {
            inspectException(e);
        }
    }

    @Test(groups = "standalone", enabled = false)
    public void digestAuthTimeoutTest() throws Exception {
        try (AsyncHttpClient client = newClient()) {
            Future<Response> f = execute(client, server2, false);
            f.get();
            fail("expected timeout");
        } catch (Exception e) {
            inspectException(e);
        }
    }

    @Test(groups = "standalone", enabled = false)
    public void digestPreemptiveAuthTimeoutTest() throws Exception {
        try (AsyncHttpClient client = newClient()) {
            Future<Response> f = execute(client, server2, true);
            f.get();
            fail("expected timeout");
        } catch (Exception e) {
            inspectException(e);
        }
    }

    @Test(groups = "standalone", enabled = false)
    public void basicFutureAuthTimeoutTest() throws Exception {
        try (AsyncHttpClient client = newClient()) {
            Future<Response> f = execute(client, server, false);
            f.get(1, TimeUnit.SECONDS);
            fail("expected timeout");
        } catch (Exception e) {
            inspectException(e);
        }
    }

    @Test(groups = "standalone", enabled = false)
    public void basicFuturePreemptiveAuthTimeoutTest() throws Exception {
        try (AsyncHttpClient client = newClient()) {
            Future<Response> f = execute(client, server, true);
            f.get(1, TimeUnit.SECONDS);
            fail("expected timeout");
        } catch (Exception e) {
            inspectException(e);
        }
    }

    @Test(groups = "standalone", enabled = false)
    public void digestFutureAuthTimeoutTest() throws Exception {
        try (AsyncHttpClient client = newClient()) {
            Future<Response> f = execute(client, server2, false);
            f.get(1, TimeUnit.SECONDS);
            fail("expected timeout");
        } catch (Exception e) {
            inspectException(e);
        }
    }

    @Test(groups = "standalone", enabled = false)
    public void digestFuturePreemptiveAuthTimeoutTest() throws Exception {
        try (AsyncHttpClient client = newClient()) {
            Future<Response> f = execute(client, server2, true);
            f.get(1, TimeUnit.SECONDS);
            fail("expected timeout");
        } catch (Exception e) {
            inspectException(e);
        }
    }

    protected void inspectException(Throwable t) {
        assertEquals(t.getCause(), RemotelyClosedException.INSTANCE);
    }

    private AsyncHttpClient newClient() {
        return asyncHttpClient(config().setPooledConnectionIdleTimeout(2000).setConnectTimeout(20000).setRequestTimeout(2000));
    }

    protected Future<Response> execute(AsyncHttpClient client, Server server, boolean preemptive) throws IOException {
        return client.prepareGet(getTargetUrl()).setRealm(realm(preemptive)).setHeader("X-Content", "Test").execute();
    }

    private Realm realm(boolean preemptive) {
        return basicAuthRealm(USER, ADMIN).setUsePreemptiveAuth(preemptive).build();
    }

    @Override
    protected String getTargetUrl() {
        return "http://localhost:" + port1 + "/";
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new IncompleteResponseHandler();
    }

    private class IncompleteResponseHandler extends AbstractHandler {

        public void handle(String s, Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            // NOTE: handler sends less bytes than are given in Content-Length, which should lead to timeout

            OutputStream out = response.getOutputStream();
            if (request.getHeader("X-Content") != null) {
                String content = request.getHeader("X-Content");
                response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(content.getBytes(UTF_8).length));
                out.write(content.substring(1).getBytes(UTF_8));
            } else {
                response.setStatus(200);
            }
            out.flush();
            out.close();
        }
    }
}
