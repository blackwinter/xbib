package org.xbib.time.format;

import org.junit.Test;

import java.time.Period;

import static org.junit.Assert.assertEquals;

public class PeriodFormatterBuilderTest {

    @Test
    public void test() {
        PeriodFormatter yearsAndMonths = new PeriodFormatterBuilder()
                     .appendYears()
                     .appendSuffix(" year", " years")
                     .appendSeparator(" and ")
                     .appendMonths()
                     .appendSuffix(" month", " months")
                     .toFormatter();

        assertEquals("3 years and 2 months",yearsAndMonths.print(Period.of(3, 2, 1)));

    }
}
