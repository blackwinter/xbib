package org.xbib.time.prettytime.i18n;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xbib.time.PrettyTime;

import java.util.Date;
import java.util.Locale;

public class SimpleTimeFormatTimeQuantifiedNameTest {
    private Locale locale;

    @Before
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(new Locale("yy"));
    }

    @Test
    public void testFuturePluralName() throws Exception {
        PrettyTime p = new PrettyTime(0);
        Assert.assertEquals("2 days from now", p.format(new Date(1000 * 60 * 60 * 24 * 2)));
    }

    @Test
    public void testPastPluralName() throws Exception {
        PrettyTime p = new PrettyTime(1000 * 60 * 60 * 24 * 2);
        Assert.assertEquals("2 days ago", p.format(new Date(0)));
    }

    @Test
    public void testFutureSingularName() throws Exception {
        PrettyTime p = new PrettyTime(0);
        Assert.assertEquals("1 day from now", p.format(new Date(1000 * 60 * 60 * 24)));
    }

    @Test
    public void testPastSingularName() throws Exception {
        PrettyTime p = new PrettyTime(1000 * 60 * 60 * 24);
        Assert.assertEquals("1 day ago", p.format(new Date(0)));
    }

    @Test
    public void testFuturePluralNameEmpty() throws Exception {
        PrettyTime p = new PrettyTime(0);
        Assert.assertEquals("2 hours from now", p.format(new Date(1000 * 60 * 60 * 2)));
    }

    @Test
    public void testPastPluralNameMissing() throws Exception {
        PrettyTime p = new PrettyTime(1000 * 60 * 60 * 2);
        Assert.assertEquals("2 hours ago", p.format(new Date(0)));
    }

    @Test
    public void testFutureSingularNameCopy() throws Exception {
        PrettyTime p = new PrettyTime(0);
        Assert.assertEquals("1 hour from now", p.format(new Date(1000 * 60 * 60)));
    }

    @Test
    public void testPastSingularNameNull() throws Exception {
        PrettyTime p = new PrettyTime(1000 * 60 * 60);
        Assert.assertEquals("1 hour ago", p.format(new Date(0)));
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
