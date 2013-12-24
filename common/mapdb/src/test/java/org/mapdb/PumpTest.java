package org.mapdb;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PumpTest {

    @Test
    public void copy() {
        DB db1 = new DB(new StoreHeap());
        Map m = db1.getHashMap("test");
        for (int i = 0; i < 1000; i++) {
            m.put(i, "aa" + i);
        }

        DB db2 = DBMaker.newMemoryDB().make();
        Pump.copy(db1, db2);

        Map m2 = db2.getHashMap("test");
        for (int i = 0; i < 1000; i++) {
            assertEquals("aa" + i, m.get(i));
        }

    }

    DB makeDB(int i) {
        if (i == 0) {
            return DBMaker.newAppendFileDB(Utils.tempDbFile()).deleteFilesAfterClose().make();
        }
        if (i == 1) {
            return DBMaker.newMemoryDB().make();
        }
        if (i == 2) {
            return DBMaker.newMemoryDB().transactionDisable().make();
        }
        return new DB(new StoreHeap());
    }

    final int dbmax = 4;


    @Test
    public void copy_all_stores_simple() {
        for (int srcc = 0; srcc < dbmax; srcc++) {
            for (int targetc = 0; targetc < dbmax; targetc++) {
                try {
                    if (targetc == 3) {
                        continue;
                    }

                    DB src = makeDB(srcc);
                    DB target = makeDB(targetc);

                    long recid1 = src.engine.put("1", Serializer.STRING_NOSIZE);
                    long recid2 = src.engine.put("2", Serializer.STRING_NOSIZE);

                    Pump.copy(src, target);

                    assertEquals("1", target.engine.get(recid1, Serializer.STRING_NOSIZE));
                    assertEquals("2", target.engine.get(recid2, Serializer.STRING_NOSIZE));
                    assertEquals("1", src.engine.get(recid1, Serializer.STRING_NOSIZE));
                    assertEquals("2", src.engine.get(recid2, Serializer.STRING_NOSIZE));

                    src.close();
                    target.close();
                } catch (Throwable e) {
                    throw new RuntimeException("Failed with " + srcc + " - " + targetc, e);
                }
            }
        }


    }

    @Test
    public void copy_all_stores() {
        for (int srcc = 0; srcc < dbmax; srcc++) {
            for (int targetc = 0; targetc < dbmax; targetc++) {
                try {
                    if (targetc == 3) {
                        continue;
                    }

                    DB src = makeDB(srcc);
                    DB target = makeDB(targetc);

                    Map m = src.getTreeMap("test");
                    for (int i = 0; i < 1000; i++) {
                        m.put(i, "99090adas d" + i);
                    }
                    src.commit();

                    Pump.copy(src, target);

                    assertEquals(src.getCatalog(), target.getCatalog());
                    Map m2 = target.getTreeMap("test");
                    assertFalse(m2.isEmpty());
                    assertEquals(m, m2);
                    src.close();
                    target.close();
                } catch (Throwable e) {
                    throw new RuntimeException("Failed with " + srcc + " - " + targetc, e);
                }
            }
        }


    }

    @Test
    public void presort() {
        final Integer max = 10000;
        List<Integer> list = new ArrayList<Integer>(max);
        for (Integer i = 0; i < max; i++) {
            list.add(i);
        }
        Collections.shuffle(list);

        Iterator<Integer> sorted = Pump.sort(list.iterator(), max / 20,
                Utils.COMPARABLE_COMPARATOR, Serializer.INTEGER);

        Integer counter = 0;
        while (sorted.hasNext()) {
            assertEquals(counter++, sorted.next());
        }
        assertEquals(max, counter);


    }

    @Test
    public void build_treeset() {
        final int max = 10000;
        List<Integer> list = new ArrayList<Integer>(max);
        for (Integer i = max - 1; i >= 0; i--) {
            list.add(i);
        }

        Engine e = new StoreHeap();
        DB db = new DB(e);

        Set s = db.createTreeSet("test")
                .nodeSize(8)
                .pumpSource(list.iterator())
                .make();

        Iterator iter = s.iterator();

        Integer count = 0;
        while (iter.hasNext()) {
            assertEquals(count++, iter.next());
        }

        for (Integer i : list) {
            assertTrue("" + i, s.contains(i));
        }

        assertEquals(max, s.size());
    }

    @Test
    public void build_treemap() {
        final int max = 10000;
        List<Integer> list = new ArrayList<Integer>(max);
        for (Integer i = max - 1; i >= 0; i--) {
            list.add(i);
        }

        Engine e = new StoreHeap();
        DB db = new DB(e);

        Fun.Function1<Object, Integer> valueExtractor = new Fun.Function1<Object, Integer>() {
            @Override
            public Object run(Integer integer) {
                return integer * 100;
            }
        };


        Map s = db.createTreeMap("test")
                .nodeSize(6)
                .pumpSource(list.iterator(), valueExtractor)
                .make();


        Iterator iter = s.keySet().iterator();

        Integer count = 0;
        while (iter.hasNext()) {
            assertEquals(count++, iter.next());
        }

        for (Integer i : list) {
            assertEquals(i * 100, s.get(i));
        }

        assertEquals(max, s.size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void build_treemap_fails_with_unsorted() {
        List a = Arrays.asList(1, 2, 3, 4, 4, 5);
        DB db = new DB(new StoreHeap());
        db.createTreeSet("test").pumpSource(a.iterator()).make();
    }

    @Test(expected = IllegalArgumentException.class)
    public void build_treemap_fails_with_unsorted2() {
        List a = Arrays.asList(1, 2, 3, 4, 3, 5);
        DB db = new DB(new StoreHeap());
        db.createTreeSet("test").pumpSource(a.iterator()).make();
    }


    @Test
    public void uuid_reversed() {
        List<UUID> u = new ArrayList<UUID>();
        Random r = new Random();
        for (int i = 0; i < 1e6; i++) {
            u.add(new UUID(r.nextLong(), r.nextLong()));
        }
        Set<UUID> sorted = new TreeSet<UUID>(Collections.reverseOrder(Utils.COMPARABLE_COMPARATOR));
        sorted.addAll(u);

        Iterator<UUID> iter = u.iterator();
        iter = Pump.sort(iter, 10000, Collections.reverseOrder(Utils.COMPARABLE_COMPARATOR), Serializer.UUID);
        Iterator<UUID> iter2 = sorted.iterator();

        while (iter.hasNext()) {
            assertEquals(iter2.next(), iter.next());
        }
        assertFalse(iter2.hasNext());
    }


    @Test
    public void duplicates_reversed() {
        List<Long> u = new ArrayList<Long>();

        for (long i = 0; i < 1e6; i++) {
            u.add(i);
            if (i % 100 == 0) {
                for (int j = 0; j < 10; j++) {
                    u.add(i);
                }
            }
        }

        Comparator c = Collections.reverseOrder(Utils.COMPARABLE_COMPARATOR);
        List<Long> sorted = new ArrayList<Long>(u);
        Collections.sort(sorted, c);

        Iterator<Long> iter = u.iterator();
        iter = Pump.sort(iter, 10000, c, Serializer.LONG);
        Iterator<Long> iter2 = sorted.iterator();

        while (iter.hasNext()) {
            assertEquals(iter2.next(), iter.next());
        }
        assertFalse(iter2.hasNext());
    }


    @Test
    public void merge() {
        Iterator i = Pump.merge(
                Arrays.asList("a", "b").iterator(),
                Arrays.asList().iterator(),
                Arrays.asList("c", "d").iterator(),
                Arrays.asList().iterator()
        );

        assertTrue(i.hasNext());
        assertEquals("a", i.next());
        assertTrue(i.hasNext());
        assertEquals("b", i.next());
        assertTrue(i.hasNext());
        assertEquals("c", i.next());
        assertTrue(i.hasNext());
        assertEquals("d", i.next());
        assertTrue(!i.hasNext());
    }

}
