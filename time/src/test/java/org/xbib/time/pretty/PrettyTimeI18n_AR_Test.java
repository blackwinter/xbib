package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrettyTimeI18n_AR_Test {
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

    // Stores current locale so that it can be restored
    private Locale locale;

    // Method setUp() is called automatically before every test method
    @Before
    public void setUp() throws Exception {
        locale = new Locale("ar");
        Locale.setDefault(locale);
    }

    @Test
    public void testCeilingInterval() throws Exception {
        Date then = format.parse("5/20/2009");
        Date ref = format.parse("6/17/2009");
        PrettyTime t = new PrettyTime(ref.getTime());
        assertEquals("1 شهر مضت", t.format(then));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDate() throws Exception {
        PrettyTime t = new PrettyTime();
        Date date = null;
        assertEquals("بعد لحظات", t.format(date));
    }

    @Test
    public void testRightNow() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("بعد لحظات", t.format(new Date()));
    }

    @Test
    public void testRightNowVariance() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("بعد لحظات", t.format(new Date(600)));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("12 دقائق من الآن", t.format(new Date(1000 * 60 * 12)));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("3 ساعات من الآن", t.format(new Date(1000 * 60 * 60 * 3)));
    }

    @Test
    public void testDaysFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("3 ايام من الآن", t.format(new Date(1000 * 60 * 60 * 24 * 3)));
    }

    @Test
    public void testWeeksFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("3 أسابيع من الآن", t.format(new Date(1000 * 60 * 60 * 24 * 7 * 3)));
    }

    @Test
    public void testMonthsFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("3 أشهر من الآن", t.format(new Date(2629743830L * 3L)));
    }

    @Test
    public void testYearsFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("3 سنوات من الآن", t.format(new Date(2629743830L * 12L * 3L)));
    }

    @Test
    public void testDecadesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("3 عقود من الآن", t.format(new Date(315569259747L * 3L)));
    }

    @Test
    public void testCenturiesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("3 قرون من الآن", t.format(new Date(3155692597470L * 3L)));
    }

    @Test
    public void testMomentsAgo() throws Exception {
        PrettyTime t = new PrettyTime(6000);
        assertEquals("منذ لحظات", t.format(new Date(0)));
    }

    @Test
    public void testMinutesAgo() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 12);
        assertEquals("12 دقائق مضت", t.format(new Date(0)));
    }

    @Test
    public void testHoursAgo() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 3);
        assertEquals("3 ساعات مضت", t.format(new Date(0)));
    }

    @Test
    public void testDaysAgo() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 24 * 3);
        assertEquals("3 ايام مضت", t.format(new Date(0)));
    }

    @Test
    public void testWeeksAgo() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 24 * 7 * 3);
        assertEquals("3 أسابيع مضت", t.format(new Date(0)));
    }

    @Test
    public void testMonthsAgo() throws Exception {
        PrettyTime t = new PrettyTime(2629743830L * 3L);
        assertEquals("3 أشهر مضت", t.format(new Date(0)));
    }

    @Test
    public void testCustomFormat() throws Exception {
        PrettyTime t = new PrettyTime(0);
        TimeUnit unit = new TimeUnit() {
            @Override
            public long getMaxQuantity() {
                return 0;
            }

            @Override
            public long getMillisPerUnit() {
                return 5000;
            }
        };
        t.clearUnits();
        t.registerUnit(unit, new SimpleTimeFormat()
                .setSingularName("tick").setPluralName("ticks")
                .setPattern("%n %u").setRoundingTolerance(20)
                .setFutureSuffix("... RUN!")
                .setFuturePrefix("self destruct in: ").setPastPrefix("self destruct was: ").setPastSuffix(
                        " ago..."));

        assertEquals("self destruct in: 5 ticks ... RUN!", t.format(new Date(25000)));
        t.setReference(25000);
        assertEquals("self destruct was: 5 ticks ago...", t.format(new Date(0)));
    }

    @Test
    public void testYearsAgo() throws Exception {
        PrettyTime t = new PrettyTime(2629743830L * 12L * 3L);
        assertEquals("3 سنوات مضت", t.format(new Date(0)));
    }

    @Test
    public void testDecadesAgo() throws Exception {
        PrettyTime t = new PrettyTime(315569259747L * 3L);
        assertEquals("3 عقود مضت", t.format(new Date(0)));
    }

    @Test
    public void testCenturiesAgo() throws Exception {
        PrettyTime t = new PrettyTime(3155692597470L * 3L);
        assertEquals("3 قرون مضت", t.format(new Date(0)));
    }

    @Test
    public void testWithinTwoHoursRounding() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("2 ساعات مضت", t.format(new Date(new Date().getTime() - 6543990)));
    }

    @Test
    public void testPreciseInTheFuture() throws Exception {
        PrettyTime t = new PrettyTime();
        List<Duration> durations = t.calculatePreciseDuration(new Date(new Date().getTime() + 1000
                * (10 * 60 + 5 * 60 * 60)));
        assertTrue(durations.size() >= 2); // might be more because of milliseconds between date capturing and result
        // calculation
        assertEquals(5, durations.get(0).getQuantity());
        assertEquals(10, durations.get(1).getQuantity());
    }

    @Test
    public void testPreciseInThePast() throws Exception {
        PrettyTime t = new PrettyTime();
        List<Duration> durations = t.calculatePreciseDuration(new Date(new Date().getTime() - 1000
                * (10 * 60 + 5 * 60 * 60)));
        assertTrue(durations.size() >= 2); // might be more because of milliseconds between date capturing and result
        // calculation
        assertEquals(-5, durations.get(0).getQuantity());
        assertEquals(-10, durations.get(1).getQuantity());
    }

    @Test
    public void testFormattingDurationListInThePast() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15 + 1000 * 60 * 38);
        List<Duration> durations = t.calculatePreciseDuration(new Date(0));
        assertEquals("3 ايام 15 ساعات 38 دقائق مضت", t.format(durations));
    }

    @Test
    public void testFormattingDurationListInTheFuture() throws Exception {
        PrettyTime t = new PrettyTime(0);
        List<Duration> durations = t.calculatePreciseDuration(new Date(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15
                + 1000 * 60 * 38));
        assertEquals("3 ايام 15 ساعات 38 دقائق من الآن", t.format(durations));
    }

    @Test
    public void testSetLocale() throws Exception {
        PrettyTime t = new PrettyTime(315569259747L * 3L);
        assertEquals("3 عقود مضت", t.format(new Date(0)));
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
