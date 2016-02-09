package org.xbib.time;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class FormatterTest {

    @Test
    public void testLocalDate() {
        String pattern = "yyyyMMdd";
        String name1 = DateTimeFormatter.ofPattern(pattern)
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
        String name2 = DateTimeFormatter.ofPattern(pattern)
                .withZone(ZoneId.systemDefault())
                .withLocale(Locale.getDefault())
                .format(LocalDate.now());
        assertEquals(name1, name2);
    }
}
