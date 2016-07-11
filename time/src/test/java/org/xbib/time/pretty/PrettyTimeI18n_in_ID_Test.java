package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrettyTimeI18n_in_ID_Test {

    private Locale locale;

    @Before
    public void setUp() throws Exception {
        locale = new Locale("in", "ID");
        Locale.setDefault(locale);
    }

    @Test
    public void testLocaleISOCorrectness() {
        assertEquals("in", this.locale.getLanguage());
        assertEquals("ID", this.locale.getCountry());
        assertEquals("Bahasa Indonesia", this.locale.getDisplayLanguage());
        assertEquals("Indonesia", this.locale.getDisplayCountry());
    }

    @Test
    public void testNow() {
        PrettyTime prettyTime = new PrettyTime(locale);
        assertEquals("dari sekarang", prettyTime.format(LocalDateTime.now()));
    }

    @Test
    public void testCeilingInterval() throws Exception {
        LocalDateTime then = LocalDateTime.of(2009, 5, 20, 0, 0);
        LocalDateTime ref = LocalDateTime.of(2009, 6, 17, 0, 0);
        PrettyTime t = new PrettyTime(ref);
        assertEquals("1 bulan yang lalu", t.format(then));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDate() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("moments from now", t.format((LocalDateTime)null));
    }

    @Test
    public void testRightNow() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("dari sekarang", t.format(LocalDateTime.now()));
    }

    @Test
    public void testRightNowVariance() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("dari sekarang", t.format((600)));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("12 menit dari sekarang", t.format((1000 * 60 * 12)));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("3 jam dari sekarang", t.format((1000 * 60 * 60 * 3)));
    }

    @Test
    public void testDaysFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 hari dari sekarang",
                t.format((1000 * 60 * 60 * 24 * 3)));
    }

    @Test
    public void testWeeksFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 minggu dari sekarang",
                t.format((1000 * 60 * 60 * 24 * 7 * 3)));
    }

    @Test
    public void testMonthsFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 bulan dari sekarang", t.format((2629743830L * 3L)));
    }

    @Test
    public void testYearsFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 tahun dari sekarang",
                t.format((2629743830L * 12L * 3L)));
    }

    @Test
    public void testDecadesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 dekade dari sekarang",
                t.format((315569259747L * 3L)));
    }

    @Test
    public void testCenturiesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0));
        assertEquals("3 abad dari sekarang",
                t.format((3155692597470L * 3L)));
    }

    /*
     * Past
     */
    @Test
    public void testMomentsAgo() throws Exception {
        PrettyTime t = new PrettyTime((6000));
        assertEquals("yang lalu", t.format((0)));
    }

    @Test
    public void testMinutesAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 12));
        assertEquals("12 menit yang lalu", t.format((0)));
    }

    @Test
    public void testHoursAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 3));
        assertEquals("3 jam yang lalu", t.format((0)));
    }

    @Test
    public void testDaysAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 3));
        assertEquals("3 hari yang lalu", t.format((0)));
    }

    @Test
    public void testWeeksAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 7 * 3));
        assertEquals("3 minggu yang lalu", t.format((0)));
    }

    @Test
    public void testMonthsAgo() throws Exception {
        PrettyTime t = new PrettyTime((2629743830L * 3L));
        assertEquals("3 bulan yang lalu", t.format((0)));
    }

    @Test
    public void testCustomFormat() throws Exception {
        PrettyTime t = new PrettyTime((0));
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
        t.registerUnit(unit, new SimpleTimeFormat().setSingularName("hitungan")
                        .setPluralName("hitungan").setPattern("%n %u")
                        .setRoundingTolerance(20).setFutureSuffix("... LARI!")
                        .setFuturePrefix("hancur dalam: ")
                        .setPastPrefix("telah hancur dalam: ")
                        .setPastSuffix(""));

        assertEquals("hancur dalam: 5 hitungan ... LARI!", t.format((25000)));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(25000), ZoneId.systemDefault());
        t.setReference(localDateTime);
        assertEquals("telah hancur dalam: 5 hitungan", t.format((0)));
    }

    @Test
    public void testYearsAgo() throws Exception {
        PrettyTime t = new PrettyTime((2629743830L * 12L * 3L));
        assertEquals("3 tahun yang lalu", t.format((0)));
    }

    @Test
    public void testDecadesAgo() throws Exception {
        PrettyTime t = new PrettyTime((315569259747L * 3L));
        assertEquals("3 dekade yang lalu", t.format((0)));
    }

    @Test
    public void testCenturiesAgo() throws Exception {
        PrettyTime t = new PrettyTime((3155692597470L * 3L));
        assertEquals("3 abad yang lalu", t.format((0)));
    }

    @Test
    public void testWithinTwoHoursRounding() throws Exception {
        PrettyTime t = new PrettyTime();
        LocalDateTime localDateTime = LocalDateTime.now().minusSeconds(6544);
        assertEquals("2 jam yang lalu", t.format(localDateTime));
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
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 3 + 1000
                * 60 * 60 * 15 + 1000 * 60 * 38));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(localDateTime);
        assertEquals("3 hari 15 jam 38 menit yang lalu", t.format(timeUnitQuantities));
    }

    @Test
    public void testFormattingDurationListInTheFuture() throws Exception {
        PrettyTime t = new PrettyTime((0));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15
                + 1000 * 60 * 38), ZoneId.systemDefault());
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(localDateTime);
        assertEquals("3 hari 15 jam 38 menit dari sekarang", t.format(timeUnitQuantities));
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
    }
}
