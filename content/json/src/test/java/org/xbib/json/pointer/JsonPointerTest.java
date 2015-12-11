
package org.xbib.json.pointer;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.json.jackson.JacksonUtils;
import org.xbib.json.jackson.JsonLoader;
import org.xbib.json.jackson.NodeType;
import org.xbib.json.jackson.SampleNodeProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class JsonPointerTest extends Assert {

    private final JsonNode testData;
    private final JsonNode document;

    public JsonPointerTest()
            throws IOException {
        testData = JsonLoader.fromResource(this.getClass().getClassLoader(), "/org/xbib/json/pointer/jsonpointer.json");
        document = testData.get("document");
    }

    @Test
    public void cannotAppendNullPointer() {
        final JsonPointer foo = null;
        try {
            JsonPointer.empty().append(foo);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            //assertEquals(e.getMessage(), BUNDLE.getMessage("nullInput"));
        }
    }

    @Test
    public void rawPointerResolvingWorks()
            throws JsonPointerException {
        final List<Object[]> list = new ArrayList<>();
        final JsonNode testNode = testData.get("pointers");
        final Map<String, JsonNode> map = JacksonUtils.asMap(testNode);

        for (final Map.Entry<String, JsonNode> entry : map.entrySet()) {
            list.add(new Object[]{entry.getKey(), entry.getValue()});
        }

        for (Object[] o : list) {
            final String input = (String) o[0];
            final JsonNode expected = (JsonNode) o[1];
            final JsonPointer pointer = new JsonPointer(input);
            assertEquals(pointer.get(document), expected);
        }
    }

    @Test
    public void uriPointerResolvingWorks()
            throws URISyntaxException, JsonPointerException {
        final List<Object[]> list = new ArrayList<>();
        final JsonNode testNode = testData.get("uris");
        final Map<String, JsonNode> map = JacksonUtils.asMap(testNode);

        for (final Map.Entry<String, JsonNode> entry : map.entrySet()) {
            list.add(new Object[]{entry.getKey(), entry.getValue()});
        }
        for (Object[] o : list) {
            final String input = (String) o[0];
            final JsonNode expected = (JsonNode) o[1];
            final URI uri = new URI(input);
            final JsonPointer pointer = new JsonPointer(uri.getFragment());

            assertEquals(pointer.get(document), expected);
        }
    }

    @Test
    public void appendingRawTokensToAPointerWorks()
            throws JsonPointerException {
        final JsonPointer ptr = new JsonPointer("/foo/bar");
        final String raw = "/0~";
        final JsonPointer expected = new JsonPointer("/foo/bar/~10~0");

        assertEquals(ptr.append(raw), expected);
    }

    @Test
    public void appendingIndicesToAPointerWorks()
            throws JsonPointerException {
        final JsonPointer ptr = new JsonPointer("/foo/bar/");
        final int index = 33;
        final JsonPointer expected = new JsonPointer("/foo/bar//33");

        assertEquals(ptr.append(index), expected);
    }

    @Test
    public void appendingOnePointerToAnotherWorks()
            throws JsonPointerException {
        final JsonPointer ptr = new JsonPointer("/a/b");
        final JsonPointer appended = new JsonPointer("/c/d");
        final JsonPointer expected = new JsonPointer("/a/b/c/d");

        assertEquals(ptr.append(appended), expected);
    }

    @Test
    public void emptyPointerAlwaysReturnsTheSameInstance() {
        Iterator<Object[]> it = SampleNodeProvider.getSamples(EnumSet.allOf(NodeType.class));
        while (it.hasNext()) {
            Object[] o = it.next();
            final JsonNode node = (JsonNode) o[0];
            assertEquals(JsonPointer.empty().get(node), node);
        }
    }

    @Test
    public void staticConstructionFromTokensWorks()
            throws JsonPointerException {
        JsonPointer ptr1, ptr2;

        ptr1 = JsonPointer.of("a", "b");
        ptr2 = new JsonPointer("/a/b");
        assertEquals(ptr1, ptr2);

        ptr1 = JsonPointer.of("", "/", "~");
        ptr2 = new JsonPointer("//~1/~0");
        assertEquals(ptr1, ptr2);

        ptr1 = JsonPointer.of(1, "xx", 0);
        ptr2 = new JsonPointer("/1/xx/0");
        assertEquals(ptr1, ptr2);

        ptr1 = JsonPointer.of("");
        ptr2 = new JsonPointer("/");
        assertEquals(ptr1, ptr2);
    }


    @Test
    public void parentComputationWorks() {
        final List<Object[]> list = new ArrayList<>();
        list.add(new Object[]{JsonPointer.empty(), JsonPointer.empty()});
        list.add(new Object[]{JsonPointer.of(1), JsonPointer.empty()});
        list.add(new Object[]{JsonPointer.of("a"), JsonPointer.empty()});
        list.add(new Object[]{JsonPointer.of("a", "b"),
                JsonPointer.of("a")});
        list.add(new Object[]{JsonPointer.of("a", "b", "c"),
                JsonPointer.of("a", "b")});
        for (Object[] o : list) {
            final JsonPointer child = (JsonPointer) o[0];
            final JsonPointer parent = (JsonPointer) o[1];
            assertEquals(child.parent(), parent);
        }
    }
}
