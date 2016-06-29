package org.xbib.util.persistent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class AbstractCollectionTestHelper {

    protected static final int RANDOM_SEED = new Random().nextInt();

    protected static final String DESC = "Random(" + RANDOM_SEED + ")";

    protected List<Integer> ascending(int size) {
        List<Integer> ints = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ints.add(i);
        }
        return ints;
    }

    protected List<Integer> descending(int size) {
        List<Integer> ints = new ArrayList<>(size);
        for (int i = size; i > 0; i--) {
            ints.add(i);
        }
        return ints;
    }

    protected List<Integer> randoms(int size) {
        Random random = new Random(RANDOM_SEED);
        Set<Integer> ints = new LinkedHashSet<>(size);
        for (int i = 0; i < size; i++) {
            ints.add(random.nextInt());
        }
        return new ArrayList<>(ints);
    }

}