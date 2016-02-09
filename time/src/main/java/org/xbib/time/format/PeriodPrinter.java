package org.xbib.time.format;

import java.io.IOException;
import java.io.Writer;
import java.time.Period;
import java.util.Locale;

/**
 * Internal interface for printing textual representations of time periods.
 * Application users will rarely use this class directly. Instead, you
 * will use one of the factory classes to create a {@link PeriodFormatter}.
 * The factory classes are:<br />
 * - {@link PeriodFormat}<br />
 * - {@link ISOPeriodFormat}<br />
 *
 */
public interface PeriodPrinter {

    /**
     * Returns the exact number of characters produced for the given period.
     * 
     * @param period  the period to use
     * @param locale  the locale to use
     * @return the estimated length
     */
    int calculatePrintedLength(Period period, Locale locale);

    /**
     * Returns the amount of fields from the given period that this printer
     * will print.
     * 
     * @param period  the period to use
     * @param stopAt stop counting at this value, enter a number &ge; 256 to count all
     * @param locale  the locale to use
     * @return amount of fields printed
     */
    int countFieldsToPrint(Period period, int stopAt, Locale locale);

    /**
     * Prints a ReadablePeriod to a StringBuilder.
     *
     * @param buf  the formatted period is appended to this buffer
     * @param period  the period to format
     * @param locale  the locale to use
     */
    void printTo(StringBuilder buf, Period period, Locale locale);

    /**
     * Prints a ReadablePeriod to a Writer.
     *
     * @param out  the formatted period is written out
     * @param period  the period to format
     * @param locale  the locale to use
     */
    void printTo(Writer out, Period period, Locale locale) throws IOException;

}