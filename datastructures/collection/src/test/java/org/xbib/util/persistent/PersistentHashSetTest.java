package org.xbib.util.persistent;

import org.junit.Test;
import org.xbib.util.persistent.PersistentHashMapTest.HashKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

public class PersistentHashSetTest {

    private static final PersistentHashSet<Integer> SET;

    static {
        SET = new PersistentHashSet<Integer>().conjAll(integers());
    }

    private static Set<Integer> integers() {
        Random random = new Random(2004);
        Set<Integer> set = new LinkedHashSet<>();
        for (int i = 1; i <= 257; i++) {
            set.add(random.nextInt());
        }
        return set;
    }

    @Test
    public void emptySetContains() {
        assertThat(new PersistentHashSet<>().contains("foo"), is(false));
    }

    @Test
    public void setEquals() {
        assertThat(SET.asSet(), equalTo(integers()));
    }

    @Test
    public void removal() {
        MutableHashSet<Integer> set = SET.toMutableSet();
        for (Integer e : set) {
            set.remove(e);
        }
        assertThat(set.size(), equalTo(0));
    }

    @Test
    public void removeMissingValue() {
        assertThat(SET.disj(123), sameInstance(SET));
    }

    @Test
    public void addAndRemoveAll() {
        PersistentHashSet<Integer> set = new PersistentHashSet<>();
        Set<Integer> ints = integers();
        set = set.conjAll(ints);
        assertThat(set.asSet(), equalTo(ints));
        set = set.disjAll(ints);
        assertThat(set.asSet(), equalTo(new HashSet<>()));
        assertThat(set.size(), equalTo(0));
    }

    @Test
    public void addIncremental() {
        PersistentHashSet<Integer> set = new PersistentHashSet<>();
        Set<Integer> ints = integers();
        for (Integer integer : ints) {
            set = set.conj(integer);
        }
        assertThat(set.asSet(), equalTo(ints));
    }

    @Test
    public void reduce() {
        PersistentHashSet<HashKey> set = new PersistentHashSet<>();
        int sum = 0;
        long count = 0;
        // ArrayNode
        for (int i = 0; i < 32; i++) {
            sum += i;
            count++;
            set = set.conj(new HashKey(i));
        }
        // HashNode
        for (int i = 1; i < 5; i++) {
            int num = i << (4 + i);
            sum += num;
            count++;
            set = set.conj(new HashKey(num));
        }
        // CollisionNodes
        set = set.conj(new HashKey(1));
        sum += 1;
        count++;
        set = set.conj(new HashKey(1));
        sum += 1;
        count++;

        assertThat(sumOf(set.stream()), equalTo(sum));
        assertThat(set.stream().count(), equalTo(count));

        assertThat(sumOf(set.parallelStream()), equalTo(sum));
        assertThat(set.parallelStream().count(), equalTo(count));

        // Reduce partially consumed in parallel
        for (int i = 1; i < set.size(); i++) {
            Spliterator<HashKey> spliterator = set.spliterator();
            final MutableNumber partialSum = new MutableNumber(0);
            for (int j = 0; j < i; j++) {
                spliterator.tryAdvance(k -> partialSum.add(k.hash));
            }
            assertThat(sumOf(StreamSupport.stream(spliterator, true)) + partialSum.intValue(), equalTo(sum));
        }
    }

    @Test
    public void splitTillTheEnd() {
        PersistentHashSet<Integer> ints = new PersistentHashSet<Integer>().conjAll(integers());
        List<Spliterator<Integer>> spliterators = new ArrayList<>();
        spliterators.add(ints.spliterator());
        int size = 0;
        while (size != spliterators.size()) {
            size = spliterators.size();
            for (int i = size - 1; i >= 0; i--) {
                Spliterator<Integer> spliterator = spliterators.get(i);
                Spliterator<Integer> split = spliterator.trySplit();
                if (split != null) {
                    spliterators.add(split);
                }
            }
        }
        final MutableLong sum = new MutableLong(0);
        for (Spliterator<Integer> spliterator : spliterators) {
            while (spliterator.tryAdvance(sum::add)) {
                ;
            }
        }
        assertThat(sum.longValue(), equalTo(
                ints.stream().map(Long::new).reduce(Long::sum).get()));
    }

    @Test
    public void trySplitSingleEntry() {
        PersistentHashSet<Integer> set = new PersistentHashSet<Integer>().conj(5);
        assertThat(set.spliterator().trySplit(), nullValue());

        Spliterator<Integer> spliterator = set.spliterator();
        assertThat(spliterator.tryAdvance(i -> assertThat(i, equalTo(5))), is(true));
        assertThat(spliterator.trySplit(), nullValue());
    }

    @Test
    public void trySplitSubSpliterator() {
        PersistentHashSet<Integer> set = new PersistentHashSet<Integer>().conj(1).conj(33);

        Spliterator<Integer> spliterator = set.spliterator();
        assertThat(spliterator.tryAdvance(i -> assertThat(i, equalTo(1))), is(true));
        assertThat(spliterator.trySplit(), nullValue());
        assertThat(spliterator.tryAdvance(i -> assertThat(i, equalTo(33))), is(true));
        assertThat(spliterator.tryAdvance(i -> {
        }), is(false));
    }

    @Test
    public void testToString() {
        PersistentHashSet<Integer> set = new PersistentHashSet<Integer>();
        assertThat(set.toString(), equalTo("[]"));

        set = set.conj(1);
        assertThat(set.toString(), equalTo("[1]"));

        set = set.conj(2);
        assertThat(set.toString(), equalTo("[1, 2]"));
    }

    private int sumOf(Stream<HashKey> stream) {
        return stream.map(Object::hashCode)
                .reduce(Integer::sum).get();
    }

    @Test
    public void emptyStream() {
        assertThat(new PersistentHashSet<>().stream().count(), equalTo(0L));
    }
}