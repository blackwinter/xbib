package org.xbib.io.http.client.cookie;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class CookieDecoderTest {

    @Test(groups = "standalone")
    public void testDecodeUnquoted() {
        Cookie cookie = CookieDecoder.decode("foo=value; domain=/; path=/");
        assertNotNull(cookie);
        assertEquals(cookie.getValue(), "value");
        assertEquals(cookie.isWrap(), false);
        assertEquals(cookie.getDomain(), "/");
        assertEquals(cookie.getPath(), "/");
    }

    @Test(groups = "standalone")
    public void testDecodeQuoted() {
        Cookie cookie = CookieDecoder.decode("ALPHA=\"VALUE1\"; Domain=docs.foo.com; Path=/accounts; Expires=Wed, 05 Feb 2014 07:37:38 GMT; Secure; HttpOnly");
        assertNotNull(cookie);
        assertEquals(cookie.getValue(), "VALUE1");
        assertEquals(cookie.isWrap(), true);
    }

    @Test(groups = "standalone")
    public void testDecodeQuotedContainingEscapedQuote() {
        Cookie cookie = CookieDecoder.decode("ALPHA=\"VALUE1\\\"\"; Domain=docs.foo.com; Path=/accounts; Expires=Wed, 05 Feb 2014 07:37:38 GMT; Secure; HttpOnly");
        assertNotNull(cookie);
        assertEquals(cookie.getValue(), "VALUE1\\\"");
        assertEquals(cookie.isWrap(), true);
    }

    @Test(groups = "standalone")
    public void testIgnoreEmptyDomain() {
        Cookie cookie = CookieDecoder.decode("sessionid=OTY4ZDllNTgtYjU3OC00MWRjLTkzMWMtNGUwNzk4MTY0MTUw;Domain=;Path=/");
        assertNull(cookie.getDomain());
    }
}
