package org.xbib.io.http.client.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TestUTF8UrlCodec {
    @Test(groups = "standalone")
    public void testBasics() {
        assertEquals(Utf8UrlEncoder.encodeQueryElement("foobar"), "foobar");
        assertEquals(Utf8UrlEncoder.encodeQueryElement("a&b"), "a%26b");
        assertEquals(Utf8UrlEncoder.encodeQueryElement("a+b"), "a%2Bb");
    }
}
