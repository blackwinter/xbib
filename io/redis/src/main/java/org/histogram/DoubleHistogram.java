
package org.histogram;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

/**
 * <h3>A floating point values High Dynamic Range (HDR) Histogram</h3>
 * It is important to note that {@link DoubleHistogram} is not thread-safe, and does not support safe concurrent
 * recording by multiple threads. If concurrent operation is required, consider usings
 * {@link ConcurrentDoubleHistogram}, {@link SynchronizedDoubleHistogram}, or(recommended)
 * {@link DoubleRecorder}, which are intended for this purpose.
 * {@link DoubleHistogram} supports the recording and analyzing sampled data value counts across a
 * configurable dynamic range of floating point (double) values, with configurable value precision within the range.
 * Dynamic range is expressed as a ratio between the highest and lowest non-zero values trackable within the histogram
 * at any given time. Value precision is expressed as the number of significant [decimal] digits in the value recording,
 * and provides control over value quantization behavior across the value range and the subsequent value resolution at
 * any given level.
 * Auto-ranging: Unlike integer value based histograms, the specific value range tracked by a {@link
 * DoubleHistogram} is not specified upfront. Only the dynamic range of values that the histogram can cover is
 * (optionally) specified. E.g. When a {@link DoubleHistogram} is created to track a dynamic range of
 * 3600000000000 (enough to track values from a nanosecond to an hour), values could be recorded into into it in any
 * consistent unit of time as long as the ratio between the highest and lowest non-zero values stays within the
 * specified dynamic range, so recording in units of nanoseconds (1.0 thru 3600000000000.0), milliseconds (0.000001
 * thru 3600000.0) seconds (0.000000001 thru 3600.0), hours (1/3.6E12 thru 1.0) will all work just as well.
 * Auto-resizing: When constructed with no specified dynamic range (or when auto-resize is turned on with {@link
 * DoubleHistogram#setAutoResize}) a {@link DoubleHistogram} will auto-resize its dynamic range to
 * include recorded values as they are encountered. Note that recording calls that cause auto-resizing may take
 * longer to execute, as resizing incurs allocation and copying of internal data structures.
 * Attempts to record non-zero values that range outside of the specified dynamic range (or exceed the limits of
 * of dynamic range when auto-resizing) may results in {@link ArrayIndexOutOfBoundsException} exceptions, either
 * due to overflow or underflow conditions. These exceptions will only be thrown if recording the value would have
 * resulted in discarding or losing the required value precision of values already recorded in the histogram.
 * See package description for {@link org.histogram} for details.
 */
public class DoubleHistogram extends EncodableHistogram {
    static final double highestAllowedValueEver; // A value that will keep us from multiplying into infinity.
    private static final int DHIST_encodingCookie = 0x0c72124e;
    private static final int DHIST_compressedEncodingCookie = 0x0c72124f;

    static {
        // We don't want to allow the histogram to shift and expand into value ranges that could equate
        // to infinity (e.g. 1024.0 * (Double.MAX_VALUE / 1024.0) == Infinity). So lets makes sure the
        // highestAllowedValueEver cap is a couple of bindary orders of magnitude away from MAX_VALUE:

        // Choose a highestAllowedValueEver that is a nice power of 2 multiple of 1.0 :
        double value = 1.0;
        while (value < Double.MAX_VALUE / 4.0) {
            value *= 2;
        }
        highestAllowedValueEver = value;
    }

    AbstractHistogram integerValuesHistogram;
    volatile double doubleToIntegerValueConversionRatio;
    volatile double integerToDoubleValueConversionRatio;
    private long configuredHighestToLowestValueRatio;
    private volatile double currentLowestValueInAutoRange;
    private volatile double currentHighestValueLimitInAutoRange;
    private boolean autoResize = false;

    /**
     * Construct a new auto-resizing DoubleHistogram using a precision stated as a number
     * of significant decimal digits.
     *
     * @param numberOfSignificantValueDigits Specifies the precision to use. This is the number of significant
     *                                       decimal digits to which the histogram will maintain value resolution
     *                                       and separation. Must be a non-negative integer between 0 and 5.
     */
    public DoubleHistogram(final int numberOfSignificantValueDigits) {
        this(2, numberOfSignificantValueDigits, Histogram.class, null);
        setAutoResize(true);
    }

    /**
     * Construct a new auto-resizing DoubleHistogram using a precision stated as a number
     * of significant decimal digits.
     * <p>
     * The {@link DoubleHistogram} will use the specified AbstractHistogram subclass
     * for tracking internal counts (e.g. {@link Histogram},
     * {@link org.histogram.ConcurrentHistogram}, {@link org.histogram.SynchronizedHistogram},
     * {@link org.histogram.IntCountsHistogram}, {@link org.histogram.ShortCountsHistogram}).
     *
     * @param numberOfSignificantValueDigits Specifies the precision to use. This is the number of significant
     *                                       decimal digits to which the histogram will maintain value resolution
     *                                       and separation. Must be a non-negative integer between 0 and 5.
     * @param internalCountsHistogramClass   The class to use for internal counts tracking
     */
    public DoubleHistogram(final int numberOfSignificantValueDigits,
                           final Class<? extends AbstractHistogram> internalCountsHistogramClass) {
        this(2, numberOfSignificantValueDigits, internalCountsHistogramClass, null);
        setAutoResize(true);
    }

    /**
     * Construct a new DoubleHistogram with the specified dynamic range (provided in
     * {@code highestToLowestValueRatio}) and using a precision stated as a number of significant
     * decimal digits.
     *
     * @param highestToLowestValueRatio      specifies the dynamic range to use
     * @param numberOfSignificantValueDigits Specifies the precision to use. This is the number of significant
     *                                       decimal digits to which the histogram will maintain value resolution
     *                                       and separation. Must be a non-negative integer between 0 and 5.
     */
    public DoubleHistogram(final long highestToLowestValueRatio, final int numberOfSignificantValueDigits) {
        this(highestToLowestValueRatio, numberOfSignificantValueDigits, Histogram.class);
    }

    /**
     * Construct a new DoubleHistogram with the specified dynamic range (provided in
     * {@code highestToLowestValueRatio}) and using a precision stated as a number of significant
     * decimal digits.
     * <p>
     * The {@link DoubleHistogram} will use the specified AbstractHistogram subclass
     * for tracking internal counts (e.g. {@link Histogram},
     * {@link org.histogram.ConcurrentHistogram}, {@link org.histogram.SynchronizedHistogram},
     * {@link org.histogram.IntCountsHistogram}, {@link org.histogram.ShortCountsHistogram}).
     *
     * @param highestToLowestValueRatio      specifies the dynamic range to use.
     * @param numberOfSignificantValueDigits Specifies the precision to use. This is the number of significant
     *                                       decimal digits to which the histogram will maintain value resolution
     *                                       and separation. Must be a non-negative integer between 0 and 5.
     * @param internalCountsHistogramClass   The class to use for internal counts tracking
     */
    protected DoubleHistogram(final long highestToLowestValueRatio,
                              final int numberOfSignificantValueDigits,
                              final Class<? extends AbstractHistogram> internalCountsHistogramClass) {
        this(highestToLowestValueRatio, numberOfSignificantValueDigits, internalCountsHistogramClass, null);
    }

    private DoubleHistogram(final long highestToLowestValueRatio,
                            final int numberOfSignificantValueDigits,
                            final Class<? extends AbstractHistogram> internalCountsHistogramClass,
                            AbstractHistogram internalCountsHistogram) {
        this(
                highestToLowestValueRatio,
                numberOfSignificantValueDigits,
                internalCountsHistogramClass,
                internalCountsHistogram,
                false
        );
    }

    //
    //
    // Auto-resizing control:
    //
    //

