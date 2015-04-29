package org.xbib.strings.encode;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URLDecoder;

public class BaseformEncodeTest extends Assert {

    @Test
    public void testBaseForm() throws Exception {
        String s = "Beitr%C3%A4ge+zum+Studium+der+Protoplasmahysteresis+und+der+hysteretischen+Vorg%C3%A4nge.+%28Zur+Kausalit%C3%A4t+des+Alterns.%29";
        s = BaseformEncoder.normalizedFromUTF8(URLDecoder.decode(s, "UTF-8"));
        assertEquals(s, "beiträge zum studium der protoplasmahysteresis und der hysteretischen vorgänge zur kausalität des alterns");
        WordBoundaryEntropyEncoder enc = new WordBoundaryEntropyEncoder();
        assertEquals(enc.encode(s), "BZSdPphyUHcVKA");
    }
}