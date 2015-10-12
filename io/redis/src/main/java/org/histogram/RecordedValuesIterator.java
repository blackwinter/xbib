
package org.histogram;

import java.util.Iterator;

/**
 * Used for iterating through all recorded histogram values using the finest granularity steps supported by the
 * underlying representation. The iteration steps through all non-zero recorded value counts, and terminates when
 * all recorded histogram values are exhausted.
 */

public class RecordedValuesIterator extends AbstractHistogramIterator implements Iterator<HistogramIterationValue> {
    int visitedIndex;

    /**
     * @param histogram The histogram this iterator will operate on
     */
    public RecordedValuesIterator(final AbstractHistogram histogram) {
        reset(histogram);
    }

    /**
     * Reset iterator for re-use in a fresh iteration over the same histogram data set.
     */
    public void reset() {
        reset(histogram);
    }

    private void reset(final AbstractHistogram histogram) {
        super.resetIterator(histogram);
        visitedIndex = -1;
    }

    @Override
    void incrementIterationLevel() {
        visitedIndex = currentIndex;
    }

    @Override
    boolean reachedIterationLevel() {
        long currentCount = histogram.getCountAtIndex(currentIndex);
        return (currentCount != 0) && (visitedIndex != currentIndex);
    }
}
