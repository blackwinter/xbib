package org.xbib.io.http.client;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
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
import static org.xbib.io.http.client.util.MiscUtils.isNonEmpty;

public class ParamEncodingTest extends AbstractBasicTest {

    @Test(groups = "standalone")
    public void testParameters() throws IOException, ExecutionException, TimeoutException, InterruptedException {

        String value = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKQLMNOPQRSTUVWXYZ1234567809`~!@#$%^&*()_+-=,.<>/?;:'\"[]{}\\| ";
        try (AsyncHttpClient client = asyncHttpClient()) {
            Future<Response> f = client.preparePost("http://localhost:" + port1).addFormParam("test", value).execute();
            Response resp = f.get(10, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getHeader("X-Param"), value.trim());
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new ParamEncoding();
    }

    private class ParamEncoding extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                String p = request.getParameter("test");
                if (isNonEmpty(p)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.addHeader("X-Param", p);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                }
            } else { // this handler is to handle POST request
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }
}
