package org.xbib.json.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public final class JsonNodeReaderTest {
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

    @DataProvider
    public Iterator<Object[]> getMalformedData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"", "read.noContent"});
        list.add(new Object[]{"[]{}", "read.trailingData"});
        list.add(new Object[]{"[]]", "read.trailingData"});

        return list.iterator();
    }

    @Test(dataProvider = "getMalformedData")
    public void malformedDataThrowsExpectedException(final String input,
                                                     final String errmsg)
            throws IOException {

        final JsonNodeReader reader = new JsonNodeReader();

        try {
            reader.fromInputStream(new ByteArrayInputStream(input.getBytes()));
            fail("No exception thrown!!");
        } catch (JsonParseException e) {
            //assertEquals(e.getOriginalMessage(), message);
        }
    }

}
