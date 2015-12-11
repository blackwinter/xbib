package org.xbib.json.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public final class JsonNumEqualsTest  extends Assert {

    private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;

    @Test
    public void numericEqualityIsAcknowledged() throws IOException {
        Iterator<Object[]> it = getInputs();
        while (it.hasNext()) {
            Object[] o = it.next();
            final JsonNode reference = (JsonNode) o[0];
            final JsonNode node = (JsonNode) o[1];
            assertTrue(JsonNumEquals.getInstance().equivalent(reference, node));
        }
    }

    @Test
    public void numericEqualityWorksWithinArrays() throws IOException {
        Iterator<Object[]> it = getInputs();
        while (it.hasNext()) {
            Object[] o = it.next();
            final JsonNode reference = (JsonNode) o[0];
            final JsonNode node = (JsonNode) o[1];
            final ArrayNode node1 = FACTORY.arrayNode();
            node1.add(reference);
            final ArrayNode node2 = FACTORY.arrayNode();
            node2.add(node);
            assertTrue(JsonNumEquals.getInstance().equivalent(node1, node2));
        }
    }

    @Test
    public void numericEqualityWorksWithinObjects() throws IOException {
        Iterator<Object[]> it = getInputs();
        while (it.hasNext()) {
            Object[] o = it.next();
            final JsonNode reference = (JsonNode) o[0];
            final JsonNode node = (JsonNode) o[1];
            final ObjectNode node1 = FACTORY.objectNode();
            node1.set("foo", reference);
            final ObjectNode node2 = FACTORY.objectNode();
            node2.set("foo", node);
            assertTrue(JsonNumEquals.getInstance().equivalent(node1, node2));
        }
    }

    private Iterator<Object[]> getInputs() throws IOException {
        final List<Object[]> list = new ArrayList<>();
        JsonNode reference;
        JsonNode testData = JsonLoader.fromResource(this.getClass().getClassLoader(), "testfile.json");
        for (final JsonNode element : testData) {
            reference = element.get("reference");
            for (final JsonNode node : element.get("equivalences")) {
                list.add(new Object[]{reference, node});
            }
        }
        return list.iterator();
    }
}
