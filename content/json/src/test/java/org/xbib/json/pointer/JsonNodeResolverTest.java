package org.xbib.json.pointer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.json.jackson.JacksonUtils;
import org.xbib.json.jackson.NodeType;
import org.xbib.json.jackson.SampleNodeProvider;

import java.util.ArrayList;
import java.util.List;

public final class JsonNodeResolverTest extends Assert {
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();

    @Test
    public void resolvingNullReturnsNull() {
        final JsonNodeResolver resolver
                = new JsonNodeResolver(ReferenceToken.fromRaw("whatever"));

        assertNull(resolver.get(null));
    }

    @Test
    public void resolvingNonContainerNodeReturnsNull() {
        final JsonNode node = (JsonNode) SampleNodeProvider.getSamplesExcept(NodeType.ARRAY,
                NodeType.OBJECT).next()[0];
        final JsonNodeResolver resolver
                = new JsonNodeResolver(ReferenceToken.fromRaw("whatever"));

        assertNull(resolver.get(node));
    }

    @Test
    public void resolvingObjectMembersWorks() {
        final JsonNodeResolver resolver
                = new JsonNodeResolver(ReferenceToken.fromRaw("a"));
        final JsonNode target = FACTORY.textNode("b");

        ObjectNode node;

        node = FACTORY.objectNode();
        node.set("a", target);

        final JsonNode resolved = resolver.get(node);
        assertEquals(resolved, target);

        node = FACTORY.objectNode();
        node.set("b", target);

        assertNull(resolver.get(node));
    }

    @Test
    public void resolvingArrayIndicesWorks() {
        final JsonNodeResolver resolver
                = new JsonNodeResolver(ReferenceToken.fromInt(1));

        final JsonNode target = FACTORY.textNode("b");
        final ArrayNode node = FACTORY.arrayNode();

        node.add(target);
        assertNull(resolver.get(node));

        node.add(target);
        assertEquals(target, resolver.get(node));
    }

    @Test
    public void invalidIndicesYieldNull() {
        final JsonNode target = FACTORY.textNode("b");
        final ArrayNode node = FACTORY.arrayNode();
        node.add(target);

        List<Object[]> list = new ArrayList();
        list.add(new Object[]{"-1"});
        list.add(new Object[]{"232398087298731987987232"});
        list.add(new Object[]{"00"});
        list.add(new Object[]{"0 "});
        list.add(new Object[]{" 0"});
        for (Object[] o : list) {
            final String raw = (String) o[0];
            final ReferenceToken refToken = ReferenceToken.fromRaw(raw);
            final JsonNodeResolver resolver = new JsonNodeResolver(refToken);
            assertNull(resolver.get(node));
        }
    }
}
