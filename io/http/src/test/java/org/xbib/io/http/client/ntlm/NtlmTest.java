package org.xbib.io.http.client.ntlm;

import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.Realm;
import org.xbib.io.http.client.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.Dsl.get;
import static org.xbib.io.http.client.Dsl.ntlmAuthRealm;

public class NtlmTest extends AbstractBasicTest {

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new NTLMHandler();
    }

    private Realm.Builder realmBuilderBase() {
        return ntlmAuthRealm("Zaphod", "Beeblebrox")//
                .setNtlmDomain("Ursa-Minor")//
                .setNtlmHost("LightCity");
    }

    private void ntlmAuthTest(Realm.Builder realmBuilder) throws IOException, InterruptedException, ExecutionException {

        try (AsyncHttpClient client = asyncHttpClient(config().setRealm(realmBuilder))) {
            Future<Response> responseFuture = client.executeRequest(get(getTargetUrl()));
            int status = responseFuture.get().getStatusCode();
            Assert.assertEquals(status, 200);
        }
    }

    // diasbled @Test(groups = "standalone")
    public void lazyNTLMAuthTest() throws IOException, InterruptedException, ExecutionException {
        ntlmAuthTest(realmBuilderBase());
    }

    // disabled @Test(groups = "standalone")
    public void preemptiveNTLMAuthTest() throws IOException, InterruptedException, ExecutionException {
        ntlmAuthTest(realmBuilderBase().setUsePreemptiveAuth(true));
    }

    public static class NTLMHandler extends AbstractHandler {

        @Override
        public void handle(String pathInContext, org.eclipse.jetty.server.Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
                ServletException {

            String authorization = httpRequest.getHeader("Authorization");
            if (authorization == null) {
                httpResponse.setStatus(401);
                httpResponse.setHeader("WWW-Authenticate", "NTLM");
            } else if (authorization.equals("NTLM TlRMTVNTUAABAAAAAYIIogAAAAAoAAAAAAAAACgAAAAFASgKAAAADw==")) {
                httpResponse.setStatus(401);
                httpResponse.setHeader("WWW-Authenticate", "NTLM TlRMTVNTUAACAAAAAAAAACgAAAABggAAU3J2Tm9uY2UAAAAAAAAAAA==");
            } else if (authorization.equals("NTLM TlRMTVNTUAADAAAAGAAYAEgAAAAYABgAYAAAABQAFAB4AAAADAAMAIwAAAASABIAmAAAAAAAAACqAAAAAYIAAgUBKAoAAAAPrYfKbe/jRoW5xDxHeoxC1gBmfWiS5+iX4OAN4xBKG/IFPwfH3agtPEia6YnhsADTVQBSAFMAQQAtAE0ASQBOAE8AUgBaAGEAcABoAG8AZABMAGkAZwBoAHQAQwBpAHQAeQA=")) {
                httpResponse.setStatus(200);
            } else {
                httpResponse.setStatus(401);
            }
            httpResponse.setContentLength(0);
            httpResponse.getOutputStream().flush();
            httpResponse.getOutputStream().close();
        }
    }
}
