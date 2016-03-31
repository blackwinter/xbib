package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrettyTimeI18n_AR_Test {

    private Locale locale;

    @Before
    public void setUp() throws Exception {
        locale = new Locale("ar");
        Locale.setDefault(locale);
    }

    @Test
    public void testCeilingInterval() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.of(2009, 6, 17, 0, 0);
        PrettyTime p = new PrettyTime(localDateTime);
        assertEquals("1 شهر مضت", p.format(LocalDateTime.of(2009, 5, 20, 0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDate() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("بعد لحظات", t.format((LocalDateTime)null));
    }

    @Test
    public void testRightNow() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("بعد لحظات", t.format(LocalDateTime.now()));
    }

    @Test
    public void testRightNowVariance() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("بعد لحظات", t.format(600));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("12 دقائق من الآن", t.format(1000 * 60 * 12));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 ساعات من الآن", t.format(1000 * 60 * 60 * 3));
    }

    @Test
    public void testDaysFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 ايام من الآن", t.format(1000 * 60 * 60 * 24 * 3));
    }

    @Test
    public void testWeeksFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 أسابيع من الآن", t.format(1000 * 60 * 60 * 24 * 7 * 3));
    }

    @Test
    public void testMonthsFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 أشهر من الآن", t.format(2629743830L * 3L));
    }

    @Test
    public void testYearsFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 سنوات من الآن", t.format(2629743830L * 12L * 3L));
    }

    @Test
    public void testDecadesFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 عقود من الآن", t.format(315569259747L * 3L));
    }

    @Test
    public void testCenturiesFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 قرون من الآن", t.format(3155692597470L * 3L));
    }

    @Test
    public void testMomentsAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(6000), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("منذ لحظات", t.format(0));
    }

    @Test
    public void testMinutesAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 12), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("12 دقائق مضت", t.format(0));
    }

    @Test
    public void testHoursAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 3), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 ساعات مضت", t.format(0));
    }

    @Test
    public void testDaysAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 3), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 ايام مضت", t.format(0));
    }

    @Test
    public void testWeeksAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 7 * 3), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 أسابيع مضت", t.format(0));
    }

    @Test
    public void testMonthsAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(2629743830L * 3L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 أشهر مضت", t.format(0));
    }

    @Test
    public void testCustomFormat() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
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

        assertEquals("self destruct in: 5 ticks ... RUN!", t.format(25000));
        localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(25000), ZoneId.systemDefault());
        t.setReference(localDateTime);
        assertEquals("self destruct was: 5 ticks ago...", t.format(0));
    }

    @Test
    public void testYearsAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(2629743830L * 12L * 3L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 سنوات مضت", t.format(0));
    }

    @Test
    public void testDecadesAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(315569259747L * 3L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 عقود مضت", t.format(0));
    }

    @Test
    public void testCenturiesAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(3155692597470L * 3L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 قرون مضت", t.format(0));
    }

    @Test
    public void testWithinTwoHoursRounding() throws Exception {
        PrettyTime t = new PrettyTime();
        LocalDateTime localDateTime = LocalDateTime.now().minus(6543990, ChronoField.MILLI_OF_SECOND.getBaseUnit());
        assertEquals("2 ساعات مضت", t.format(localDateTime));
    }

    @Test
    public void testPreciseInTheFuture() throws Exception {
        PrettyTime t = new PrettyTime();
        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(10 * 60 + 5 * 60 * 60);
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(localDateTime);
        assertTrue(timeUnitQuantities.size() >= 2);
        assertEquals(5, timeUnitQuantities.get(0).getQuantity());
        assertEquals(10, timeUnitQuantities.get(1).getQuantity());
    }

    @Test
    public void testPreciseInThePast() throws Exception {
        PrettyTime t = new PrettyTime();
        LocalDateTime localDateTime = LocalDateTime.now().minusSeconds(10 * 60 + 5 * 60 * 60);
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(localDateTime);
        assertTrue(timeUnitQuantities.size() >= 2);
        assertEquals(-5, timeUnitQuantities.get(0).getQuantity());
        assertEquals(-10, timeUnitQuantities.get(1).getQuantity());
    }

    @Test
    public void testFormattingDurationListInThePast() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15 + 1000 * 60 * 38), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()));
        assertEquals("3 ايام 15 ساعات 38 دقائق مضت", t.format(timeUnitQuantities));
    }

    @Test
    public void testFormattingDurationListInTheFuture() throws Exception {
        PrettyTime t = new PrettyTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15 + 1000 * 60 * 38), ZoneId.systemDefault());
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(localDateTime);
        assertEquals("3 ايام 15 ساعات 38 دقائق من الآن", t.format(timeUnitQuantities));
    }

    @Test
    public void testSetLocale() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(315569259747L * 3L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("3 عقود مضت", t.format(0));
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
