package org.xbib.json.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public final class JsonNodeReaderTest extends Assert {

    @Test
    public void streamIsClosedOnRead()
            throws IOException {
        final InputStream in = spy(new ByteArrayInputStream("[]".getBytes("UTF-8")));
        final JsonNode node = new JsonNodeReader().fromInputStream(in);
        verify(in).close();
        assertEquals(node, new ObjectMapper().readTree(new ByteArrayInputStream("[]".getBytes("UTF-8"))));
    }

    @Test
    public void readerIsClosedOnRead()
            throws IOException {
        final Reader reader = spy(new StringReader("[]"));
        final JsonNode node = new JsonNodeReader().fromReader(reader);
        assertEquals(node, new ObjectMapper().readTree(new StringReader("[]")));
        verify(reader).close();
    }

    @Test
    public void malformedDataThrowsExpectedException()
            throws IOException {

        String[] inputs = new String[] {
                "", "[]{}", "[]]"
        };
        final JsonNodeReader reader = new JsonNodeReader();
        for (String input : inputs) {
            try {
                reader.fromInputStream(new ByteArrayInputStream(input.getBytes()));
                fail("No exception thrown!!");
            } catch (JsonParseException e) {
                //
            }
        }
    }

}
