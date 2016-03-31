package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PrettyTimeI18n_zh_TW_Test {

    private Locale locale;

    @Before
    public void setUp() throws Exception {
        locale = Locale.TRADITIONAL_CHINESE;
        Locale.setDefault(Locale.TRADITIONAL_CHINESE);
    }

    @Test
    public void testPrettyTime() {
        PrettyTime p = new PrettyTime(locale);
        assertEquals("剛剛", p.format(LocalDateTime.now()));
    }

    @Test
    public void testPrettyTimeCenturies() {
        PrettyTime p = new PrettyTime((3155692597470L * 3L), locale);
        assertEquals("3 世紀 前", p.format((0)));

        p = new PrettyTime((0), locale);
        assertEquals("3 世紀 後", p.format((3155692597470L * 3L)));
    }

    @Test
    public void testCeilingInterval() throws Exception {
        LocalDateTime then = LocalDateTime.of(2009, 5, 20, 0, 0);
        LocalDateTime ref = LocalDateTime.of(2009, 6, 17, 0, 0);
        PrettyTime t = new PrettyTime(ref, locale);
        assertEquals("1 個月 前", t.format(then));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDate() throws Exception {
        PrettyTime t = new PrettyTime(locale);
        assertEquals("зараз", t.format((LocalDateTime)null));
    }

    @Test
    public void testRightNow() throws Exception {
        PrettyTime t = new PrettyTime(locale);
        assertEquals("剛剛", t.format(LocalDateTime.now()));
    }

    @Test
    public void testRightNowVariance() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("剛剛", t.format((600)));
    }

    @Test
    public void testMinutesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("12 分鐘 後", t.format((1000 * 60 * 12)));
    }

    @Test
    public void testHoursFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("3 小時 後", t.format((1000 * 60 * 60 * 3)));
    }

    @Test
    public void testDaysFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("3 天 後", t.format((1000 * 60 * 60 * 24 * 3)));
    }

    @Test
    public void testWeeksFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("3 週 後", t.format((1000 * 60 * 60 * 24 * 7 * 3)));
    }

    @Test
    public void testMonthsFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("3 個月 後", t.format((2629743830L * 3L)));
    }

    @Test
    public void testYearsFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("3 年 後", t.format((2629743830L * 12L * 3L)));
    }

    @Test
    public void testDecadesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("30 年 後", t.format((315569259747L * 3L)));
    }

    @Test
    public void testCenturiesFromNow() throws Exception {
        PrettyTime t = new PrettyTime((0), locale);
        assertEquals("3 世紀 後", t.format((3155692597470L * 3L)));
    }

    /*
     * Past
     */
    @Test
    public void testMomentsAgo() throws Exception {
        PrettyTime t = new PrettyTime((6000), locale);
        assertEquals("片刻之前", t.format((0)));
    }

    @Test
    public void testMinutesAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 12), locale);
        assertEquals("12 分鐘 前", t.format((0)));
    }

    @Test
    public void test1HourAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60), locale);
        assertEquals("1 小時 前", t.format((0)));
    }

    @Test
    public void test3HoursAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 3), locale);
        assertEquals("3 小時 前", t.format((0)));
    }

    @Test
    public void test6HoursAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 6), locale);
        assertEquals("6 小時 前", t.format((0)));
    }

    @Test
    public void testDaysAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 3), locale);
        assertEquals("3 天 前", t.format((0)));
    }

    @Test
    public void testWeeksAgo() throws Exception {
        PrettyTime t = new PrettyTime((1000 * 60 * 60 * 24 * 7 * 3), locale);
        assertEquals("3 週 前", t.format((0)));
    }

    @Test
    public void testMonthsAgo() throws Exception {
        PrettyTime t = new PrettyTime((2629743830L * 3L), locale);
        assertEquals("3 個月 前", t.format((0)));
    }

    @Test
    public void testYearsAgo() throws Exception {
        PrettyTime t = new PrettyTime((2629743830L * 12L * 3L), locale);
        assertEquals("3 年 前", t.format((0)));
    }

    @Test
    public void test8YearsAgo() throws Exception {
        PrettyTime t = new PrettyTime((2629743830L * 12L * 8L), locale);
        assertEquals("8 年 前", t.format((0)));
    }

    @Test
    public void testDecadesAgo() throws Exception {
        PrettyTime t = new PrettyTime((315569259747L * 3L), locale);
        assertEquals("30 年 前", t.format((0)));
    }

    @Test
    public void test8DecadesAgo() throws Exception {
        PrettyTime t = new PrettyTime((315569259747L * 8L), locale);
        assertEquals("80 年 前", t.format((0)));
    }

    @Test
    public void testCenturiesAgo() throws Exception {
        PrettyTime t = new PrettyTime((3155692597470L * 3L), locale);
        assertEquals("3 世紀 前", t.format((0)));
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }
}
