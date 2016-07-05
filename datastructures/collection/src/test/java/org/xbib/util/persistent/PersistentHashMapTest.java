package org.xbib.util.persistent;

import org.junit.Test;
import org.xbib.util.persistent.AbstractHashTrie.ArrayNode;
import org.xbib.util.persistent.AbstractHashTrie.HashNode;
import org.xbib.util.persistent.AbstractHashTrie.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class PersistentHashMapTest extends AbstractPersistentMapTestHelper<PersistentHashMap<Integer, Integer>> {

    @Test
    public void sizeWithCollisions() {
        HashKey k1 = new HashKey(1);
        HashKey k2 = new HashKey(1);
        HashKey k3 = new HashKey(1);

        PersistentHashMap<Object, Object> map = PersistentHashMap.empty();
        assertThat(map.size(), equalTo(0));

        map = map.assoc(k1, k1);
        assertThat(map.size(), equalTo(1));

        // Same key and value
        map = map.assoc(k1, k1);
        assertThat(map.size(), equalTo(1));

        // Same key, different value
        map = map.assoc(k1, k2);
        assertThat(map.size(), equalTo(1));

        // Colliding key
        map = map.assoc(k2, k2);
        assertThat(map.size(), equalTo(2));

        // Same colliding key and value
        map = map.assoc(k2, k2);
        assertThat(map.size(), equalTo(2));

        // Same colliding key, different value
        map = map.assoc(k2, k1);
        assertThat(map.size(), equalTo(2));

        // Another colliding key
        map = map.assoc(k3, k3);
        assertThat(map.size(), equalTo(3));

    }

    @Test
    public void sizeWithDeepCollision() {
        HashKey k0 = new HashKey(0);
        HashKey k1 = new HashKey(0);

        PersistentHashMap<Object, Object> map = PersistentHashMap.empty();
        map = map.assoc(k0, k0);
        map = map.assoc(k1, k1);
        assertThat(map.size(), equalTo(2));

        for (int i = 1; i < 32; i++) {
            map = map.assoc(i, i);
            assertThat(map.size(), equalTo(i + 2));
        }
        assertThat(map.size(), equalTo(33));
        map = map.assoc(32, 32);
        assertThat(map.size(), equalTo(34));
    }

    @Test
    public void collisionDissoc() {
        HashKey k0 = new HashKey(0);
        HashKey k1 = new HashKey(0);
        HashKey k2 = new HashKey(0);

        PersistentHashMap<Object, Object> map = PersistentHashMap.empty();
        map = map.assoc(k0, k0);
        map = map.assoc(k1, k1);

        assertThat(map.dissoc(0).size(), equalTo(2));

        assertThat(map.dissoc(k1).size(), equalTo(1));
        assertThat(map.dissoc(k1).get(k0), equalTo((Object) k0));

        assertThat(map.dissoc(k0).size(), equalTo(1));
        assertThat(map.dissoc(k0).get(k0), nullValue());

        map = map.assoc(k2, k2);
        assertThat(map.dissoc(k0).size(), equalTo(2));
        assertThat(map.dissoc(k0).get(k2), equalTo((Object) k2));
        assertThat(map.dissoc(k0).get(k1), equalTo((Object) k1));

        assertThat(map.dissoc(k1).size(), equalTo(2));
        assertThat(map.dissoc(k1).get(k0), equalTo((Object) k0));
        assertThat(map.dissoc(k1).get(k2), equalTo((Object) k2));

        assertThat(map.dissoc(k2).size(), equalTo(2));
        assertThat(map.dissoc(k2).get(k0), equalTo((Object) k0));
        assertThat(map.dissoc(k2).get(k1), equalTo((Object) k1));

        assertThat(map.dissoc(0), sameInstance(map));
    }

    @Test
    public void collisions() {
        HashKey k1 = new HashKey(1);
        HashKey k2 = new HashKey(1);
        HashKey k3 = new HashKey(1);

        PersistentHashMap<HashKey, HashKey> map = PersistentHashMap.empty();
        map = map.assoc(k1, k1);
        map = map.assoc(k2, k1);
        map = map.assoc(k2, k2);
        map = map.assoc(k3, k3);

        assertThat(map.get(k1), equalTo(k1));
        assertThat(map.get(k2), equalTo(k2));
        assertThat(map.get(k3), equalTo(k3));

        assertThat(map.get(new HashKey(1)), nullValue());

        Map<HashKey, HashKey> hashMap = new HashMap<HashKey, HashKey>() {{
            put(k1, k1);
            put(k2, k2);
            put(k3, k3);
        }};
        assertThat(map.asMap(), equalTo(hashMap));

        map = map.assocAll(hashMap);
        assertThat(map.asMap(), equalTo(hashMap));

        map = map.dissoc(k1);
        assertThat(map.containsKey(k1), equalTo(false));
        assertThat(map.containsKey(k2), equalTo(true));
        assertThat(map.containsKey(k3), equalTo(true));

        map = map.dissoc(k2);
        map = map.dissoc(k2);
        assertThat(map.get(k2), nullValue());

        map = map.dissoc(k3);
        assertThat(map.get(k3), nullValue());

        assertThat(map.size(), equalTo(0));
    }

    @Test
    public void collisionsIncremental() {
        PersistentHashMap<HashKey, HashKey> map = PersistentHashMap.<HashKey, HashKey>empty();
        List<HashKey> keys = new ArrayList<>();
        for (int i = 0; i < 4097; i++) {
            HashKey key = new HashKey(i);
            keys.add(key);
            map = map.assoc(key, key);

            key = new HashKey(i);
            keys.add(key);
            map = map.assoc(key, key);
        }
        assertThat(map.size(), equalTo(keys.size()));
        for (HashKey key : keys) {
            assertThat(map.get(key), equalTo(key));
        }
        assertThat(map.get(new HashKey(5)), nullValue());

        int size = map.size();
        for (HashKey key : keys) {
            map = map.dissoc(key);
            map = map.dissoc(key);
            assertThat(map.size(), equalTo(size - 1));
            size--;
        }
    }

    @Test
    public void assocAllMap() {
        Map<Integer, Integer> ints = new HashMap<Integer, Integer>() {{
            put(1, 1);
            put(2, 2);
        }};
        Map<Integer, Integer> map = PersistentHashMap.copyOf(ints).asMap();
        assertThat(map, equalTo(ints));
    }

    @Test
    public void assocAllPersistentMap() {
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.of(1, 1);
        PersistentHashMap<Integer, Integer> ints = PersistentHashMap.of(2, 2, 3, 3);
        Map<Integer, Integer> expected = new HashMap<Integer, Integer>() {{
            put(1, 1);
            put(2, 2);
            put(3, 3);
        }};

        assertThat(map.assocAll(ints).asMap(), equalTo(expected));
    }

    @Override
    protected PersistentHashMap<Integer, Integer> emptyMap() {
        return PersistentHashMap.empty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void assertMapProperties(PersistentMap<Integer, Integer> map) {
        assertThat(map, instanceOf(PersistentHashMap.class));
        PersistentHashMap<Integer, Integer> hashMap = (PersistentHashMap<Integer, Integer>) map;
        Node root = hashMap.root();
        if (root instanceof HashNode) {
            assertThat(((HashNode) root).updateContext.isCommitted(), equalTo(true));
        }
        if (root instanceof ArrayNode) {
            assertThat(((ArrayNode) root).updateContext.isCommitted(), equalTo(true));
        }
    }

    @Override
    protected void assertEmptyMap(PersistentMap<Integer, Integer> map) {
        assertThat(map.size(), equalTo(0));
        assertThat(((PersistentHashMap<Integer, Integer>) map).root(), notNullValue());
    }

    static class HashKey {
        public final int hash;

        public HashKey(int hash) {
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        public String toString() {
            return "" + hash + "@" + System.identityHashCode(this);
        }
    }

}