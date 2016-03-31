package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xbib.time.pretty.units.Hour;
import org.xbib.time.pretty.units.JustNow;
import org.xbib.time.pretty.units.Minute;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PrettyTimeUnitConfigurationTest {

    // Stores current locale so that it can be restored
    private Locale locale;

    // Method setUp() is called automatically before every test method
    @Before
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.ROOT);
    }

    @Test
    public void testRightNow() throws Exception {
        LocalDateTime ref = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        LocalDateTime then = LocalDateTime.ofInstant(Instant.ofEpochMilli(2), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(ref);
        TimeFormat format = t.removeUnit(JustNow.class);
        Assert.assertNotNull(format);
        assertEquals("2 milliseconds from now", t.format(then));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        TimeFormat format = t.removeUnit(Minute.class);
        Assert.assertNotNull(format);
        assertEquals("720 seconds from now", t.format(1000 * 60 * 12));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        TimeFormat format = t.removeUnit(Hour.class);
        Assert.assertNotNull(format);
        assertEquals("180 minutes from now", t.format(1000 * 60 * 60 * 3));
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
