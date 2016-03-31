package org.xbib.time.pretty;

/**
 * Format a Duration object.
 */
public interface TimeFormat {
    /**
     * Given a populated {@link TimeUnitQuantity} object. Apply formatting (with rounding) and output the result.
     *
     * @param timeUnitQuantity The original {@link TimeUnitQuantity} instance from which the time string should be decorated.
     */
    String format(final TimeUnitQuantity timeUnitQuantity);

    /**
     * Given a populated {@link TimeUnitQuantity} object. Apply formatting (without rounding) and output the result.
     *
     * @param timeUnitQuantity The original {@link TimeUnitQuantity} instance from which the time string should be decorated.
     */
    String formatUnrounded(TimeUnitQuantity timeUnitQuantity);

    /**
     * Decorate with past or future prefix/suffix (with rounding)
     *
     * @param timeUnitQuantity The original {@link TimeUnitQuantity} instance from which the time string should be decorated.
     * @param time     The formatted time string.
     */
    String decorate(TimeUnitQuantity timeUnitQuantity, String time);

    /**
     * Decorate with past or future prefix/suffix (without rounding)
     *
     * @param timeUnitQuantity The original {@link TimeUnitQuantity} instance from which the time string should be decorated.
     * @param time     The formatted time string.
     */
    String decorateUnrounded(TimeUnitQuantity timeUnitQuantity, String time);

}