package org.xbib.time.prettytime;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xbib.time.PrettyTime;
import org.xbib.time.TimeFormat;
import org.xbib.time.units.Hour;
import org.xbib.time.units.JustNow;
import org.xbib.time.units.Minute;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PrettyTimeUnitConfigurationTest {
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

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
        Date ref = new Date(0);
        Date then = new Date(2);

        PrettyTime t = new PrettyTime(ref);
        TimeFormat format = t.removeUnit(JustNow.class);
        Assert.assertNotNull(format);
        assertEquals("moments from now", t.format(then));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(new Date(0));
        TimeFormat format = t.removeUnit(Minute.class);
        Assert.assertNotNull(format);
        assertEquals("12 minutes from now", t.format(new Date(1000 * 60 * 12)));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        PrettyTime t = new PrettyTime(new Date(0));
        TimeFormat format = t.removeUnit(Hour.class);
        Assert.assertNotNull(format);
        assertEquals("3 hours from now", t.format(new Date(1000 * 60 * 60 * 3)));
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
