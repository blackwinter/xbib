package org.xbib.util;

import org.junit.Test;
import org.xbib.util.concurrent.BlockingMutativeArrayListMultiMap;
import org.xbib.util.concurrent.ConcurrentHashMapArrayListMultiMap;
import org.xbib.util.concurrent.CopyOnWriteArrayListMultiMap;
import org.xbib.util.concurrent.PartiallyBlockingCopyOnWriteArrayListMultiMap;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultiMapTest {

    @Test
    public void testLinkedHashMultiMap() {
        LinkedHashMultiMap<String,String> map = new LinkedHashMultiMap<>();
        map.put("a", "b");
        map.put("b", "c");
        map.put("a", "c");
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals("[b, c]", map.get("a").toString());
        assertEquals("[c]", map.get("b").toString());
        map.putAll("a", Arrays.asList("d", "e"));
        assertEquals("[b, c, d, e]", map.get("a").toString());
    }

    @Test
    public void testTreeMultiMap() {
        TreeMultiMap<String,String> map = new TreeMultiMap<>();
        map.put("a", "b");
        map.put("b", "c");
        map.put("a", "c");
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals("[b, c]", map.get("a").toString());
        assertEquals("[c]", map.get("b").toString());
        map.putAll("a", Arrays.asList("d", "e"));
        assertEquals("[b, c, d, e]", map.get("a").toString());
    }

    @Test
    public void testBlockingMutativeArrayListMultiMap() {
        BlockingMutativeArrayListMultiMap<String,String> map = new BlockingMutativeArrayListMultiMap<>();
        map.put("a", "b");
        map.put("b", "c");
        map.put("a", "c");
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals("[b, c]", map.get("a").toString());
        assertEquals("[c]", map.get("b").toString());
        map.putAll("a", Arrays.asList("d", "e"));
        assertEquals("[b, c, d, e]", map.get("a").toString());
    }

    @Test
    public void testConcurrentHashMapArrayListMultiMap() {
        ConcurrentHashMapArrayListMultiMap<String,String> map = new ConcurrentHashMapArrayListMultiMap<>();
        map.put("a", "b");
        map.put("b", "c");
        map.put("a", "c");
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals("[b, c]", map.get("a").toString());
        assertEquals("[c]", map.get("b").toString());
        map.putAll("a", Arrays.asList("d", "e"));
        assertEquals("[b, c, d, e]", map.get("a").toString());
    }

    @Test
    public void testCopyOnWriteArrayListMultiMap() {
        CopyOnWriteArrayListMultiMap<String,String> map = new CopyOnWriteArrayListMultiMap<>();
        map.put("a", "b");
        map.put("b", "c");
        map.put("a", "c");
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals("[b, c]", map.get("a").toString());
        assertEquals("[c]", map.get("b").toString());
        map.putAll("a", Arrays.asList("d", "e"));
        assertEquals("[b, c, d, e]", map.get("a").toString());
    }


    @Test
    public void testPartiallyBlockingCopyOnWriteArrayListMultiMap() {
        PartiallyBlockingCopyOnWriteArrayListMultiMap<String,String> map = new PartiallyBlockingCopyOnWriteArrayListMultiMap<>();
        map.put("a", "b");
        map.put("b", "c");
        map.put("a", "c");
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals("[b, c]", map.get("a").toString());
        assertEquals("[c]", map.get("b").toString());
        map.putAll("a", Arrays.asList("d", "e"));
        assertEquals("[b, c, d, e]", map.get("a").toString());
    }
}
