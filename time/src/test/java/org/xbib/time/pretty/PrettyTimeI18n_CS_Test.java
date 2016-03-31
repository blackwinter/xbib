package org.xbib.time.pretty;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xbib.time.pretty.units.JustNow;
import org.xbib.time.pretty.units.Month;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PrettyTimeI18n_CS_Test {
    private static Locale locale;

    @BeforeClass
    public static void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(new Locale("cs"));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

    @Test
    public void testCeilingInterval() throws Exception {
        LocalDateTime then = LocalDateTime.of(2009, 5, 20, 0, 0);
        LocalDateTime ref = LocalDateTime.of(2009, 6, 17, 0, 0);
        PrettyTime t = new PrettyTime(ref);
        assertEquals("před 1 měsícem", t.format(then));
    }

    @Test
    public void testRightNow() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("za chvíli", t.format(LocalDateTime.now()));
    }

    @Test
    public void testRightNowVariance() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("za chvíli", t.format(600));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        for (TimeUnit u : t.getUnits()) {
            if (u instanceof JustNow) {
                ((JustNow) u).setMaxQuantity(1000L);
            }
        }
        assertEquals("za 1 minutu", t.format(1000 * 60 ));
        assertEquals("za 3 minuty", t.format(1000 * 60 * 3));
        assertEquals("za 12 minut", t.format(1000 * 60 * 12));
    }

    @Test
    public void testHoursFromNow() throws Exception {

        PrettyTime t = new PrettyTime(0);
        assertEquals("za 1 hodinu", t.format(1000 * 60 * 60 ));
        assertEquals("za 3 hodiny", t.format(1000 * 60 * 60 * 3));
        assertEquals("za 10 hodin", t.format(1000 * 60 * 60 * 10));
    }

    @Test
    public void testDaysFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("za 1 den", t.format(1000 * 60 * 60 * 24));
        assertEquals("za 3 dny", t.format(1000 * 60 * 60 * 24 * 3));
        assertEquals("za 5 dní", t.format(1000 * 60 * 60 * 24 * 5));
    }

    @Test
    public void testWeeksFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        for (TimeUnit u : t.getUnits()) {
            if (u instanceof Month) {
                t.removeUnit(u);
            }
        }
        assertEquals("za 1 týden", t.format(1000 * 60 * 60 * 24 * 7L));
        assertEquals("za 3 týdny", t.format(1000 * 60 * 60 * 24 * 7 * 3L));
        assertEquals("za 5 týdnů", t.format(1000 * 60 * 60 * 24 * 7 * 5L));
    }

    @Test
    public void testMonthsFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("za 1 měsíc", t.format(2629743830L));
        assertEquals("za 3 měsíce", t.format(2629743830L * 3L));
        assertEquals("za 6 měsíců", t.format(2629743830L * 6L));
    }

    @Test
    public void testYearsFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("za 1 rok", t.format(2629743830L * 12L));
        assertEquals("za 3 roky", t.format(2629743830L * 12L * 3L));
        assertEquals("za 9 let", t.format(2629743830L * 12L * 9L));
    }

    @Test
    public void testDecadesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("za 3 desetiletí", t.format(315569259747L * 3L));
    }

    @Test
    public void testCenturiesFromNow() throws Exception {
        PrettyTime t = new PrettyTime(0);
        assertEquals("za 3 století", t.format(3155692597470L * 3L));
    }

    /*
     * Past
     */
    @Test
    public void testMomentsAgo() throws Exception {
        PrettyTime t = new PrettyTime(6000);
        assertEquals("před chvílí", t.format(0));
    }

    @Test
    public void testMinutesAgo() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 12);
        assertEquals("před 12 minutami", t.format(0));
    }

    @Test
    public void testHoursAgo() throws Exception {
        LocalDateTime base = LocalDateTime.now();
        PrettyTime t = new PrettyTime(base);
        assertEquals("před 1 hodinou", t.format(base.minusHours(1)));
        assertEquals("před 3 hodinami", t.format(base.minusHours(3)));
    }

    @Test
    public void testDaysAgo() throws Exception {
        LocalDateTime base = LocalDateTime.now();
        PrettyTime t = new PrettyTime(base);
        assertEquals("před 1 dnem", t.format(base.minusDays(1)));
        assertEquals("před 3 dny", t.format(base.minusDays(3)));
    }

    @Test
    public void testWeeksAgo() throws Exception {
        LocalDateTime base = LocalDateTime.now();
        PrettyTime t = new PrettyTime(base);
        assertEquals("před 1 týdnem", t.format(base.minusWeeks(1)));
        assertEquals("před 3 týdny", t.format(base.minusWeeks(3)));
    }

    @Test
    public void testMonthsAgo() throws Exception {
        LocalDateTime base = LocalDateTime.now();
        PrettyTime t = new PrettyTime(base);

        assertEquals("před 1 měsícem", t.format(base.minusMonths(1)));
        assertEquals("před 3 měsíci", t.format(base.minusMonths(3)));
    }

    @Test
    public void testYearsAgo() throws Exception {
        LocalDateTime base = LocalDateTime.now();
        PrettyTime t = new PrettyTime(base);
        for (TimeUnit u : t.getUnits()) {
            if (u instanceof Month) {
                t.removeUnit(u);
            }
        }
        assertEquals("před 1 rokem", t.format(base.minusYears(1)));
        assertEquals("před 3 roky", t.format(base.minusYears(3)));
    }

    @Test
    public void testDecadesAgo() throws Exception {
        PrettyTime t = new PrettyTime(315569259747L * 3L);
        assertEquals("před 3 desetiletími", t.format(0));
    }

    @Test
    public void testCenturiesAgo() throws Exception {
        PrettyTime t = new PrettyTime(3155692597470L * 3L);
        assertEquals("před 3 stoletími", t.format(0));
    }

    @Test
    public void testFormattingDurationListInThePast() throws Exception {
        PrettyTime t = new PrettyTime(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15 + 1000 * 60 * 38);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(localDateTime);
        assertEquals("před 3 dny 15 hodinami 38 minutami", t.format(timeUnitQuantities));
    }

    @Test
    public void testFormattingDurationListInTheFuture() throws Exception {
        PrettyTime t = new PrettyTime(0);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15 + 1000 * 60 * 38), ZoneId.systemDefault());
        List<TimeUnitQuantity> timeUnitQuantities = t.calculatePreciseDuration(localDateTime);
        assertEquals("za 3 dny 15 hodin 38 minut", t.format(timeUnitQuantities));
    }

    /**
     * Tests formatApproximateDuration and by proxy, formatDuration.
     *
     * @throws Exception
     */
    @Test
    public void testFormatApproximateDuration() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime tenMinAgo = localDateTime.minus(10, ChronoField.MINUTE_OF_DAY.getBaseUnit());
        PrettyTime t = new PrettyTime();
        String result = t.formatApproximateDuration(tenMinAgo);
        Assert.assertEquals("10 minutami", result);
    }

}
