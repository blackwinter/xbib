package org.xbib.io;

import org.testng.annotations.Test;

public class InputServiceTest {

    @Test
    public void testInputService() {
          InputService.asLinesFromResource("/log4j.properties");
    }

}