    private DoubleHistogram(final long highestToLowestValueRatio,
                            final int numberOfSignificantValueDigits,
                            final Class<? extends AbstractHistogram> internalCountsHistogramClass,
                            AbstractHistogram internalCountsHistogram,
                            boolean mimicInternalModel) {
        try {
            if (highestToLowestValueRatio < 2) {
                throw new IllegalArgumentException("highestToLowestValueRatio must be >= 2");
            }

            if ((highestToLowestValueRatio * Math.pow(10.0, numberOfSignificantValueDigits)) >= (1L << 61)) {
                throw new IllegalArgumentException(
                        "highestToLowestValueRatio * (10^numberOfSignificantValueDigits) must be < (1L << 61)");
            }
            if (internalCountsHistogramClass == AtomicHistogram.class) {
                throw new IllegalArgumentException(
                        "AtomicHistogram cannot be used as an internal counts histogram (does not support shifting)." +
                                " Use ConcurrentHistogram instead.");
            }

            long integerValueRange = deriveIntegerValueRange(highestToLowestValueRatio, numberOfSignificantValueDigits);

            final AbstractHistogram valuesHistogram;
            double initialLowestValueInAutoRange;

            if (internalCountsHistogram == null) {
                // Create the internal counts histogram:
                Constructor<? extends AbstractHistogram> histogramConstructor =
                        internalCountsHistogramClass.getConstructor(long.class, long.class, int.class);

                valuesHistogram =
                        histogramConstructor.newInstance(
                                1L,
                                (integerValueRange - 1),
                                numberOfSignificantValueDigits
                        );

                // We want the auto-ranging to tend towards using a value range that will result in using the
                // lower tracked value ranges and leave the higher end empty unless the range is actually used.
                // This is most easily done by making early recordings force-shift the lower value limit to
                // accommodate them (forcing a force-shift for the higher values would achieve the opposite).
                // We will therefore start with a very high value range, and let the recordings autoAdjust
                // downwards from there:
                initialLowestValueInAutoRange = Math.pow(2.0, 800);
            } else if (mimicInternalModel) {
                Constructor<? extends AbstractHistogram> histogramConstructor =
                        internalCountsHistogramClass.getConstructor(AbstractHistogram.class);

                valuesHistogram = histogramConstructor.newInstance(internalCountsHistogram);

                initialLowestValueInAutoRange = Math.pow(2.0, 800);
            } else {
                // Verify that the histogram we got matches:
                if ((internalCountsHistogram.getLowestDiscernibleValue() != 1) ||
                        (internalCountsHistogram.getHighestTrackableValue() != integerValueRange - 1) ||
                        internalCountsHistogram.getNumberOfSignificantValueDigits() != numberOfSignificantValueDigits) {
                    throw new IllegalStateException("integer values histogram does not match stated parameters.");
                }
                valuesHistogram = internalCountsHistogram;
                // Derive initialLowestValueInAutoRange from valuesHistogram's integerToDoubleValueConversionRatio:
                initialLowestValueInAutoRange =
                        internalCountsHistogram.getIntegerToDoubleValueConversionRatio() *
                                internalCountsHistogram.subBucketHalfCount;
            }

            // Set our double tracking range and internal histogram:
            init(highestToLowestValueRatio, initialLowestValueInAutoRange, valuesHistogram);

        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Construct a {@link DoubleHistogram} with the same range settings as a given source,
     * duplicating the source's start/end timestamps (but NOT it's contents)
     *
     * @param source The source histogram to duplicate
     */
    public DoubleHistogram(final DoubleHistogram source) {
        this(source.configuredHighestToLowestValueRatio,
                source.getNumberOfSignificantValueDigits(),
                source.integerValuesHistogram.getClass(),
                source.integerValuesHistogram,
                true);
        this.autoResize = source.autoResize;
        setTrackableValueRange(source.currentLowestValueInAutoRange, source.currentHighestValueLimitInAutoRange);
    }

    //
    //
    //
    // Value recording support:
    //
    //
    //

    static boolean isDoubleHistogramCookie(int cookie) {
        return isCompressedDoubleHistogramCookie(cookie) || isNonCompressedDoubleHistogramCookie(cookie);
    }

    static boolean isCompressedDoubleHistogramCookie(int cookie) {
        return (cookie == DHIST_compressedEncodingCookie);
    }

    static boolean isNonCompressedDoubleHistogramCookie(int cookie) {
        return (cookie == DHIST_encodingCookie);
    }

    static DoubleHistogram constructHistogramFromBuffer(
            int cookie,
            final ByteBuffer buffer,
            final Class<? extends AbstractHistogram> histogramClass,
            final long minBarForHighestToLowestValueRatio) throws DataFormatException {
        int numberOfSignificantValueDigits = buffer.getInt();
        long configuredHighestToLowestValueRatio = buffer.getLong();
        final AbstractHistogram valuesHistogram;
        if (isNonCompressedDoubleHistogramCookie(cookie)) {
            valuesHistogram =
                    AbstractHistogram.decodeFromByteBuffer(buffer, histogramClass, minBarForHighestToLowestValueRatio);
        } else if (isCompressedDoubleHistogramCookie(cookie)) {
            valuesHistogram =
                    AbstractHistogram.decodeFromCompressedByteBuffer(buffer, histogramClass, minBarForHighestToLowestValueRatio);
        } else {
            throw new IllegalStateException("The buffer does not contain a DoubleHistogram");
        }
        DoubleHistogram histogram =
                new DoubleHistogram(
                        configuredHighestToLowestValueRatio,
                        numberOfSignificantValueDigits,
                        histogramClass,
                        valuesHistogram
                );
        return histogram;
    }

    /**
     * Construct a new DoubleHistogram by decoding it from a ByteBuffer.
     *
     * @param buffer                             The buffer to decode from
     * @param minBarForHighestToLowestValueRatio Force highestTrackableValue to be set at least this high
     * @return The newly constructed DoubleHistogram
     */
    public static DoubleHistogram decodeFromByteBuffer(
            final ByteBuffer buffer,
            final long minBarForHighestToLowestValueRatio) {
        return decodeFromByteBuffer(buffer, Histogram.class, minBarForHighestToLowestValueRatio);
    }

    /**
     * Construct a new DoubleHistogram by decoding it from a ByteBuffer, using a
     * specified AbstractHistogram subclass for tracking internal counts (e.g. {@link Histogram},
     * {@link org.histogram.ConcurrentHistogram}, {@link org.histogram.SynchronizedHistogram},
     * {@link org.histogram.IntCountsHistogram}, {@link org.histogram.ShortCountsHistogram}).
     *
     * @param buffer                             The buffer to decode from
     * @param internalCountsHistogramClass       The class to use for internal counts tracking
     * @param minBarForHighestToLowestValueRatio Force highestTrackableValue to be set at least this high
     * @return The newly constructed DoubleHistogram
     */
    public static DoubleHistogram decodeFromByteBuffer(
            final ByteBuffer buffer,
            final Class<? extends AbstractHistogram> internalCountsHistogramClass,
            long minBarForHighestToLowestValueRatio) {
        try {
            int cookie = buffer.getInt();
            if (!isNonCompressedDoubleHistogramCookie(cookie)) {
                throw new IllegalArgumentException("The buffer does not contain a DoubleHistogram");
            }
            DoubleHistogram histogram = constructHistogramFromBuffer(cookie, buffer, internalCountsHistogramClass,
                    minBarForHighestToLowestValueRatio);
            return histogram;
        } catch (DataFormatException ex) {
            throw new RuntimeException(ex);
        }
    }

    //
    //
    //
    // Shift and auto-ranging support:
    //
    //
    //

    /**
     * Construct a new DoubleHistogram by decoding it from a compressed form in a ByteBuffer.
     *
     * @param buffer                             The buffer to decode from
     * @param minBarForHighestToLowestValueRatio Force highestTrackableValue to be set at least this high
     * @return The newly constructed DoubleHistogram
     * @throws DataFormatException on error parsing/decompressing the buffer
     */
    public static DoubleHistogram decodeFromCompressedByteBuffer(
            final ByteBuffer buffer,
            final long minBarForHighestToLowestValueRatio) throws DataFormatException {
        return decodeFromCompressedByteBuffer(buffer, Histogram.class, minBarForHighestToLowestValueRatio);
    }

    /**
     * Construct a new DoubleHistogram by decoding it from a compressed form in a ByteBuffer, using a
     * specified AbstractHistogram subclass for tracking internal counts (e.g. {@link Histogram},
     * {@link AtomicHistogram}, {@link org.histogram.SynchronizedHistogram},
     * {@link org.histogram.IntCountsHistogram}, {@link org.histogram.ShortCountsHistogram}).
     *
     * @param buffer                             The buffer to decode from
     * @param internalCountsHistogramClass       The class to use for internal counts tracking
     * @param minBarForHighestToLowestValueRatio Force highestTrackableValue to be set at least this high
     * @return The newly constructed DoubleHistogram
     * @throws DataFormatException on error parsing/decompressing the buffer
     */
    public static DoubleHistogram decodeFromCompressedByteBuffer(
            final ByteBuffer buffer,
            Class<? extends AbstractHistogram> internalCountsHistogramClass,
            long minBarForHighestToLowestValueRatio) throws DataFormatException {
        int cookie = buffer.getInt();
        if (!isCompressedDoubleHistogramCookie(cookie)) {
            throw new IllegalArgumentException("The buffer does not contain a compressed DoubleHistogram");
        }
        DoubleHistogram histogram = constructHistogramFromBuffer(cookie, buffer, internalCountsHistogramClass,
                minBarForHighestToLowestValueRatio);
        return histogram;
    }

    private static int findContainingBinaryOrderOfMagnitude(final long longNumber) {
        int pow2ceiling = 64 - Long.numberOfLeadingZeros(longNumber); // smallest power of 2 containing value
        return pow2ceiling;
    }

    private static int findContainingBinaryOrderOfMagnitude(final double doubleNumber) {
        long longNumber = (long) Math.ceil(doubleNumber);
        return findContainingBinaryOrderOfMagnitude(longNumber);
    }

    private void init(final long configuredHighestToLowestValueRatio, final double lowestTrackableUnitValue,
                      final AbstractHistogram integerValuesHistogram) {
        this.configuredHighestToLowestValueRatio = configuredHighestToLowestValueRatio;
        this.integerValuesHistogram = integerValuesHistogram;
        long internalHighestToLowestValueRatio =
                deriveInternalHighestToLowestValueRatio(configuredHighestToLowestValueRatio);
        setTrackableValueRange(lowestTrackableUnitValue, lowestTrackableUnitValue * internalHighestToLowestValueRatio);
    }

    //
    //
    //
    // Clearing support:
    //
    //
    //

    private void setTrackableValueRange(final double lowestValueInAutoRange, final double highestValueInAutoRange) {
        this.currentLowestValueInAutoRange = lowestValueInAutoRange;
        this.currentHighestValueLimitInAutoRange = highestValueInAutoRange;
        this.integerToDoubleValueConversionRatio = lowestValueInAutoRange / getLowestTrackingIntegerValue();
        this.doubleToIntegerValueConversionRatio = 1.0 / integerToDoubleValueConversionRatio;
        integerValuesHistogram.setIntegerToDoubleValueConversionRatio(integerToDoubleValueConversionRatio);
    }

    //
    //
    //
    // Copy support:
    //
    //
    //

    public boolean isAutoResize() {
        return autoResize;
    }

    public void setAutoResize(boolean autoResize) {
        this.autoResize = autoResize;
    }

    /**
     * Record a value in the histogram
     *
     * @param value The value to be recorded
     * @throws ArrayIndexOutOfBoundsException (may throw) if value is cannot be covered by the histogram's range
     */
    public void recordValue(final double value) throws ArrayIndexOutOfBoundsException {
        recordSingleValue(value);
    }

    /**
     * Record a value in the histogram (adding to the value's current count)
     *
     * @param value The value to be recorded
     * @param count The number of occurrences of this value to record
     * @throws ArrayIndexOutOfBoundsException (may throw) if value is cannot be covered by the histogram's range
     */
    public void recordValueWithCount(final double value, final long count) throws ArrayIndexOutOfBoundsException {
        recordCountAtValue(count, value);
    }

    //
    //
    //
    // Add support:
    //
    //
    //

    /**
     * Record a value in the histogram.
     * <p>
     * To compensate for the loss of sampled values when a recorded value is larger than the expected
     * interval between value samples, Histogram will auto-generate an additional series of decreasingly-smaller
     * (down to the expectedIntervalBetweenValueSamples) value records.
     * <p>
     * Note: This is a at-recording correction method, as opposed to the post-recording correction method provided
     * by {@link #copyCorrectedForCoordinatedOmission(double)}.
     * The use cases for these two methods are mutually exclusive, and only one of the two should be be used on
     * a given data set to correct for the same coordinated omission issue.
     * <p>
     * See notes in the description of the Histogram calls for an illustration of why this corrective behavior is
     * important.
     *
     * @param value                               The value to record
     * @param expectedIntervalBetweenValueSamples If expectedIntervalBetweenValueSamples is larger than 0, add
     *                                            auto-generated value records as appropriate if value is larger
     *                                            than expectedIntervalBetweenValueSamples
     * @throws ArrayIndexOutOfBoundsException (may throw) if value is cannot be covered by the histogram's range
     */
    public void recordValueWithExpectedInterval(final double value, final double expectedIntervalBetweenValueSamples)
            throws ArrayIndexOutOfBoundsException {
        recordValueWithCountAndExpectedInterval(value, 1, expectedIntervalBetweenValueSamples);
    }

    private void recordCountAtValue(final long count, final double value) throws ArrayIndexOutOfBoundsException {
        if ((value < currentLowestValueInAutoRange) || (value >= currentHighestValueLimitInAutoRange)) {
            // Zero is valid and needs no auto-ranging, but also rare enough that we should deal
            // with it on the slow path...
            autoAdjustRangeForValue(value);
        }

        long integerValue = (long) (value * doubleToIntegerValueConversionRatio);
        integerValuesHistogram.recordValueWithCount(integerValue, count);
    }

    private void recordSingleValue(final double value) throws ArrayIndexOutOfBoundsException {
        if ((value < currentLowestValueInAutoRange) || (value >= currentHighestValueLimitInAutoRange)) {
            // Zero is valid and needs no auto-ranging, but also rare enough that we should deal
            // with it on the slow path...
            autoAdjustRangeForValue(value);
        }

        long integerValue = (long) (value * doubleToIntegerValueConversionRatio);
        integerValuesHistogram.recordValue(integerValue);
    }

    //
    //
    //
    // Comparison support:
    //
    //
    //

    private void recordValueWithCountAndExpectedInterval(final double value, final long count,
                                                         final double expectedIntervalBetweenValueSamples)
            throws ArrayIndexOutOfBoundsException {
        recordCountAtValue(count, value);
        if (expectedIntervalBetweenValueSamples <= 0) {
            return;
        }
        for (double missingValue = value - expectedIntervalBetweenValueSamples;
             missingValue >= expectedIntervalBetweenValueSamples;
             missingValue -= expectedIntervalBetweenValueSamples) {
            recordCountAtValue(count, missingValue);
        }
    }

    //
    //
    //
    // Histogram structure querying support:
    //
    //
    //

    private void autoAdjustRangeForValue(final double value) {
        // Zero is always valid, and doesn't need auto-range adjustment:
        if (value == 0.0) {
            return;
        }
        autoAdjustRangeForValueSlowPath(value);
    }

    private synchronized void autoAdjustRangeForValueSlowPath(final double value) {
        try {
            if (value < currentLowestValueInAutoRange) {
                if (value < 0.0) {
                    throw new ArrayIndexOutOfBoundsException("Negative values cannot be recorded");
                }
                do {
                    int shiftAmount =
                            findCappedContainingBinaryOrderOfMagnitude(
                                    Math.ceil(currentLowestValueInAutoRange / value) - 1.0);
                    shiftCoveredRangeToTheRight(shiftAmount);
                }
                while (value < currentLowestValueInAutoRange);
            } else if (value >= currentHighestValueLimitInAutoRange) {
                if (value > highestAllowedValueEver) {
                    throw new ArrayIndexOutOfBoundsException(
                            "Values above " + highestAllowedValueEver + " cannot be recorded");
                }
                do {
                    // If value is an exact whole multiple of currentHighestValueLimitInAutoRange, it "belongs" with
                    // the next level up, as it crosses the limit. With floating point values, the simplest way to
                    // make this shift on exact multiple values happen (but not for any just-smaller-than-exact-multiple
                    // values) is to use a value that is 1 ulp bigger in computing the ratio for the shift amount:
                    int shiftAmount =
                            findCappedContainingBinaryOrderOfMagnitude(
                                    Math.ceil((value + Math.ulp(value)) / currentHighestValueLimitInAutoRange) - 1.0);
                    shiftCoveredRangeToTheLeft(shiftAmount);
                }
                while (value >= currentHighestValueLimitInAutoRange);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException("The value " + value +
                    " is out of bounds for histogram, current covered range [" +
                    currentLowestValueInAutoRange + ", " + currentHighestValueLimitInAutoRange +
                    ") cannot be extended any further.\n" +
                    "Caused by: " + ex);
        }
    }

    private void shiftCoveredRangeToTheRight(final int numberOfBinaryOrdersOfMagnitude) {
        // We are going to adjust the tracked range by effectively shifting it to the right
        // (in the integer shift sense).
        //
        // To counter the right shift of the value multipliers, we need to left shift the internal
        // representation such that the newly shifted integer values will continue to return the
        // same double values.

        // Initially, new range is the same as current range, to make sure we correctly recover
        // from a shift failure if one happens:
        double newLowestValueInAutoRange = currentLowestValueInAutoRange;
        double newHighestValueLimitInAutoRange = currentHighestValueLimitInAutoRange;

        try {
            double shiftMultiplier = 1.0 / (1L << numberOfBinaryOrdersOfMagnitude);

            // First, temporarily change the highest value in auto-range without changing conversion ratios.
            // This is done to force new values higher than the new expected highest value to attempt an
            // adjustment (which is synchronized and will wait behind this one). This ensures that we will
            // not end up with any concurrently recorded values that would need to be discarded if the shift
            // fails. If this shift succeeds, the pending adjustment attempt will end up doing nothing.
            currentHighestValueLimitInAutoRange *= shiftMultiplier;

            // First shift the values, to give the shift a chance to fail:

            // Shift integer histogram left, increasing the recorded integer values for current recordings
            // by a factor of (1 << numberOfBinaryOrdersOfMagnitude):

            // (no need to shift any values if all recorded values are at the 0 value level:)
            if (getTotalCount() > integerValuesHistogram.getCountAtIndex(0)) {
                // Apply the shift:
                try {
                    integerValuesHistogram.shiftValuesLeft(numberOfBinaryOrdersOfMagnitude);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // Failed to shift, try to expand size instead:
                    handleShiftValuesException(numberOfBinaryOrdersOfMagnitude, ex);
                    // First expand the highest limit to reflect successful size expansion:
                    newHighestValueLimitInAutoRange /= shiftMultiplier;
                    // Successfully expanded histogram range by numberOfBinaryOrdersOfMagnitude, but not
                    // by shifting (shifting failed because there was not room to shift left into). Instead,
                    // we grew the max value without changing the value mapping. Since we were trying to
                    // shift values left to begin with, trying to shift the left again will work (we now
                    // have room to shift into):
                    integerValuesHistogram.shiftValuesLeft(numberOfBinaryOrdersOfMagnitude);
                }
            }
            // Shift (or resize) was successful. Adjust new range to reflect:
            newLowestValueInAutoRange *= shiftMultiplier;
            newHighestValueLimitInAutoRange *= shiftMultiplier;
        } finally {
            // Set the new range to either the successfully changed one, or the original one:
            setTrackableValueRange(newLowestValueInAutoRange, newHighestValueLimitInAutoRange);
        }
    }

    private void shiftCoveredRangeToTheLeft(final int numberOfBinaryOrdersOfMagnitude) {
        // We are going to adjust the tracked range by effectively shifting it to the right
        // (in the integer shift sense).
        //
        // To counter the left shift of the value multipliers, we need to right shift the internal
        // representation such that the newly shifted integer values will continue to return the
        // same double values.

        // Initially, new range is the same as current range, to make sure we correctly recover
        // from a shift failure if one happens:
        double newLowestValueInAutoRange = currentLowestValueInAutoRange;
        double newHighestValueLimitInAutoRange = currentHighestValueLimitInAutoRange;

        try {
            double shiftMultiplier = 1.0 * (1L << numberOfBinaryOrdersOfMagnitude);

            // First, temporarily change the lowest value in auto-range without changing conversion ratios.
            // This is done to force new values lower than the new expected lowest value to attempt an
            // adjustment (which is synchronized and will wait behind this one). This ensures that we will
            // not end up with any concurrently recorded values that would need to be discarded if the shift
            // fails. If this shift succeeds, the pending adjustment attempt will end up doing nothing.
            currentLowestValueInAutoRange *= shiftMultiplier;

            // First shift the values, to give the shift a chance to fail:

            // Shift integer histogram right, decreasing the recorded integer values for current recordings
            // by a factor of (1 << numberOfBinaryOrdersOfMagnitude):

            // (no need to shift any values if all recorded values are at the 0 value level:)
            if (getTotalCount() > integerValuesHistogram.getCountAtIndex(0)) {
                // Apply the shift:
                try {
                    integerValuesHistogram.shiftValuesRight(numberOfBinaryOrdersOfMagnitude);
                    // Shift was successful. Adjust new range to reflect:
                    newLowestValueInAutoRange *= shiftMultiplier;
                    newHighestValueLimitInAutoRange *= shiftMultiplier;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // Failed to shift, try to expand size instead:
                    handleShiftValuesException(numberOfBinaryOrdersOfMagnitude, ex);
                    // Successfully expanded histogram range by numberOfBinaryOrdersOfMagnitude, but not
                    // by shifting (shifting failed because there was not room to shift right into). Instead,
                    // we grew the max value without changing the value mapping. Since we were trying to
                    // shift values right to begin with to make room for a larger value than we had had
                    // been able to fit before, no shift is needed, as the value should now fit. So rather
                    // than shifting and adjusting both lowest and highest limits, we'll end up just
                    // expanding newHighestValueLimitInAutoRange to indicate the newly expanded range.
                    // We therefore reverse-scale the newLowestValueInAutoRange before lating the later
                    // code scale both up:
                    newLowestValueInAutoRange /= shiftMultiplier;
                }
            }
            // Shift (or resize) was successful. Adjust new range to reflect:
            newLowestValueInAutoRange *= shiftMultiplier;
            newHighestValueLimitInAutoRange *= shiftMultiplier;
        } finally {
            // Set the new range to either the successfully changed one, or the original one:
            setTrackableValueRange(newLowestValueInAutoRange, newHighestValueLimitInAutoRange);
        }
    }

    private void handleShiftValuesException(final int numberOfBinaryOrdersOfMagnitude, Exception ex) {
        if (!autoResize) {
            throw new ArrayIndexOutOfBoundsException("Value outside of histogram covered range.\nCaused by: " + ex);
        }

        long highestTrackableValue = integerValuesHistogram.getHighestTrackableValue();
        int currentContainingOrderOfMagnitude = findContainingBinaryOrderOfMagnitude(highestTrackableValue);
        int newContainingOrderOfMagnitude = numberOfBinaryOrdersOfMagnitude + currentContainingOrderOfMagnitude;
        if (newContainingOrderOfMagnitude > 63) {
            throw new ArrayIndexOutOfBoundsException(
                    "Cannot resize histogram covered range beyond (1L << 63) / (1L << " +
                            (integerValuesHistogram.subBucketHalfCountMagnitude) + ") - 1.\n" +
                            "Caused by: " + ex);
        }
        long newHighestTrackableValue = (1L << newContainingOrderOfMagnitude) - 1;
        integerValuesHistogram.resize(newHighestTrackableValue);
        integerValuesHistogram.highestTrackableValue = newHighestTrackableValue;
        configuredHighestToLowestValueRatio <<= numberOfBinaryOrdersOfMagnitude;
    }

    /**
     * Reset the contents and stats of this histogram
     */
    public void reset() {
        integerValuesHistogram.clearCounts();
    }

    /**
     * Create a copy of this histogram, complete with data and everything.
     *
     * @return A distinct copy of this histogram.
     */
    public DoubleHistogram copy() {
        final DoubleHistogram targetHistogram =
                new DoubleHistogram(configuredHighestToLowestValueRatio, getNumberOfSignificantValueDigits());
        targetHistogram.setTrackableValueRange(currentLowestValueInAutoRange, currentHighestValueLimitInAutoRange);
        integerValuesHistogram.copyInto(targetHistogram.integerValuesHistogram);
        return targetHistogram;
    }

    /**
     * Get a copy of this histogram, corrected for coordinated omission.
     * <p>
     * To compensate for the loss of sampled values when a recorded value is larger than the expected
     * interval between value samples, the new histogram will include an auto-generated additional series of
     * decreasingly-smaller (down to the expectedIntervalBetweenValueSamples) value records for each count found
     * in the current histogram that is larger than the expectedIntervalBetweenValueSamples.
     * <p>
     * Note: This is a post-correction method, as opposed to the at-recording correction method provided
     * by {@link #recordValueWithExpectedInterval(double, double) recordValueWithExpectedInterval}. The two
     * methods are mutually exclusive, and only one of the two should be be used on a given data set to correct
     * for the same coordinated omission issue.
     * by
     * <p>
     * See notes in the description of the Histogram calls for an illustration of why this corrective behavior is
     * important.
     *
     * @param expectedIntervalBetweenValueSamples If expectedIntervalBetweenValueSamples is larger than 0, add
     *                                            auto-generated value records as appropriate if value is larger
     *                                            than expectedIntervalBetweenValueSamples
     * @return a copy of this histogram, corrected for coordinated omission.
     */
    public DoubleHistogram copyCorrectedForCoordinatedOmission(final double expectedIntervalBetweenValueSamples) {
        final DoubleHistogram targetHistogram =
                new DoubleHistogram(configuredHighestToLowestValueRatio, getNumberOfSignificantValueDigits());
        targetHistogram.setTrackableValueRange(currentLowestValueInAutoRange, currentHighestValueLimitInAutoRange);
        targetHistogram.addWhileCorrectingForCoordinatedOmission(this, expectedIntervalBetweenValueSamples);
        return targetHistogram;
    }

    /**
     * Copy this histogram into the target histogram, overwriting it's contents.
     *
     * @param targetHistogram the histogram to copy into
     */
    public void copyInto(final DoubleHistogram targetHistogram) {
        targetHistogram.reset();
        targetHistogram.add(this);
        targetHistogram.setStartTimeStamp(integerValuesHistogram.startTimeStampMsec);
        targetHistogram.setEndTimeStamp(integerValuesHistogram.endTimeStampMsec);
    }

    /**
     * Copy this histogram, corrected for coordinated omission, into the target histogram, overwriting it's contents.
     * (see {@link #copyCorrectedForCoordinatedOmission} for more detailed explanation about how correction is applied)
     *
     * @param targetHistogram                     the histogram to copy into
     * @param expectedIntervalBetweenValueSamples If expectedIntervalBetweenValueSamples is larger than 0, add
     *                                            auto-generated value records as appropriate if value is larger
     *                                            than expectedIntervalBetweenValueSamples
     */
    public void copyIntoCorrectedForCoordinatedOmission(final DoubleHistogram targetHistogram,
                                                        final double expectedIntervalBetweenValueSamples) {
        targetHistogram.reset();
        targetHistogram.addWhileCorrectingForCoordinatedOmission(this, expectedIntervalBetweenValueSamples);
        targetHistogram.setStartTimeStamp(integerValuesHistogram.startTimeStampMsec);
        targetHistogram.setEndTimeStamp(integerValuesHistogram.endTimeStampMsec);
    }

    /**
     * Add the contents of another histogram to this one.
     *
     * @param fromHistogram The other histogram.
     * @throws ArrayIndexOutOfBoundsException (may throw) if values in fromHistogram's cannot be
     *                                        covered by this histogram's range
     */
    public void add(final DoubleHistogram fromHistogram) throws ArrayIndexOutOfBoundsException {
        int arrayLength = fromHistogram.integerValuesHistogram.countsArrayLength;
        AbstractHistogram fromIntegerHistogram = fromHistogram.integerValuesHistogram;
        for (int i = 0; i < arrayLength; i++) {
            long count = fromIntegerHistogram.getCountAtIndex(i);
            if (count > 0) {
                recordValueWithCount(
                        fromIntegerHistogram.valueFromIndex(i) *
                                fromHistogram.integerToDoubleValueConversionRatio,
                        count);
            }
        }
    }

    /**
     * Add the contents of another histogram to this one, while correcting the incoming data for coordinated omission.
     * <p>
     * To compensate for the loss of sampled values when a recorded value is larger than the expected
     * interval between value samples, the values added will include an auto-generated additional series of
     * decreasingly-smaller (down to the expectedIntervalBetweenValueSamples) value records for each count found
     * in the current histogram that is larger than the expectedIntervalBetweenValueSamples.
     * <p>
     * Note: This is a post-recording correction method, as opposed to the at-recording correction method provided
     * by {@link #recordValueWithExpectedInterval(double, double) recordValueWithExpectedInterval}. The two
     * methods are mutually exclusive, and only one of the two should be be used on a given data set to correct
     * for the same coordinated omission issue.
     * by
     * <p>
     * See notes in the description of the Histogram calls for an illustration of why this corrective behavior is
     * important.
     *
     * @param fromHistogram                       Other histogram. highestToLowestValueRatio and numberOfSignificantValueDigits must match.
     * @param expectedIntervalBetweenValueSamples If expectedIntervalBetweenValueSamples is larger than 0, add
     *                                            auto-generated value records as appropriate if value is larger
     *                                            than expectedIntervalBetweenValueSamples
     * @throws ArrayIndexOutOfBoundsException (may throw) if values exceed highestTrackableValue
     */
    public void addWhileCorrectingForCoordinatedOmission(final DoubleHistogram fromHistogram,
                                                         final double expectedIntervalBetweenValueSamples) {
        final DoubleHistogram toHistogram = this;

        for (HistogramIterationValue v : fromHistogram.integerValuesHistogram.recordedValues()) {
            toHistogram.recordValueWithCountAndExpectedInterval(
                    v.getValueIteratedTo() * integerToDoubleValueConversionRatio,
                    v.getCountAtValueIteratedTo(), expectedIntervalBetweenValueSamples);
        }
    }

    /**
     * Subtract the contents of another histogram from this one.
     *
     * @param otherHistogram The other histogram.
     * @throws ArrayIndexOutOfBoundsException (may throw) if values in fromHistogram's cannot be
     *                                        covered by this histogram's range
     */
    public void subtract(final DoubleHistogram otherHistogram) {
        int arrayLength = otherHistogram.integerValuesHistogram.countsArrayLength;
        AbstractHistogram otherIntegerHistogram = otherHistogram.integerValuesHistogram;
        for (int i = 0; i < arrayLength; i++) {
            long otherCount = otherIntegerHistogram.getCountAtIndex(i);
            if (otherCount > 0) {
                double otherValue = otherIntegerHistogram.valueFromIndex(i) *
                        otherHistogram.integerToDoubleValueConversionRatio;
                if (getCountAtValue(otherValue) < otherCount) {
                    throw new IllegalArgumentException("otherHistogram count (" + otherCount + ") at value " +
                            otherValue + " is larger than this one's (" + getCountAtValue(otherValue) + ")");
                }
                recordValueWithCount(otherValue, -otherCount);
            }
        }
    }

    //
    //
    //
    // Timestamp support:
    //
    //
    //

    /**
     * Determine if this histogram is equivalent to another.
     *
     * @param other the other histogram to compare to
     * @return True if this histogram are equivalent with the other.
     */
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DoubleHistogram)) {
            return false;
        }
        DoubleHistogram that = (DoubleHistogram) other;
        if ((currentLowestValueInAutoRange != that.currentLowestValueInAutoRange) ||
                (currentHighestValueLimitInAutoRange != that.currentHighestValueLimitInAutoRange) ||
                (getNumberOfSignificantValueDigits() != that.getNumberOfSignificantValueDigits())) {
            return false;
        }
        if (integerValuesHistogram.countsArrayLength != that.integerValuesHistogram.countsArrayLength) {
            return false;
        }
        if (getTotalCount() != that.getTotalCount()) {
            return false;
        }
        for (int i = 0; i < integerValuesHistogram.countsArrayLength; i++) {
            if (integerValuesHistogram.getCountAtIndex(i) != that.integerValuesHistogram.getCountAtIndex(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the total count of all recorded values in the histogram
     *
     * @return the total count of all recorded values in the histogram
     */
    public long getTotalCount() {
        return integerValuesHistogram.getTotalCount();
    }

    /**
     * get the current lowest (non zero) trackable value the automatically determined range
     * (keep in mind that this can change because it is auto ranging)
     *
     * @return current lowest trackable value the automatically determined range
     */
    double getCurrentLowestTrackableNonZeroValue() {
        return currentLowestValueInAutoRange;
    }

    /**
     * get the current highest trackable value in the automatically determined range
     * (keep in mind that this can change because it is auto ranging)
     *
     * @return current highest trackable value in the automatically determined range
     */
    double getCurrentHighestTrackableValue() {
        return currentHighestValueLimitInAutoRange;
    }

    //
    //
    //
    // Histogram Data access support:
    //
    //
    //

    /**
     * Get the current conversion ratio from interval integer value representation to double units.
     * (keep in mind that this can change because it is auto ranging). This ratio can be useful
     * for converting integer values found in iteration, although the preferred form for accessing
     * iteration values would be to use the
     * {@link org.histogram.HistogramIterationValue#getDoubleValueIteratedTo() getDoubleValueIteratedTo()}
     * and
     * {@link org.histogram.HistogramIterationValue#getDoubleValueIteratedFrom() getDoubleValueIteratedFrom()}
     * accessors to {@link org.histogram.HistogramIterationValue} iterated values.
     *
     * @return the current conversion ratio from interval integer value representation to double units.
     */
    public double getIntegerToDoubleValueConversionRatio() {
        return integerToDoubleValueConversionRatio;
    }

    /**
     * get the configured numberOfSignificantValueDigits
     *
     * @return numberOfSignificantValueDigits
     */
    public int getNumberOfSignificantValueDigits() {
        return integerValuesHistogram.numberOfSignificantValueDigits;
    }

    /**
     * get the Dynamic range of the histogram: the configured ratio between the highest trackable value and the
     * lowest trackable non zero value at any given time.
     *
     * @return the dynamic range of the histogram, expressed as the ratio between the highest trackable value
     * and the lowest trackable non zero value at any given time.
     */
    public long getHighestToLowestValueRatio() {
        return configuredHighestToLowestValueRatio;
    }

    /**
     * Get the size (in value units) of the range of values that are equivalent to the given value within the
     * histogram's resolution. Where "equivalent" means that value samples recorded for any two
     * equivalent values are counted in a common total count.
     *
     * @param value The given value
     * @return The lowest value that is equivalent to the given value within the histogram's resolution.
     */
    public double sizeOfEquivalentValueRange(final double value) {
        return integerValuesHistogram.sizeOfEquivalentValueRange((long) (value * doubleToIntegerValueConversionRatio)) *
                integerToDoubleValueConversionRatio;
    }

    /**
     * Get the lowest value that is equivalent to the given value within the histogram's resolution.
     * Where "equivalent" means that value samples recorded for any two
     * equivalent values are counted in a common total count.
     *
     * @param value The given value
     * @return The lowest value that is equivalent to the given value within the histogram's resolution.
     */
    public double lowestEquivalentValue(final double value) {
        return integerValuesHistogram.lowestEquivalentValue((long) (value * doubleToIntegerValueConversionRatio)) *
                integerToDoubleValueConversionRatio;
    }

    /**
     * Get the highest value that is equivalent to the given value within the histogram's resolution.
     * Where "equivalent" means that value samples recorded for any two
     * equivalent values are counted in a common total count.
     *
     * @param value The given value
     * @return The highest value that is equivalent to the given value within the histogram's resolution.
     */
    public double highestEquivalentValue(final double value) {
        double nextNonEquivalentValue = nextNonEquivalentValue(value);
        // Theoretically, nextNonEquivalentValue - ulp(nextNonEquivalentValue) == nextNonEquivalentValue
        // is possible (if the ulp size switches right at nextNonEquivalentValue), so drop by 2 ulps and
        // increment back up to closest within-ulp value.
        double highestEquivalentValue = nextNonEquivalentValue - (2 * Math.ulp(nextNonEquivalentValue));
        while (highestEquivalentValue + Math.ulp(highestEquivalentValue) < nextNonEquivalentValue) {
            highestEquivalentValue += Math.ulp(highestEquivalentValue);
        }

        return highestEquivalentValue;
    }

    /**
     * Get a value that lies in the middle (rounded up) of the range of values equivalent the given value.
     * Where "equivalent" means that value samples recorded for any two
     * equivalent values are counted in a common total count.
     *
     * @param value The given value
     * @return The value lies in the middle (rounded up) of the range of values equivalent the given value.
     */
    public double medianEquivalentValue(final double value) {
        return integerValuesHistogram.medianEquivalentValue((long) (value * doubleToIntegerValueConversionRatio)) *
                integerToDoubleValueConversionRatio;
    }

    /**
     * Get the next value that is not equivalent to the given value within the histogram's resolution.
     * Where "equivalent" means that value samples recorded for any two
     * equivalent values are counted in a common total count.
     *
     * @param value The given value
     * @return The next value that is not equivalent to the given value within the histogram's resolution.
     */
    public double nextNonEquivalentValue(final double value) {
        return integerValuesHistogram.nextNonEquivalentValue((long) (value * doubleToIntegerValueConversionRatio)) *
                integerToDoubleValueConversionRatio;
    }

    /**
     * Determine if two values are equivalent with the histogram's resolution.
     * Where "equivalent" means that value samples recorded for any two
     * equivalent values are counted in a common total count.
     *
     * @param value1 first value to compare
     * @param value2 second value to compare
     * @return True if values are equivalent to within the histogram's resolution.
     */
    public boolean valuesAreEquivalent(final double value1, final double value2) {
        return (lowestEquivalentValue(value1) == lowestEquivalentValue(value2));
    }

    /**
     * Provide a (conservatively high) estimate of the Histogram's total footprint in bytes
     *
     * @return a (conservatively high) estimate of the Histogram's total footprint in bytes
     */
    public int getEstimatedFootprintInBytes() {
        return integerValuesHistogram._getEstimatedFootprintInBytes();
    }

    /**
     * get the start time stamp [optionally] stored with this histogram
     *
     * @return the start time stamp [optionally] stored with this histogram
     */
    public long getStartTimeStamp() {
        return integerValuesHistogram.startTimeStampMsec;
    }

    /**
     * Set the start time stamp value associated with this histogram to a given value.
     *
     * @param timeStampMsec the value to set the time stamp to, [by convention] in msec since the epoch.
     */
    public void setStartTimeStamp(final long timeStampMsec) {
        this.integerValuesHistogram.startTimeStampMsec = timeStampMsec;
    }

    /**
     * get the end time stamp [optionally] stored with this histogram
     *
     * @return the end time stamp [optionally] stored with this histogram
     */
    public long getEndTimeStamp() {
        return integerValuesHistogram.endTimeStampMsec;
    }

    /**
     * Set the end time stamp value associated with this histogram to a given value.
     *
     * @param timeStampMsec the value to set the time stamp to, [by convention] in msec since the epoch.
     */
    public void setEndTimeStamp(final long timeStampMsec) {
        this.integerValuesHistogram.endTimeStampMsec = timeStampMsec;
    }

    /**
     * Get the lowest recorded value level in the histogram
     *
     * @return the Min value recorded in the histogram
     */
    public double getMinValue() {
        return integerValuesHistogram.getMinValue() * integerToDoubleValueConversionRatio;
    }


    // Percentile iterator support:

    /**
     * Get the highest recorded value level in the histogram
     *
     * @return the Max value recorded in the histogram
     */
    public double getMaxValue() {
        return integerValuesHistogram.getMaxValue() * integerToDoubleValueConversionRatio;
    }

    // Linear iterator support:

    /**
     * Get the lowest recorded non-zero value level in the histogram
     *
     * @return the lowest recorded non-zero value level in the histogram
     */
    public double getMinNonZeroValue() {
        return integerValuesHistogram.getMinNonZeroValue() * integerToDoubleValueConversionRatio;
    }

    // Logarithmic iterator support:

    /**
     * Get the highest recorded value level in the histogram as a double
     *
     * @return the highest recorded value level in the histogram as a double
     */
    @Override
    public double getMaxValueAsDouble() {
        return getMaxValue();
    }

    // Recorded value iterator support:

    /**
     * Get the computed mean value of all recorded values in the histogram
     *
     * @return the mean value (in value units) of the histogram data
     */
    public double getMean() {
        return integerValuesHistogram.getMean() * integerToDoubleValueConversionRatio;
    }

    // AllValues iterator support:

    /**
     * Get the computed standard deviation of all recorded values in the histogram
     *
     * @return the standard deviation (in value units) of the histogram data
     */
    public double getStdDeviation() {
        return integerValuesHistogram.getStdDeviation() * integerToDoubleValueConversionRatio;
    }

    /**
     * Get the value at a given percentile.
     * When the percentile is &gt; 0.0, the value returned is the value that the given the given
     * percentage of the overall recorded value entries in the histogram are either smaller than
     * or equivalent to. When the percentile is 0.0, the value returned is the value that all value
     * entries in the histogram are either larger than or equivalent to.
     * <p>
     * Note that two values are "equivalent" in this statement if
     * {@link DoubleHistogram#valuesAreEquivalent} would return true.
     *
     * @param percentile The percentile for which to return the associated value
     * @return The value that the given percentage of the overall recorded value entries in the
     * histogram are either smaller than or equivalent to. When the percentile is 0.0, returns the
     * value that all value entries in the histogram are either larger than or equivalent to.
     */
    public double getValueAtPercentile(final double percentile) {
        return integerValuesHistogram.getValueAtPercentile(percentile) * integerToDoubleValueConversionRatio;
    }

    //
    //
    //
    // Textual percentile output support:
    //
    //
    //

    /**
     * Get the percentile at a given value.
     * The percentile returned is the percentile of values recorded in the histogram that are smaller
     * than or equivalent to the given value.
     * <p>
     * Note that two values are "equivalent" in this statement if
     * {@link DoubleHistogram#valuesAreEquivalent} would return true.
     *
     * @param value The value for which to return the associated percentile
     * @return The percentile of values recorded in the histogram that are smaller than or equivalent
     * to the given value.
     */
    public double getPercentileAtOrBelowValue(final double value) {
        return integerValuesHistogram.getPercentileAtOrBelowValue((long) (value * doubleToIntegerValueConversionRatio));
    }

    /**
     * Get the count of recorded values within a range of value levels (inclusive to within the histogram's resolution).
     *
     * @param lowValue  The lower value bound on the range for which
     *                  to provide the recorded count. Will be rounded down with
     *                  {@link DoubleHistogram#lowestEquivalentValue lowestEquivalentValue}.
     * @param highValue The higher value bound on the range for which to provide the recorded count.
     *                  Will be rounded up with {@link DoubleHistogram#highestEquivalentValue highestEquivalentValue}.
     * @return the total count of values recorded in the histogram within the value range that is
     * {@literal >=} lowestEquivalentValue(<i>lowValue</i>) and {@literal <=} highestEquivalentValue(<i>highValue</i>)
     */
    public double getCountBetweenValues(final double lowValue, final double highValue)
            throws ArrayIndexOutOfBoundsException {
        return integerValuesHistogram.getCountBetweenValues(
                (long) (lowValue * doubleToIntegerValueConversionRatio),
                (long) (highValue * doubleToIntegerValueConversionRatio)
        );
    }

    //
    //
    //
    // Serialization support:
    //
    //
    //

    /**
     * Get the count of recorded values at a specific value (to within the histogram resolution at the value level).
     *
     * @param value The value for which to provide the recorded count
     * @return The total count of values recorded in the histogram within the value range that is
     * {@literal >=} lowestEquivalentValue(<i>value</i>) and {@literal <=} highestEquivalentValue(<i>value</i>)
     */
    public long getCountAtValue(final double value) throws ArrayIndexOutOfBoundsException {
        return integerValuesHistogram.getCountAtValue((long) (value * doubleToIntegerValueConversionRatio));
    }

    /**
     * Provide a means of iterating through histogram values according to percentile levels. The iteration is
     * performed in steps that start at 0% and reduce their distance to 100% according to the
     * <i>percentileTicksPerHalfDistance</i> parameter, ultimately reaching 100% when all recorded histogram
     * values are exhausted.
     * <p>
     *
     * @param percentileTicksPerHalfDistance The number of iteration steps per half-distance to 100%.
     * @return An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >}
     * through the histogram using a
     * {@link DoublePercentileIterator}
     */
    public Percentiles percentiles(final int percentileTicksPerHalfDistance) {
        return new Percentiles(this, percentileTicksPerHalfDistance);
    }

    /**
     * Provide a means of iterating through histogram values using linear steps. The iteration is
     * performed in steps of <i>valueUnitsPerBucket</i> in size, terminating when all recorded histogram
     * values are exhausted.
     *
     * @param valueUnitsPerBucket The size (in value units) of the linear buckets to use
     * @return An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >}
     * through the histogram using a
     * {@link DoubleLinearIterator}
     */
    public LinearBucketValues linearBucketValues(final double valueUnitsPerBucket) {
        return new LinearBucketValues(this, valueUnitsPerBucket);
    }

    //
    //
    //
    // Encoding/Decoding support:
    //
    //
    //

    /**
     * Provide a means of iterating through histogram values at logarithmically increasing levels. The iteration is
     * performed in steps that start at <i>valueUnitsInFirstBucket</i> and increase exponentially according to
     * <i>logBase</i>, terminating when all recorded histogram values are exhausted.
     *
     * @param valueUnitsInFirstBucket The size (in value units) of the first bucket in the iteration
     * @param logBase                 The multiplier by which bucket sizes will grow in each iteration step
     * @return An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >}
     * through the histogram using
     * a {@link DoubleLogarithmicIterator}
     */
    public LogarithmicBucketValues logarithmicBucketValues(final double valueUnitsInFirstBucket,
                                                           final double logBase) {
        return new LogarithmicBucketValues(this, valueUnitsInFirstBucket, logBase);
    }

    /**
     * Provide a means of iterating through all recorded histogram values using the finest granularity steps
     * supported by the underlying representation. The iteration steps through all non-zero recorded value counts,
     * and terminates when all recorded histogram values are exhausted.
     *
     * @return An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >}
     * through the histogram using
     * a {@link DoubleRecordedValuesIterator}
     */
    public RecordedValues recordedValues() {
        return new RecordedValues(this);
    }

    /**
     * Provide a means of iterating through all histogram values using the finest granularity steps supported by
     * the underlying representation. The iteration steps through all possible unit value levels, regardless of
     * whether or not there were recorded values for that value level, and terminates when all recorded histogram
     * values are exhausted.
     *
     * @return An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >}
     * through the histogram using a {@link DoubleAllValuesIterator}
     */
    public AllValues allValues() {
        return new AllValues(this);
    }

    /**
     * Produce textual representation of the value distribution of histogram data by percentile. The distribution is
     * output with exponentially increasing resolution, with each exponentially decreasing half-distance containing
     * five (5) percentile reporting tick points.
     *
     * @param printStream                 Stream into which the distribution will be output
     *                                    <p>
     * @param outputValueUnitScalingRatio The scaling factor by which to divide histogram recorded values units in
     *                                    output
     */
    public void outputPercentileDistribution(final PrintStream printStream,
                                             final Double outputValueUnitScalingRatio) {
        outputPercentileDistribution(printStream, 5, outputValueUnitScalingRatio);
    }

    /**
     * Produce textual representation of the value distribution of histogram data by percentile. The distribution is
     * output with exponentially increasing resolution, with each exponentially decreasing half-distance containing
     * <i>dumpTicksPerHalf</i> percentile reporting tick points.
     *
     * @param printStream                    Stream into which the distribution will be output
     *                                       <p>
     * @param percentileTicksPerHalfDistance The number of reporting points per exponentially decreasing half-distance
     *                                       <p>
     * @param outputValueUnitScalingRatio    The scaling factor by which to divide histogram recorded values units in
     *                                       output
     */
    public void outputPercentileDistribution(final PrintStream printStream,
                                             final int percentileTicksPerHalfDistance,
                                             final Double outputValueUnitScalingRatio) {
        outputPercentileDistribution(printStream, percentileTicksPerHalfDistance, outputValueUnitScalingRatio, false);
    }

    /**
     * Produce textual representation of the value distribution of histogram data by percentile. The distribution is
     * output with exponentially increasing resolution, with each exponentially decreasing half-distance containing
     * <i>dumpTicksPerHalf</i> percentile reporting tick points.
     *
     * @param printStream                    Stream into which the distribution will be output
     *                                       <p>
     * @param percentileTicksPerHalfDistance The number of reporting points per exponentially decreasing half-distance
     *                                       <p>
     * @param outputValueUnitScalingRatio    The scaling factor by which to divide histogram recorded values units in
     *                                       output
     * @param useCsvFormat                   Output in CSV format if true. Otherwise use plain text form.
     */
    public void outputPercentileDistribution(final PrintStream printStream,
                                             final int percentileTicksPerHalfDistance,
                                             final Double outputValueUnitScalingRatio,
                                             final boolean useCsvFormat) {
        integerValuesHistogram.outputPercentileDistribution(printStream,
                percentileTicksPerHalfDistance,
                outputValueUnitScalingRatio / integerToDoubleValueConversionRatio,
                useCsvFormat);
    }

    /**
     * Get the capacity needed to encode this histogram into a ByteBuffer
     *
     * @return the capacity needed to encode this histogram into a ByteBuffer
     */
    @Override
    public int getNeededByteBufferCapacity() {
        return integerValuesHistogram.getNeededByteBufferCapacity();
    }

    private int getNeededByteBufferCapacity(final int relevantLength) {
        return integerValuesHistogram.getNeededByteBufferCapacity(relevantLength);
    }

    /**
     * Encode this histogram into a ByteBuffer
     *
     * @param buffer The buffer to encode into
     * @return The number of bytes written to the buffer
     */
    synchronized public int encodeIntoByteBuffer(final ByteBuffer buffer) {
        long maxValue = integerValuesHistogram.getMaxValue();
        int relevantLength = integerValuesHistogram.getLengthForNumberOfBuckets(
                integerValuesHistogram.getBucketsNeededToCoverValue(maxValue));
        if (buffer.capacity() < getNeededByteBufferCapacity(relevantLength)) {
            throw new ArrayIndexOutOfBoundsException("buffer does not have capacity for " +
                    getNeededByteBufferCapacity(relevantLength) + " bytes");
        }
        buffer.putInt(DHIST_encodingCookie);
        buffer.putInt(getNumberOfSignificantValueDigits());
        buffer.putLong(configuredHighestToLowestValueRatio);
        return integerValuesHistogram.encodeIntoByteBuffer(buffer) + 16;
    }

    /**
     * Encode this histogram in compressed form into a byte array
     *
     * @param targetBuffer     The buffer to encode into
     * @param compressionLevel Compression level (for java.util.zip.Deflater).
     * @return The number of bytes written to the buffer
     */
    @Override
    synchronized public int encodeIntoCompressedByteBuffer(
            final ByteBuffer targetBuffer,
            final int compressionLevel) {
        targetBuffer.putInt(DHIST_compressedEncodingCookie);
        targetBuffer.putInt(getNumberOfSignificantValueDigits());
        targetBuffer.putLong(configuredHighestToLowestValueRatio);
        return integerValuesHistogram.encodeIntoCompressedByteBuffer(targetBuffer, compressionLevel) + 16;
    }

    /**
     * Encode this histogram in compressed form into a byte array
     *
     * @param targetBuffer The buffer to encode into
     * @return The number of bytes written to the array
     */
    public int encodeIntoCompressedByteBuffer(final ByteBuffer targetBuffer) {
        return encodeIntoCompressedByteBuffer(targetBuffer, Deflater.DEFAULT_COMPRESSION);
    }

    private long deriveInternalHighestToLowestValueRatio(final long externalHighestToLowestValueRatio) {
        // Internal dynamic range needs to be 1 order of magnitude larger than the containing order of magnitude.
        // e.g. the dynamic range that covers [0.9, 2.1) is 2.33x, which on it's own would require 4x range to
        // cover the contained order of magnitude. But (if 1.0 was a bucket boundary, for example, the range
        // will actually need to cover [0.5..1.0) [1.0..2.0) [2.0..4.0), mapping to an 8x internal dynamic range.
        long internalHighestToLowestValueRatio =
                1L << (findContainingBinaryOrderOfMagnitude(externalHighestToLowestValueRatio) + 1);
        return internalHighestToLowestValueRatio;
    }

    private long deriveIntegerValueRange(final long externalHighestToLowestValueRatio,
                                         final int numberOfSignificantValueDigits) {
        long internalHighestToLowestValueRatio =
                deriveInternalHighestToLowestValueRatio(externalHighestToLowestValueRatio);

        // We cannot use the bottom half of bucket 0 in an integer values histogram to represent double
        // values, because the required precision does not exist there. We therefore need the integer
        // range to be bigger, such that the entire double value range can fit in the upper halves of
        // all buckets. Compute the integer value range that will achieve this:

        long lowestTackingIntegerValue = AbstractHistogram.numberOfSubbuckets(numberOfSignificantValueDigits) / 2;
        long integerValueRange = lowestTackingIntegerValue * internalHighestToLowestValueRatio;

        return integerValueRange;
    }

    //
    //
    //
    // Internal helper methods:
    //
    //
    //

    private long getLowestTrackingIntegerValue() {
        return integerValuesHistogram.subBucketHalfCount;
    }

    private int findCappedContainingBinaryOrderOfMagnitude(final double doubleNumber) {
        if (doubleNumber > configuredHighestToLowestValueRatio) {
            return (int) (Math.log(configuredHighestToLowestValueRatio) / Math.log(2));
        }
        if (doubleNumber > Math.pow(2.0, 50)) {
            return 50;
        }
        return findContainingBinaryOrderOfMagnitude(doubleNumber);
    }

    /**
     * An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >} through
     * the histogram using a {@link DoublePercentileIterator}
     */
    public class Percentiles implements Iterable<DoubleHistogramIterationValue> {
        final DoubleHistogram histogram;
        final int percentileTicksPerHalfDistance;

        private Percentiles(final DoubleHistogram histogram, final int percentileTicksPerHalfDistance) {
            this.histogram = histogram;
            this.percentileTicksPerHalfDistance = percentileTicksPerHalfDistance;
        }

        /**
         * @return A {@link DoublePercentileIterator}{@literal <}{@link DoubleHistogramIterationValue}{@literal >}
         */
        public Iterator<DoubleHistogramIterationValue> iterator() {
            return new DoublePercentileIterator(histogram, percentileTicksPerHalfDistance);
        }
    }

    /**
     * An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >} through
     * the histogram using a {@link DoubleLinearIterator}
     */
    public class LinearBucketValues implements Iterable<DoubleHistogramIterationValue> {
        final DoubleHistogram histogram;
        final double valueUnitsPerBucket;

        private LinearBucketValues(final DoubleHistogram histogram, final double valueUnitsPerBucket) {
            this.histogram = histogram;
            this.valueUnitsPerBucket = valueUnitsPerBucket;
        }

        /**
         * @return A {@link DoubleLinearIterator}{@literal <}{@link DoubleHistogramIterationValue}{@literal >}
         */
        public Iterator<DoubleHistogramIterationValue> iterator() {
            return new DoubleLinearIterator(histogram, valueUnitsPerBucket);
        }
    }

    /**
     * An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >} through
     * the histogram using a {@link DoubleLogarithmicIterator}
     */
    public class LogarithmicBucketValues implements Iterable<DoubleHistogramIterationValue> {
        final DoubleHistogram histogram;
        final double valueUnitsInFirstBucket;
        final double logBase;

        private LogarithmicBucketValues(final DoubleHistogram histogram,
                                        final double valueUnitsInFirstBucket, final double logBase) {
            this.histogram = histogram;
            this.valueUnitsInFirstBucket = valueUnitsInFirstBucket;
            this.logBase = logBase;
        }

        /**
         * @return A {@link DoubleLogarithmicIterator}{@literal <}{@link DoubleHistogramIterationValue}{@literal >}
         */
        public Iterator<DoubleHistogramIterationValue> iterator() {
            return new DoubleLogarithmicIterator(histogram, valueUnitsInFirstBucket, logBase);
        }
    }

    /**
     * An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >} through
     * the histogram using a {@link DoubleRecordedValuesIterator}
     */
    public class RecordedValues implements Iterable<DoubleHistogramIterationValue> {
        final DoubleHistogram histogram;

        private RecordedValues(final DoubleHistogram histogram) {
            this.histogram = histogram;
        }

        /**
         * @return A {@link DoubleRecordedValuesIterator}{@literal <}{@link HistogramIterationValue}{@literal >}
         */
        public Iterator<DoubleHistogramIterationValue> iterator() {
            return new DoubleRecordedValuesIterator(histogram);
        }
    }

    /**
     * An {@link Iterable}{@literal <}{@link DoubleHistogramIterationValue}{@literal >} through
     * the histogram using a {@link DoubleAllValuesIterator}
     */
    public class AllValues implements Iterable<DoubleHistogramIterationValue> {
        final DoubleHistogram histogram;

        private AllValues(final DoubleHistogram histogram) {
            this.histogram = histogram;
        }

        /**
         * @return A {@link DoubleAllValuesIterator}{@literal <}{@link HistogramIterationValue}{@literal >}
         */
        public Iterator<DoubleHistogramIterationValue> iterator() {
            return new DoubleAllValuesIterator(histogram);
        }
    }
}