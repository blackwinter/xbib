package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PrettyTimeI18n_BG_Test {

    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

    // Stores current locale so that it can be restored
    private Locale locale;

    // Method setUp() is called automatically before every test method
    @Before
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(new Locale("bg"));
    }

    @Test
    public void testCenturiesFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("след 3 века", t.format(new Date(3155692597470L * 3L)));
    }

    @Test
    public void testCenturiesAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(3155692597470L * 3L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 3 века", t.format(new Date(0)));
    }

    @Test
    public void testCenturySingular() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(3155692597470L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 1 век", t.format(new Date(0)));
    }

    @Test
    public void testDaysFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("след 3 дни", t.format(new Date(1000 * 60 * 60 * 24 * 3)));
    }

    @Test
    public void testDaysAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 3), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 3 дни", t.format(new Date(0)));
    }

    @Test
    public void testDaySingular() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 1 ден", t.format(new Date(0)));
    }

    @Test
    public void testDecadesAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(315569259747L * 3L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 3 десетилетия", t.format(new Date(0)));
    }

    @Test
    public void testDecadesFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("след 3 десетилетия", t.format(new Date(315569259747L * 3L)));
    }

    @Test
    public void testDecadeSingular() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("след 1 десетилетие", t.format(new Date(315569259747L)));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("след 3 часа", t.format(new Date(1000 * 60 * 60 * 3)));
    }

    @Test
    public void testHoursAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 3), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 3 часа", t.format(new Date(0)));
    }

    @Test
    public void testHourSingular() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 1 час", t.format(new Date(0)));
    }

    @Test
    public void testRightNow() throws Exception {
        PrettyTime t = new PrettyTime();
        assertEquals("в момента", t.format(new Date()));
    }

    @Test
    public void testMomentsAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(6000), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("току що", t.format(new Date(0)));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("след 12 минути", t.format(new Date(1000 * 60 * 12)));
    }

    @Test
    public void testMinutesAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 12), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 12 минути", t.format(new Date(0)));
    }

    @Test
    public void testMonthsFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("след 3 месеца", t.format(new Date(2629743830L * 3L)));
    }

    @Test
    public void testMonthsAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(2629743830L * 3L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 3 месеца", t.format(new Date(0)));
    }

    @Test
    public void testMonthSingular() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(2629743830L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 1 месец", t.format(new Date(0)));
    }

    @Test
    public void testWeeksFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("след 3 седмици", t.format(new Date(1000 * 60 * 60 * 24 * 7 * 3)));
    }

    @Test
    public void testWeeksAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 7 * 3), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 3 седмици", t.format(new Date(0)));
    }

    @Test
    public void testWeekSingular() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 7), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 1 седмица", t.format(new Date(0)));
    }

    @Test
    public void testYearsFromNow() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("след 3 години", t.format(new Date(2629743830L * 12L * 3L)));
    }

    @Test
    public void testYearsAgo() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(2629743830L * 12L * 3L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 3 години", t.format(new Date(0)));
    }

    @Test
    public void testYearSingular() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(2629743830L * 12L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        assertEquals("преди 1 година", t.format(new Date(0)));
    }

    @Test
    public void testFormattingDurationListInThePast() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15 + 1000 * 60 * 38), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        List<Duration> durations = t.calculatePreciseDuration(new Date(0));
        assertEquals("преди 3 дни 15 часа 38 минути", t.format(durations));
    }

    @Test
    public void testFormattingDurationListInTheFuture() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
        PrettyTime t = new PrettyTime(localDateTime);
        List<Duration> durations = t.calculatePreciseDuration(new Date(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15
                + 1000 * 60 * 38));
        assertEquals("след 3 дни 15 часа 38 минути", t.format(durations));
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
