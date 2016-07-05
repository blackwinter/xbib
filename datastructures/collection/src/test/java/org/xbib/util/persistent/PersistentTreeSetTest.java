package org.xbib.util.persistent;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;
import static java.util.Spliterator.SORTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

public class PersistentTreeSetTest {

    private static final Random RANDOM = new Random(2007);
    private static final PersistentTreeSet<Integer> NUM_SET;
    private static final int NUM_SET_SUM;

    static {
        PersistentTreeSet<Integer> set = PersistentTreeSet.empty();
        int sum = 0;
        for (int i = 1; i <= 1000000; i++) {
            set = set.conj(i);
            sum += i;
        }
        NUM_SET = set;
        NUM_SET_SUM = sum;
    }

    private static Set<Integer> randoms(int count) {
        Set<Integer> set = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            set.add(RANDOM.nextInt());
        }
        return set;
    }

    @Test
    public void immutabilityOnConjAndDisj() {
        List<PersistentTreeSet<Integer>> sets = new ArrayList<>();
        Set<Integer> ints = randoms(1234);
        PersistentTreeSet<Integer> set = PersistentTreeSet.empty();
        sets.add(set);
        for (Integer e : ints) {
            set = set.conj(e);
            sets.add(set);
        }
        assertSets(sets, ints);
        int i = 1;
        for (Integer e : ints) {
            set = sets.get(i);
            sets.set(i - 1, set.disj(e));
            i++;
        }
        assertSets(sets, ints);
    }

    @Test
    public void reduceSum() {
        assertThat(NUM_SET.stream().reduce(Integer::sum).get(), equalTo(NUM_SET_SUM));
    }

    @Test
    public void emptySetSpliterator() {
        assertThat(PersistentTreeSet.empty().spliterator().tryAdvance(n -> fail()), is(false));
        assertThat(PersistentTreeSet.empty().spliterator().trySplit(), nullValue());
    }

    @Test
    public void trySplitTooSmallSet() {
        assertThat(PersistentTreeSet.empty().conj(1).conj(2).spliterator().trySplit(), nullValue());
        Spliterator<Integer> spliterator = PersistentTreeSet.of(1, 2, 3).spliterator();
        // Initialize stack, ignore result which is covered by other tests
        spliterator.tryAdvance(n -> {
        });
        assertThat(spliterator.trySplit(), nullValue());
    }

    @Test
    public void spliteratorSize() {
        Spliterator<Integer> spliterator = PersistentTreeSet.of(1, 2, 3).spliterator();
        assertThat(spliterator.estimateSize(), equalTo(3L));
        assertThat(spliterator.hasCharacteristics(SIZED), equalTo(true));

        Spliterator<Integer> prefix = spliterator.trySplit();
        assertThat(prefix, notNullValue());

        assertThat(spliterator.estimateSize(), equalTo(1L));
        assertThat(prefix.estimateSize(), equalTo(1L));

        assertThat(spliterator.hasCharacteristics(SIZED), is(true));
        assertThat(prefix.hasCharacteristics(SIZED), is(false));
    }

    @Test
    public void spliteratorCharacteristics() {
        Spliterator<Integer> spliterator = PersistentTreeSet.of(1).spliterator();
        assertThat(spliterator.hasCharacteristics(SIZED | ORDERED | SORTED | DISTINCT | IMMUTABLE), is(true));
    }

    @Test
    public void emptySetToString() {
        assertThat(PersistentTreeSet.empty().toString(), equalTo("[]"));
    }

    @Test
    public void setToString() {
        assertThat(PersistentTreeSet.of(1, 2, 3).toString(), equalTo("[1, 2, 3]"));
    }

    @Test
    public void reducePartiallyConsumedSpliterator() {
        Spliterator<Integer> spliterator = NUM_SET.spliterator();
        assertThat(spliterator.tryAdvance(n -> assertThat(n, equalTo(1))), is(true));
        assertThat(spliterator.tryAdvance(n -> assertThat(n, equalTo(2))), is(true));

        assertThat(StreamSupport.stream(spliterator, false).reduce(Integer::sum).get(), equalTo(NUM_SET_SUM - 2 - 1));
    }

    @Test
    public void parallelReduceSum() {
        assertThat(NUM_SET.parallelStream().reduce(Integer::sum).get(), equalTo(NUM_SET_SUM));
    }

    private void assertSets(List<PersistentTreeSet<Integer>> sets, Set<Integer> ints) {
        PersistentTreeSet<Integer> set;
        assertThat(sets.get(0).size(), equalTo(0));
        assertThat(sets.get(0).root(), nullValue());
        for (int i = 1; i <= ints.size(); i++) {
            set = sets.get(i);
            assertThat(set.size(), equalTo(i));
            int j = 0;
            for (Integer e : ints) {
                if (j++ < i) {
                    assertThat(set.contains(e), equalTo(true));
                } else {
                    assertThat(set.contains(e), equalTo(false));
                }
            }
        }
    }
}