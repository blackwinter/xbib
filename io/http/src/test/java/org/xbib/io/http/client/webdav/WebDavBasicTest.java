package org.xbib.io.http.client.webdav;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.apache.coyote.http11.Http11NioProtocol;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.RequestBuilder;
import org.xbib.io.http.client.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.delete;
import static org.xbib.io.http.client.Dsl.put;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;

public class WebDavBasicTest extends AbstractBasicTest {

    protected Embedded embedded;

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {

        port1 = findFreePort();
        embedded = new Embedded();
        String path = new File(".").getAbsolutePath();
        embedded.setCatalinaHome(path);

        Engine engine = embedded.createEngine();
        engine.setDefaultHost("localhost");

        Host host = embedded.createHost("localhost", path);
        engine.addChild(host);

        Context c = embedded.createContext("/", path);
        c.setReloadable(false);
        Wrapper w = c.createWrapper();
        w.addMapping("/*");
        w.setServletClass(org.apache.catalina.servlets.WebdavServlet.class.getName());
        w.addInitParameter("readonly", "false");
        w.addInitParameter("listings", "true");

        w.setLoadOnStartup(0);

        c.addChild(w);
        host.addChild(c);

        Connector connector = embedded.createConnector("localhost", port1, Http11NioProtocol.class.getName());
        connector.setContainer(host);
        embedded.addEngine(engine);
        embedded.addConnector(connector);
        embedded.start();
    }

    @AfterClass(alwaysRun = true)
    public void tearDownGlobal() throws InterruptedException, Exception {
        embedded.stop();
    }

    protected String getTargetUrl() {
        return String.format("http://localhost:%s/folder1", port1);
    }

    @AfterMethod(alwaysRun = true)
    // FIXME not sure that's threadsafe
    public void clean() throws InterruptedException, Exception {
        try (AsyncHttpClient c = asyncHttpClient()) {
            c.executeRequest(delete(getTargetUrl())).get();
        }
    }

    @Test(groups = "standalone")
    public void mkcolWebDavTest1() throws InterruptedException, IOException, ExecutionException {
        try (AsyncHttpClient c = asyncHttpClient()) {
            Request mkcolRequest = new RequestBuilder("MKCOL").setUrl(getTargetUrl()).build();
            Response response = c.executeRequest(mkcolRequest).get();
            assertEquals(response.getStatusCode(), 201);
        }
    }

    @Test(groups = "standalone")
    public void mkcolWebDavTest2() throws InterruptedException, IOException, ExecutionException {
        try (AsyncHttpClient c = asyncHttpClient()) {
            Request mkcolRequest = new RequestBuilder("MKCOL").setUrl(getTargetUrl() + "/folder2").build();
            Response response = c.executeRequest(mkcolRequest).get();
            assertEquals(response.getStatusCode(), 409);
        }
    }

    @Test(groups = "standalone")
    public void basicPropFindWebDavTest() throws InterruptedException, IOException, ExecutionException {
        try (AsyncHttpClient c = asyncHttpClient()) {
            Request propFindRequest = new RequestBuilder("PROPFIND").setUrl(getTargetUrl()).build();
            Response response = c.executeRequest(propFindRequest).get();

            assertEquals(response.getStatusCode(), 404);
        }
    }

    @Test(groups = "standalone")
    public void propFindWebDavTest() throws InterruptedException, IOException, ExecutionException {
        try (AsyncHttpClient c = asyncHttpClient()) {
            Request mkcolRequest = new RequestBuilder("MKCOL").setUrl(getTargetUrl()).build();
            Response response = c.executeRequest(mkcolRequest).get();
            assertEquals(response.getStatusCode(), 201);

            Request putRequest = put(String.format("http://localhost:%s/folder1/Test.txt", port1)).setBody("this is a test").build();
            response = c.executeRequest(putRequest).get();
            assertEquals(response.getStatusCode(), 201);

            Request propFindRequest = new RequestBuilder("PROPFIND").setUrl(String.format("http://localhost:%s/folder1/Test.txt", port1)).build();
            response = c.executeRequest(propFindRequest).get();

            assertEquals(response.getStatusCode(), 207);
            assertTrue(response.getResponseBody().contains("<status>HTTP/1.1 200 OK</status>"));
        }
    }

    @Test(groups = "standalone")
    public void propFindCompletionHandlerWebDavTest() throws InterruptedException, IOException, ExecutionException {
        try (AsyncHttpClient c = asyncHttpClient()) {
            Request mkcolRequest = new RequestBuilder("MKCOL").setUrl(getTargetUrl()).build();
            Response response = c.executeRequest(mkcolRequest).get();
            assertEquals(response.getStatusCode(), 201);

            Request propFindRequest = new RequestBuilder("PROPFIND").setUrl(getTargetUrl()).build();
            WebDavResponse webDavResponse = c.executeRequest(propFindRequest, new WebDavCompletionHandlerBase<WebDavResponse>() {
                @Override
                public void onThrowable(Throwable t) {

                    t.printStackTrace();
                }

                @Override
                public WebDavResponse onCompleted(WebDavResponse response) throws Exception {
                    return response;
                }
            }).get();

            assertNotNull(webDavResponse);
            assertEquals(webDavResponse.getStatusCode(), 200);
        }
    }
}
