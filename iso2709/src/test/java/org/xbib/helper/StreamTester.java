package org.xbib.helper;

import java.io.InputStream;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

public class StreamTester {

    protected static void assertStream(String name, InputStream expected, InputStream actual) {
        int offset = 0;
        try {
            while(true) {
                final int exp = expected.read();
                if(exp == -1) {
                    assertEquals(name + ": expecting end of actual stream at offset " + offset, -1, actual.read());
                    break;
                } else {
                    final int act = actual.read();
                    assertEquals(name + ": expecting same data at offset " + offset, exp, act);
                }
                offset++;
            }
        } catch(Exception e) {
            fail(name + ": exception at offset " + offset + ": " + e);
        }
    }
}
