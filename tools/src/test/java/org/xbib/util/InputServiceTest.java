package org.xbib.util;

import org.junit.Test;

public class InputServiceTest {

    @Test
    public void testInputService() {
        InputService.asLinesFromResource("/log4j2.xml");
    }
}
