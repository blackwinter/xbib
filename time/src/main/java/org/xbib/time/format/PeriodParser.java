package org.xbib.time.format;

import java.util.Locale;

/**
 * Internal interface for parsing textual representations of time periods.
 * Application users will rarely use this class directly. Instead, you
 * will use one of the factory classes to create a {@link PeriodFormatter}.
 * The factory classes are:
 * - {@link PeriodFormat}
 * - {@link ISOPeriodFormat}
 *
 */
public interface PeriodParser {

    /**
     * Parses a period from the given text, at the given position, saving the
     * result into the given PeriodAmount. If the parse
     * succeeds, the return value is the new text position. Note that the parse
     * may succeed without fully reading the text.
     * If it fails, the return value is negative, but the period may still be
     * modified. To determine the position where the parse failed, apply the
     * one's complement operator (~) on the return value.
     *
     * @param amount the period amount
     * @param periodStr  text to parse
     * @param position position to start parsing from
     * @param locale  the locale to use for parsing
     * @return new position, if negative, parse failed. Apply complement
     * operator (~) to get position of failure
     * @throws IllegalArgumentException if any field is out of range
     */
    int parseInto(PeriodAmount amount, String periodStr, int position, Locale locale);

}