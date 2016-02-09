package org.xbib.time.pretty;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PrettyTimeI18n_SV_Test {
    private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    private Locale locale;

    @Before
    public void setUp() throws Exception {
        locale = new Locale("sv");
    }

    @Test
    public void testPrettyTime() {
        PrettyTime p = new PrettyTime(locale);
        assertEquals(p.format(new Date()), "om en stund");
    }

    @Test
    public void testPrettyTimeCenturies() {
        PrettyTime p = new PrettyTime((3155692597470L * 3L), locale);
        assertEquals("3 århundraden sedan", p.format(new Date(0)));

        p = new PrettyTime((0), locale);
        assertEquals("om 3 århundraden", p.format(new Date(3155692597470L * 3L)));
    }

    @Test
    public void testCeilingInterval() throws Exception {
        Date then = format.parse("20/5/2009");
        Date ref = format.parse("17/6/2009");
        PrettyTime t = new PrettyTime(ref.getTime(), locale);
        assertEquals("1 månad sedan", t.format(then));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDate() throws Exception {
        PrettyTime t = new PrettyTime(locale);
        Date date = null;
        assertEquals("om en stund", t.format(date));
    }

    @Test
    public void testRightNow() throws Exception {
        PrettyTime t = new PrettyTime(locale);
        assertEquals("om en stund", t.format(new Date()));
    }

    @Test
    public void testRightNowVariance() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("om en stund", t.format(new Date(600)));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("om 12 minuter", t.format(new Date(1000 * 60 * 12)));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("om 3 timmar", t.format(new Date(1000 * 60 * 60 * 3)));
    }

    @Test
    public void testDaysFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("om 3 dagar", t.format(new Date(1000 * 60 * 60 * 24 * 3)));
    }

    @Test
    public void testWeeksFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("om 3 veckor", t.format(new Date(1000 * 60 * 60 * 24 * 7 * 3)));
    }

    @Test
    public void testMonthsFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("om 3 månader", t.format(new Date(2629743830L * 3L)));
    }

    @Test
    public void testYearsFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("om 3 år", t.format(new Date(2629743830L * 12L * 3L)));
    }

    @Test
    public void testDecadesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("om 3 årtionden", t.format(new Date(315569259747L * 3L)));
    }

    @Test
    public void testCenturiesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("om 3 århundraden", t.format(new Date(3155692597470L * 3L)));
    }

    /*
     * Past
     */
    @Test
    public void testMomentsAgo() throws Exception {
        PrettyTime t = new PrettyTime((6000), locale);
        assertEquals("en stund sedan", t.format(new Date(0)));
    }

    @Test
    public void testMinutesAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 12), locale);
        assertEquals("12 minuter sedan", t.format(new Date(0)));
    }

    @Test
    public void testHoursAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 3), locale);
        assertEquals("3 timmar sedan", t.format(new Date(0)));
    }

    @Test
    public void testDaysAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 3), locale);
        assertEquals("3 dagar sedan", t.format(new Date(0)));
    }

    @Test
    public void testWeeksAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 7 * 3), locale);
        assertEquals("3 veckor sedan", t.format(new Date(0)));
    }

    @Test
    public void testMonthsAgo() throws Exception {
        PrettyTime t = new PrettyTime((2629743830L * 3L), locale);
        assertEquals("3 månader sedan", t.format(new Date(0)));
    }

    @Test
    public void testYearsAgo() throws Exception {
        PrettyTime t = new PrettyTime((2629743830L * 12L * 3L), locale);
        assertEquals("3 år sedan", t.format(new Date(0)));
    }

    @Test
    public void testDecadesAgo() throws Exception {
        PrettyTime t = new PrettyTime((315569259747L * 3L), locale);
        assertEquals("3 årtionden sedan", t.format(new Date(0)));
    }

    @Test
    public void testCenturiesAgo() throws Exception {
        PrettyTime t = new PrettyTime((3155692597470L * 3L), locale);
        assertEquals("3 århundraden sedan", t.format(new Date(0)));
    }
}
