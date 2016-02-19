package org.xbib.io.field;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

public class GroupStreamTest extends Assert {

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

        assertEquals(groupCount, 11);

    }
}
