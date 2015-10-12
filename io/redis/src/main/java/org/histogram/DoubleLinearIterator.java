
package org.histogram;

import java.util.Iterator;

/**
 * Used for iterating through {@link DoubleHistogram} values in linear steps. The iteration is
 * performed in steps of <i>valueUnitsPerBucket</i> in size, terminating when all recorded histogram
 * values are exhausted. Note that each iteration "bucket" includes values up to and including
 * the next bucket boundary value.
 */
public class DoubleLinearIterator implements Iterator<DoubleHistogramIterationValue> {
    private final LinearIterator integerLinearIterator;
    private final DoubleHistogramIterationValue iterationValue;
    DoubleHistogram histogram;

    /**
     * @param histogram           The histogram this iterator will operate on
     * @param valueUnitsPerBucket The size (in value units) of each bucket iteration.
     */
    public DoubleLinearIterator(final DoubleHistogram histogram, final double valueUnitsPerBucket) {
        this.histogram = histogram;
        integerLinearIterator = new LinearIterator(
                histogram.integerValuesHistogram,
                (long) (valueUnitsPerBucket * histogram.doubleToIntegerValueConversionRatio)
        );
        iterationValue = new DoubleHistogramIterationValue(integerLinearIterator.currentIterationValue);
    }

    /**
     * Reset iterator for re-use in a fresh iteration over the same histogram data set.
     *
     * @param valueUnitsPerBucket The size (in value units) of each bucket iteration.
     */
    public void reset(final double valueUnitsPerBucket) {
        integerLinearIterator.reset((long) (valueUnitsPerBucket * histogram.doubleToIntegerValueConversionRatio));
    }

    @Override
    public boolean hasNext() {
        return integerLinearIterator.hasNext();
    }

    @Override
    public DoubleHistogramIterationValue next() {
        integerLinearIterator.next();
        return iterationValue;
    }

    @Override
    public void remove() {
        integerLinearIterator.remove();
    }
}