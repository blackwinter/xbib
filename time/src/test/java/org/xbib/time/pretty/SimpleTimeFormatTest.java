package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class SimpleTimeFormatTest {
    // Stores current locale so that it can be restored
    private Locale locale;

    // Method setUp() is called automatically before every test method
    @Before
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.ROOT);
    }

    @Test
    public void testRounding() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 3 + 1000 * 60 * 45);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        TimeUnitQuantity timeUnitQuantity = t.approximateDuration(localDateTime);
        assertEquals("4 hours ago", t.format(timeUnitQuantity));
        assertEquals("3 hours ago", t.formatUnrounded(timeUnitQuantity));
    }

    @Test
    public void testDecorating() throws Exception {
        PrettyTime t = new PrettyTime();
        TimeFormat format = new SimpleTimeFormat().setFutureSuffix("from now").setPastSuffix("ago");

        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(1);
        TimeUnitQuantity timeUnitQuantity = t.approximateDuration(localDateTime);
        assertEquals("some time from now", format.decorate(timeUnitQuantity, "some time"));

        localDateTime = LocalDateTime.now().minusSeconds(10);
        timeUnitQuantity = t.approximateDuration(localDateTime);
        assertEquals("some time ago", format.decorate(timeUnitQuantity, "some time"));
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
