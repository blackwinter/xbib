package org.xbib.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FinderTests {

    @Test
    public void simpleFinderTest() throws IOException {
        File file = File.createTempFile("finder.", ".tmp");
        file.deleteOnExit();
    }
}
