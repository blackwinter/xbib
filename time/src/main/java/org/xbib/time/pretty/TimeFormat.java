package org.xbib.time.pretty;

/**
 * Format a Duration object.
 */
public interface TimeFormat {
    /**
     * Given a populated {@link Duration} object. Apply formatting (with rounding) and output the result.
     *
     * @param duration The original {@link Duration} instance from which the time string should be decorated.
     */
    String format(final Duration duration);

    /**
     * Given a populated {@link Duration} object. Apply formatting (without rounding) and output the result.
     *
     * @param duration The original {@link Duration} instance from which the time string should be decorated.
     */
    String formatUnrounded(Duration duration);

    /**
     * Decorate with past or future prefix/suffix (with rounding)
     *
     * @param duration The original {@link Duration} instance from which the time string should be decorated.
     * @param time     The formatted time string.
     */
    String decorate(Duration duration, String time);

    /**
     * Decorate with past or future prefix/suffix (without rounding)
     *
     * @param duration The original {@link Duration} instance from which the time string should be decorated.
     * @param time     The formatted time string.
     */
    String decorateUnrounded(Duration duration, String time);

}