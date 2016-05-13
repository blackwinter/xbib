package org.xbib.time.pretty;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrettyTimeI18n_CA_Test {

    protected static Locale locale;

    @BeforeClass
    public static void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(new Locale("ca"));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

    @Test
    public void testCeilingInterval() throws Exception {
        LocalDateTime then = LocalDateTime.of(2009, 5, 20, 0, 0);
        LocalDateTime ref = LocalDateTime.of(2009, 6, 17, 0, 0);
        PrettyTime t = new PrettyTime(ref, new Locale("ca"));
        assertEquals("fa 1 mes", t.format(then));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDate() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("en un instant", t.format((LocalDateTime)null));
    }

    @Test
    public void testRightNow() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("en un instant", t.format(LocalDateTime.now()));
    }

    @Test
    public void testRightNowVariance() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("en un instant", t.format(600));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("dintre de 12 minuts", t.format((1000 * 60 * 12)));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("dintre de 3 hores", t.format((1000 * 60 * 60 * 3)));
    }

    @Test
    public void testDaysFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("dintre de 3 dies", t.format((1000 * 60 * 60 * 24 * 3)));
    }

    @Test
    public void testWeeksFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("dintre de 3 setmanes", t.format((1000 * 60 * 60 * 24 * 7 * 3)));
    }

    @Test
    public void testMonthsFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("dintre de 3 mesos", t.format((2629743830L * 3L)));
    }

    @Test
    public void testYearsFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("dintre de 3 anys", t.format((2629743830L * 12L * 3L)));
    }

    @Test
    public void testDecadesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("dintre de 3 desenis", t.format((315569259747L * 3L)));
    }

    @Test
    public void testCenturiesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("dintre de 3 segles", t.format((3155692597470L * 3L)));
    }

    @Test
    public void testMomentsAgo() throws Exception {
        PrettyTime t = new PrettyTime(6000);
        assertEquals("fa uns instants", t.format((0)));
    }

    @Test
    public void testMinutesAgo() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 12);
        assertEquals("fa 12 minuts", t.format((0)));
    }

    @Test
    public void testHoursAgo() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 3);
        assertEquals("fa 3 hores", t.format((0)));
    }

    @Test
    public void testDaysAgo() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 24 * 3);
        assertEquals("fa 3 dies", t.format((0)));
    }

    @Test
    public void testWeeksAgo() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 24 * 7 * 3);
        assertEquals("fa 3 setmanes", t.format((0)));
    }

    @Test
    public void testMonthsAgo() throws Exception {
        PrettyTime t = new PrettyTime(2629743830L * 3L);
        assertEquals("fa 3 mesos", t.format((0)));
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

        assertEquals("self destruct in: 5 ticks ... RUN!", t.format((25000)));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(25000), ZoneId.systemDefault());
        t.setReference(localDateTime);
        assertEquals("self destruct was: 5 ticks ago...", t.format((0)));
    }

    @Test
    public void testYearsAgo() throws Exception {
        PrettyTime t = new PrettyTime(2629743830L * 12L * 3L);
        assertEquals("fa 3 anys", t.format((0)));
    }

    @Test
    public void testDecadesAgo() throws Exception {
        PrettyTime t = new PrettyTime(315569259747L * 3L);
        assertEquals("fa 3 desenis", t.format((0)));
    }

    @Test
    public void testCenturiesAgo() throws Exception {
        PrettyTime t = new PrettyTime(3155692597470L * 3L);
        assertEquals("fa 3 segles", t.format((0)));
    }

    @Test
    public void testWithinTwoHoursRounding() throws Exception {
        PrettyTime t = new PrettyTime();
        LocalDateTime localDateTime = LocalDateTime.now().minusHours(2);
        assertEquals("fa 2 hores", t.format(localDateTime));
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
        assertTrue(-10 == timeUnitQuantities.get(1).getQuantity() || -9 == timeUnitQuantities.get(1).getQuantity());
    }

    @Test
    public void testFormattingDurationListInThePast() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15 + 1000 * 60 * 38);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(localDateTime);
        assertEquals("fa 3 dies 15 hores 38 minuts", t.format(timeUnitQuantities));
    }

    @Test
    public void testFormattingDurationListInTheFuture() throws Exception {
        PrettyTime t = new PrettyTime(0);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15
        + 1000 * 60 * 38), ZoneId.systemDefault());
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(localDateTime);
        assertEquals("dintre de 3 dies 15 hores 38 minuts", t.format(timeUnitQuantities));
    }

    @Test
    public void testSetLocale() throws Exception {
        PrettyTime t = new PrettyTime(315569259747L * 3L);
        assertEquals("fa 3 desenis", t.format((0)));
        t.setLocale(Locale.GERMAN);
        assertEquals("vor 3 Jahrzehnten", t.format((0)));
    }

}
