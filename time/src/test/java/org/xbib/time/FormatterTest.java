package org.xbib.time;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatterTest {

    @Test
    public void testTimestampPattern() {
        String pattern = "yyyyMMdd";
        String name = DateTimeFormatter.ofPattern(pattern)
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
        System.err.println("name 1 = " + name);
        name = DateTimeFormatter.ofPattern(pattern)
                .withZone(ZoneId.systemDefault())
                .withLocale(Locale.getDefault())
                .format(LocalDate.now());
        System.err.println("name 2 = " + name);
    }
}
