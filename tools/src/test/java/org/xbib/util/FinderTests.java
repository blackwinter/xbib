package org.xbib.util;

import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

public class FinderTests {

    @Test
    public void simpleFinderTest() throws IOException {
        Queue<URI> uris = new Finder()
                .find("src/main/java",
                        "org",
                        "org/xbib/tools",
                        "*.java")
                .sortBy("name")
                .order("desc")
                .getURIs(2);
        assertEquals(2, uris.size());
    }
}
