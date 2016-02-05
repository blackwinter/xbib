package org.xbib.time.prettytime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbib.time.Duration;
import org.xbib.time.PrettyTime;
import org.xbib.time.TimeFormat;
import org.xbib.time.format.SimpleTimeFormat;

import java.util.Date;
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
        Duration duration = t.approximateDuration(new Date(0));

        assertEquals("4 hours ago", t.format(duration));
        assertEquals("3 hours ago", t.formatUnrounded(duration));
    }

    @Test
    public void testDecorating() throws Exception {
        PrettyTime t = new PrettyTime();
        TimeFormat format = new SimpleTimeFormat().setFutureSuffix("from now").setPastSuffix("ago");

        Duration duration = t.approximateDuration(new Date(System.currentTimeMillis() + 1000));
        assertEquals("some time from now", format.decorate(duration, "some time"));

        duration = t.approximateDuration(new Date(System.currentTimeMillis() - 10000));
        assertEquals("some time ago", format.decorate(duration, "some time"));
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
