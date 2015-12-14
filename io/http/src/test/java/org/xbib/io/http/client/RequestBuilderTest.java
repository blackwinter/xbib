package org.xbib.io.http.client;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.get;

public class RequestBuilderTest {

    private final static String SAFE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890-_~.";
    private final static String HEX_CHARS = "0123456789ABCDEF";

    @Test(groups = "standalone")
    public void testEncodesQueryParameters() throws UnsupportedEncodingException {
        String[] values = new String[]{"abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKQLMNOPQRSTUVWXYZ", "1234567890", "1234567890", "`~!@#$%^&*()", "`~!@#$%^&*()", "_+-=,.<>/?",
                "_+-=,.<>/?", ";:'\"[]{}\\| ", ";:'\"[]{}\\| "};

        /*
         * as per RFC-5849 (Oauth), and RFC-3986 (percent encoding) we MUST
         * encode everything except for "safe" characters; and nothing but them.
         * Safe includes ascii letters (upper and lower case), digits (0 - 9)
         * and FOUR special characters: hyphen ('-'), underscore ('_'), tilde
         * ('~') and period ('.')). Everything else must be percent-encoded,
         * byte-by-byte, using UTF-8 encoding (meaning three-byte Unicode/UTF-8
         * code points are encoded as three three-letter percent-encode
         * entities).
         */
        for (String value : values) {
            RequestBuilder builder = get("http://example.com/").addQueryParam("name", value);

            StringBuilder sb = new StringBuilder();
            for (int i = 0, len = value.length(); i < len; ++i) {
                char c = value.charAt(i);
                if (SAFE_CHARS.indexOf(c) >= 0) {
                    sb.append(c);
                } else {
                    int hi = (c >> 4);
                    int lo = c & 0xF;
                    sb.append('%').append(HEX_CHARS.charAt(hi)).append(HEX_CHARS.charAt(lo));
                }
            }
            String expValue = sb.toString();
            Request request = builder.build();
            assertEquals(request.getUrl(), "http://example.com/?name=" + expValue);
        }
    }

    @Test(groups = "standalone")
    public void testChaining() throws IOException, ExecutionException, InterruptedException {
        Request request = get("http://foo.com").addQueryParam("x", "value").build();

        Request request2 = new RequestBuilder(request).build();

        assertEquals(request2.getUri(), request.getUri());
    }

    @Test(groups = "standalone")
    public void testParsesQueryParams() throws IOException, ExecutionException, InterruptedException {
        Request request = get("http://foo.com/?param1=value1").addQueryParam("param2", "value2").build();

        assertEquals(request.getUrl(), "http://foo.com/?param1=value1&param2=value2");
        List<Param> params = request.getQueryParams();
        assertEquals(params.size(), 2);
        assertEquals(params.get(0), new Param("param1", "value1"));
        assertEquals(params.get(1), new Param("param2", "value2"));
    }

    @Test(groups = "standalone")
    public void testUserProvidedRequestMethod() {
        Request req = new RequestBuilder("ABC").setUrl("http://foo.com").build();
        assertEquals(req.getMethod(), "ABC");
        assertEquals(req.getUrl(), "http://foo.com");
    }

    @Test(groups = "standalone")
    public void testPercentageEncodedUserInfo() {
        final Request req = get("http://hello:wor%20ld@foo.com").build();
        assertEquals(req.getMethod(), "GET");
        assertEquals(req.getUrl(), "http://hello:wor%20ld@foo.com");
    }

    @Test(groups = "standalone")
    public void testContentTypeCharsetToBodyEncoding() {
        final Request req = get("http://localhost").setHeader("Content-Type", "application/json; charset=utf-8").build();
        assertEquals(req.getCharset(), UTF_8);
        final Request req2 = get("http://localhost").setHeader("Content-Type", "application/json; charset=\"utf-8\"").build();
        assertEquals(req2.getCharset(), UTF_8);
    }
}
