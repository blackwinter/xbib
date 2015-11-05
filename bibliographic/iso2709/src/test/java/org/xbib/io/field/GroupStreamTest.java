package org.xbib.io.field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.testng.Assert.assertEquals;

public class GroupStreamTest {

    private static final Logger logger = LogManager.getLogger(FieldStreamTest.class.getName());

    int dataCount = 0;

    int groupCount= 0;

    private void incDataCount() {
        dataCount++;
    }

    private void incGroupCount() {
        groupCount++;
    }

    @Test
    public void testStream() throws Exception {

        FieldListener listener = new FieldListener() {
            @Override
            public void data(String data) {
                incDataCount();
            }

            @Override
            public void mark(char ch) {
                incGroupCount();
            }

        };

        InputStream in = getClass().getResourceAsStream("/sequential.groupstream");

        try (FieldStream stream = new GroupStreamReader(new InputStreamReader(in), 8192, listener)) {
            while (stream.ready()) {
                stream.readField();
            }
        }
        logger.info("data = {} group = {}", dataCount,  groupCount);

        assertEquals(groupCount, 11);

    }
}
