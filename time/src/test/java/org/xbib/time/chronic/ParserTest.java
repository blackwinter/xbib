package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.chronic.tags.Pointer;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ParserTest extends Assert {

    private ZonedDateTime time_2006_08_16_14_00_00 = construct(2006, 8, 16, 14, 0, 0);

    private final static ZoneId ZONE_ID = ZoneId.of("GMT");

    public static ZonedDateTime construct(int year, int month) {
        return ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour) {
        return ZonedDateTime.of(year, month, day, hour, 0, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour, int minutes) {
        return ZonedDateTime.of(year, month, day, hour, minutes, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour, int minutes, int seconds) {
        return ZonedDateTime.of(year, month, day, hour, minutes, seconds, 0, ZONE_ID);
    }


    protected void assertEquals(ZonedDateTime ec, Span ac) {
        if (ec != null) {
            assertEquals(ec, ac.getBeginCalendar());
        }
    }

    @Test
    public void test_parse_guess_dates() {
        // rm_sd

        Span time;
        time = parse_now("may 27");
        assertEquals(construct(2007, 5, 27, 12), time);

        time = parse_now("may 28", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 5, 28, 12), time);

        time = parse_now("may 28 5pm", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 5, 28, 17), time);

        time = parse_now("may 28 at 5pm", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 5, 28, 17), time);

        time = parse_now("may 28 at 5:32.19pm", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 5, 28, 17, 32, 19), time);

        // rm_od

        time = parse_now("may 27th");
        assertEquals(construct(2007, 5, 27, 12), time);

        time = parse_now("may 27th", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 5, 27, 12), time);

        time = parse_now("may 27th 5:00 pm", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 5, 27, 17), time);

        time = parse_now("may 27th at 5pm", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 5, 27, 17), time);

        time = parse_now("may 27th at 5", new Options().setAmbiguousTimeRange(0));
        assertEquals(construct(2007, 5, 27, 5), time);

        // rm_sy

        time = parse_now("June 1979");
        assertEquals(construct(1979, 6, 16, 0), time);

        time = parse_now("dec 79");
        assertEquals(construct(1979, 12, 16, 12), time);

        // rm_sd_sy

        time = parse_now("jan 3 2010");
        assertEquals(construct(2010, 1, 3, 12), time);

        time = parse_now("jan 3 2010 midnight");
        assertEquals(construct(2010, 1, 4, 0), time);

        time = parse_now("jan 3 2010 at midnight");
        assertEquals(construct(2010, 1, 4, 0), time);

        time = parse_now("jan 3 2010 at 4", new Options().setAmbiguousTimeRange(0));
        assertEquals(construct(2010, 1, 3, 4), time);

        //time = parse_now("January 12, '00");
        //assertEquals(Time.construct(2000, 1, 12, 12), time);

        time = parse_now("may 27 79");
        assertEquals(construct(1979, 5, 27, 12), time);

        time = parse_now("may 27 79 4:30");
        assertEquals(construct(1979, 5, 27, 16, 30), time);

        time = parse_now("may 27 79 at 4:30", new Options().setAmbiguousTimeRange(0));
        assertEquals(construct(1979, 5, 27, 4, 30), time);

        // sd_rm_sy

        time = parse_now("3 jan 2010");
        assertEquals(construct(2010, 1, 3, 12), time);

        time = parse_now("3 jan 2010 4pm");
        assertEquals(construct(2010, 1, 3, 16), time);

        // sm_sd_sy

        time = parse_now("5/27/1979");
        assertEquals(construct(1979, 5, 27, 12), time);

        time = parse_now("5/27/1979 4am");
        assertEquals(construct(1979, 5, 27, 4), time);

        // sd_sm_sy

        time = parse_now("27/5/1979");
        assertEquals(construct(1979, 5, 27, 12), time);

        time = parse_now("27/5/1979 @ 0700");
        assertEquals(construct(1979, 5, 27, 7), time);

        // sm_sy

        time = parse_now("05/06");
        assertEquals(construct(2006, 5, 16, 12), time);

        time = parse_now("12/06");
        assertEquals(construct(2006, 12, 16, 12), time);

        time = parse_now("13/06");
        assertEquals(null, time);

        // sy_sm_sd

        time = parse_now("2000-1-1");
        assertEquals(construct(2000, 1, 1, 12), time);

        time = parse_now("2006-08-20");
        assertEquals(construct(2006, 8, 20, 12), time);

        time = parse_now("2006-08-20 7pm");
        assertEquals(construct(2006, 8, 20, 19), time);

        time = parse_now("2006-08-20 03:00");
        assertEquals(construct(2006, 8, 20, 3), time);

        time = parse_now("2006-08-20 03:30:30");
        assertEquals(construct(2006, 8, 20, 3, 30, 30), time);

        time = parse_now("2006-08-20 15:30:30");
        assertEquals(construct(2006, 8, 20, 15, 30, 30), time);

        time = parse_now("2006-08-20 15:30.30");
        assertEquals(construct(2006, 8, 20, 15, 30, 30), time);

        time = parse_now("Mon Apr 02 17:00:00 PDT 2007");
        assertEquals(construct(2007, 4, 2, 17), time);

        //time = parse_now("jan 5 13:00");
        //assertEquals(Time.construct(2007, 1, 5, 13), time);

        // due to limitations of the Time class, these don't work

        time = parse_now("may 40");
        assertEquals(null, time);

        time = parse_now("may 27 40");
        assertEquals(null, time);

        time = parse_now("1800-08-20");
        assertEquals(null, time);
    }

    @Test
    public void test_foo() throws ParseException {
        Chronic.parse("two months ago this friday");
    }

    @Test
    public void testMonth() throws ParseException {
        Span span = Chronic.parse("first day of next month");
        ZonedDateTime zdt = span.getBeginCalendar();
        span = Chronic.parse("first day of this month");
        zdt = span.getBeginCalendar();
    }

    @Test
    public void test_parse_guess_r() throws ParseException {
        Span time;
        time = parse_now("friday");
        assertEquals(construct(2006, 8, 18, 12), time);

        time = parse_now("tue");
        assertEquals(construct(2006, 8, 22, 12), time);

        time = parse_now("5");
        assertEquals(construct(2006, 8, 16, 17), time);

        Options options = new Options()
                .setAmbiguousTimeRange(0)
                .setCompatibilityMode(true)
                .setNow(construct(2006, 8, 16, 3, 0, 0));
        time = Chronic.parse("5", options);
        assertEquals(construct(2006, 8, 16, 5), time);

        time = parse_now("13:00");
        assertEquals(construct(2006, 8, 17, 13), time);

        time = parse_now("13:45");
        assertEquals(construct(2006, 8, 17, 13, 45), time);

        time = parse_now("november");
        assertEquals(construct(2006, 11, 16), time);
    }

    @Test
    public void test_parse_guess_rr() {
        Span time;
        time = parse_now("friday 13:00");
        assertEquals(construct(2006, 8, 18, 13), time);

        time = parse_now("monday 4:00");
        assertEquals(construct(2006, 8, 21, 16), time);

        time = parse_now("sat 4:00", new Options().setAmbiguousTimeRange(0));
        assertEquals(construct(2006, 8, 19, 4), time);

        time = parse_now("sunday 4:20", new Options().setAmbiguousTimeRange(0));
        assertEquals(construct(2006, 8, 20, 4, 20), time);

        time = parse_now("4 pm");
        assertEquals(construct(2006, 8, 16, 16), time);

        time = parse_now("4 am", new Options().setAmbiguousTimeRange(0));
        assertEquals(construct(2006, 8, 16, 4), time);

        time = parse_now("12 pm");
        assertEquals(construct(2006, 8, 16, 12), time);

        time = parse_now("12:01 pm");
        assertEquals(construct(2006, 8, 16, 12, 1), time);

        time = parse_now("12:01 am");
        assertEquals(construct(2006, 8, 16, 0, 1), time);

        time = parse_now("12 am");
        assertEquals(construct(2006, 8, 16), time);

        time = parse_now("4:00 in the morning");
        assertEquals(construct(2006, 8, 16, 4), time);

        time = parse_now("november 4");
        assertEquals(construct(2006, 11, 4, 12), time);

        time = parse_now("aug 24");
        assertEquals(construct(2006, 8, 24, 12), time);
    }

    @Test
    public void test_parse_guess_rrr() {
        Span time;
        time = parse_now("friday 1 pm");
        assertEquals(construct(2006, 8, 18, 13), time);

        time = parse_now("friday 11 at night");
        assertEquals(construct(2006, 8, 18, 23), time);

        time = parse_now("friday 11 in the evening");
        assertEquals(construct(2006, 8, 18, 23), time);

        time = parse_now("sunday 6am");
        assertEquals(construct(2006, 8, 20, 6), time);

        time = parse_now("friday evening at 7");
        assertEquals(construct(2006, 8, 18, 19), time);
    }

    @Test
    public void test_parse_guess_gr() throws ParseException {
        Span time;
        // year

        time = parse_now("this year");
        //assertEquals(construct(2006, 10, 24, 12, 30), time);
        assertEquals(construct(2006, 10, 24, 12), time);

        time = parse_now("this year", new Options().setContext(Pointer.PointerType.PAST));
        //assertEquals(construct(2006, 4, 24, 12, 30), time);
        assertEquals(construct(2006, 4, 24, 12), time);

        // month

        time = parse_now("this month");
        assertEquals(construct(2006, 8, 24, 12), time);

        time = parse_now("this month", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 8, 8, 12), time);

        Options options = new Options();
        options.setCompatibilityMode(true);
        options.setNow(construct(2006, 11, 15));
        time = Chronic.parse("next month", options);
        assertEquals(construct(2006, 12, 16, 12), time);

        // month name

        time = parse_now("last november");
        assertEquals(construct(2005, 11, 16), time);

        // fortnight

        time = parse_now("this fortnight");
        assertEquals(construct(2006, 8, 21, 19, 30), time);

        time = parse_now("this fortnight", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 8, 14, 19), time);

        // week

        time = parse_now("this week");
        assertEquals(construct(2006, 8, 18, 7, 30), time);

        time = parse_now("this week", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 8, 14, 19), time);

        // week

        time = parse_now("this weekend");
        assertEquals(construct(2006, 8, 20), time);

        time = parse_now("this weekend", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 8, 13), time);

        time = parse_now("last weekend");
        assertEquals(construct(2006, 8, 13), time);

        // day

        time = parse_now("this day");
        assertEquals(construct(2006, 8, 16, 19, 30), time);

        time = parse_now("this day", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 8, 16, 7), time);

        time = parse_now("today");
        assertEquals(construct(2006, 8, 16, 19, 30), time);

        time = parse_now("yesterday");
        assertEquals(construct(2006, 8, 15, 12), time);

        time = parse_now("tomorrow");
        assertEquals(construct(2006, 8, 17, 12), time);

        // day name

        time = parse_now("this tuesday");
        assertEquals(construct(2006, 8, 22, 12), time);

        time = parse_now("next tuesday");
        assertEquals(construct(2006, 8, 22, 12), time);

        time = parse_now("last tuesday");
        assertEquals(construct(2006, 8, 15, 12), time);

        time = parse_now("this wed");
        assertEquals(construct(2006, 8, 23, 12), time);

        time = parse_now("next wed");
        assertEquals(construct(2006, 8, 23, 12), time);

        time = parse_now("last wed");
        assertEquals(construct(2006, 8, 9, 12), time);

        // day portion

        time = parse_now("this morning");
        assertEquals(construct(2006, 8, 16, 9), time);

        time = parse_now("tonight");
        assertEquals(construct(2006, 8, 16, 22), time);

        // minute

        time = parse_now("next minute");
        assertEquals(construct(2006, 8, 16, 14, 1, 30), time);

        // second

        time = parse_now("this second");
        assertEquals(construct(2006, 8, 16, 14), time);

        time = parse_now("this second", new Options().setContext(Pointer.PointerType.PAST));
        assertEquals(construct(2006, 8, 16, 14), time);

        time = parse_now("next second");
        assertEquals(construct(2006, 8, 16, 14, 0, 1), time);

        time = parse_now("last second");
        assertEquals(construct(2006, 8, 16, 13, 59, 59), time);
    }

    @Test
    public void test_parse_guess_grr() {
        Span time;
        time = parse_now("yesterday at 4:00");
        assertEquals(construct(2006, 8, 15, 16), time);

        time = parse_now("today at 9:00");
        assertEquals(construct(2006, 8, 16, 9), time);

        time = parse_now("today at 2100");
        assertEquals(construct(2006, 8, 16, 21), time);

        time = parse_now("this day at 0900");
        assertEquals(construct(2006, 8, 16, 9), time);

        time = parse_now("tomorrow at 0900");
        assertEquals(construct(2006, 8, 17, 9), time);

        time = parse_now("yesterday at 4:00", new Options().setAmbiguousTimeRange(0));
        assertEquals(construct(2006, 8, 15, 4), time);

        time = parse_now("last friday at 4:00");
        assertEquals(construct(2006, 8, 11, 16), time);

        time = parse_now("next wed 4:00");
        assertEquals(construct(2006, 8, 23, 16), time);

        time = parse_now("yesterday afternoon");
        assertEquals(construct(2006, 8, 15, 15), time);

        time = parse_now("last week tuesday");
        assertEquals(construct(2006, 8, 8, 12), time);

        time = parse_now("tonight at 7");
        assertEquals(construct(2006, 8, 16, 19), time);

        time = parse_now("tonight 7");
        assertEquals(construct(2006, 8, 16, 19), time);

        time = parse_now("7 tonight");
        assertEquals(construct(2006, 8, 16, 19), time);
    }

    @Test
    public void test_parse_guess_grrr() {
        Span time;
        time = parse_now("today at 6:00pm");
        assertEquals(construct(2006, 8, 16, 18), time);

        time = parse_now("today at 6:00am");
        assertEquals(construct(2006, 8, 16, 6), time);

        time = parse_now("this day 1800");
        assertEquals(construct(2006, 8, 16, 18), time);

        time = parse_now("yesterday at 4:00pm");
        assertEquals(construct(2006, 8, 15, 16), time);

        time = parse_now("tomorrow evening at 7");
        assertEquals(construct(2006, 8, 17, 19), time);

        time = parse_now("tomorrow morning at 5:30");
        assertEquals(construct(2006, 8, 17, 5, 30), time);

        time = parse_now("next monday at 12:01 am");
        assertEquals(construct(2006, 8, 21, 0, 1), time);

        time = parse_now("next monday at 12:01 pm");
        assertEquals(construct(2006, 8, 21, 12, 1), time);
    }

    @Test
    public void test_parse_guess_rgr() {
        Span time;
        time = parse_now("afternoon yesterday");
        assertEquals(construct(2006, 8, 15, 15), time);

        time = parse_now("tuesday last week");
        assertEquals(construct(2006, 8, 8, 12), time);
    }

    @Test
    public void test_parse_guess_s_r_p() throws ParseException {
        Span time;

        time = parse_now("3 years ago");
        assertEquals(construct(2003, 8, 16, 14), time);

        time = parse_now("1 month ago");
        assertEquals(construct(2006, 7, 16, 14), time);

        time = parse_now("1 fortnight ago");
        assertEquals(construct(2006, 8, 2, 14), time);

        time = parse_now("2 fortnights ago");
        assertEquals(construct(2006, 7, 19, 14), time);

        time = parse_now("3 weeks ago");
        assertEquals(construct(2006, 7, 26, 14), time);

        time = parse_now("2 weekends ago");
        assertEquals(construct(2006, 8, 5), time);

        time = parse_now("3 days ago");
        assertEquals(construct(2006, 8, 13, 14), time);

        //time = parse_now("1 monday ago");
        //assertEquals(Time.construct(2006, 8, 14, 12), time);

        time = parse_now("5 mornings ago");
        assertEquals(construct(2006, 8, 12, 9), time);

        time = parse_now("7 hours ago");
        assertEquals(construct(2006, 8, 16, 7), time);

        time = parse_now("3 minutes ago");
        assertEquals(construct(2006, 8, 16, 13, 57), time);

        time = parse_now("20 seconds before now");
        assertEquals(construct(2006, 8, 16, 13, 59, 40), time);

        // future

        time = parse_now("3 years from now");
        assertEquals(construct(2009, 8, 16, 14, 0, 0), time);

        time = parse_now("6 months hence");
        assertEquals(construct(2007, 2, 16, 14), time);

        time = parse_now("3 fortnights hence");
        assertEquals(construct(2006, 9, 27, 14), time);

        time = parse_now("1 week from now");
        assertEquals(construct(2006, 8, 23, 14, 0, 0), time);

        time = parse_now("1 weekend from now");
        assertEquals(construct(2006, 8, 19), time);

        time = parse_now("2 weekends from now");
        assertEquals(construct(2006, 8, 26), time);

        time = parse_now("1 day hence");
        assertEquals(construct(2006, 8, 17, 14), time);

        time = parse_now("5 mornings hence");
        assertEquals(construct(2006, 8, 21, 9), time);

        time = parse_now("1 hour from now");
        assertEquals(construct(2006, 8, 16, 15), time);

        time = parse_now("20 minutes hence");
        assertEquals(construct(2006, 8, 16, 14, 20), time);

        time = parse_now("20 seconds from now");
        assertEquals(construct(2006, 8, 16, 14, 0, 20), time);

        Options options = new Options();
        options.setCompatibilityMode(true);
        options.setNow(construct(2007, 3, 7, 23, 30));
        time = Chronic.parse("2 months ago", options);
        assertEquals(construct(2007, 1, 7, 23, 30), time);
    }

    @Test
    public void test_parse_guess_p_s_r() {
        Span time;
        time = parse_now("in 3 hours");
        assertEquals(construct(2006, 8, 16, 17), time);
    }

    @Test
    public void test_parse_guess_s_r_p_a() {
        Span time;

        time = parse_now("3 years ago tomorrow");
        assertEquals(construct(2003, 8, 17, 12), time);

        time = parse_now("3 years ago this friday");
        assertEquals(construct(2003, 8, 18, 12), time);

        time = parse_now("3 months ago saturday at 5:00 pm");
        assertEquals(construct(2006, 5, 19, 17), time);

        time = parse_now("2 days from this second");
        assertEquals(construct(2006, 8, 18, 14), time);

        time = parse_now("7 hours before tomorrow at midnight");
        assertEquals(construct(2006, 8, 17, 17), time);

    }

    @Test
    public void test_parse_guess_o_r_s_r() {
        Span time = parse_now("3rd wednesday in november");
        assertEquals(construct(2006, 11, 15, 12), time);

        time = parse_now("10th wednesday in november");
        assertEquals(null, time);

        //time = parse_now("3rd wednesday in 2007");
        //assertEquals(Time.construct(2007, 1, 20, 12), time);
    }

    @Test
    public void test_parse_guess_o_r_g_r() {
        Span time;
        time = parse_now("3rd month next year");
        //assertEquals(Time.construct(2007, 3, 16, 12, 30), time);
        //assertEquals(construct(2007, 3, 16, 11, 30), time);
        assertEquals(construct(2007, 3, 16, 12), time);

        time = parse_now("3rd thursday this september");
        assertEquals(construct(2006, 9, 21, 12), time);

        time = parse_now("4th day last week");
        assertEquals(construct(2006, 8, 9, 12), time);
    }

    @Test
    public void test_parse_guess_nonsense() {
        parse_now("some stupid nonsense");
    }

    @Test
    public void test_parse_span() throws ParseException {
        Span span;
        span = parse_now("friday", new Options().setGuess(false));
        assertEquals(construct(2006, 8, 18), span.getBeginCalendar());
        assertEquals(construct(2006, 8, 19), span.getEndCalendar());

        span = parse_now("november", new Options().setGuess(false));
        assertEquals(construct(2006, 11), span.getBeginCalendar());
        assertEquals(construct(2006, 12), span.getEndCalendar());

        Options options = new Options().setNow(time_2006_08_16_14_00_00).setGuess(false);
        span = Chronic.parse("weekend", options);
        assertEquals(construct(2006, 8, 19), span.getBeginCalendar());
        assertEquals(construct(2006, 8, 21), span.getEndCalendar());
    }

    @Test
    public void test_parse_words() {
        assertEquals(parse_now("33 days from now"), parse_now("thirty-three days from now"));
        assertEquals(parse_now("2867532 seconds from now"), parse_now("two million eight hundred and sixty seven thousand five hundred and thirty two seconds from now"));
        assertEquals(parse_now("may 10th"), parse_now("may tenth"));
    }

    @Test
    public void test_parse_only_complete_pointers() {
        assertEquals(time_2006_08_16_14_00_00, parse_now("eat pasty buns today at 2pm"));
        assertEquals(time_2006_08_16_14_00_00, parse_now("futuristically speaking today at 2pm"));
        assertEquals(time_2006_08_16_14_00_00, parse_now("meeting today at 2pm"));
    }

    Span parse_now(String string) {
        return parse_now(string, new Options());
    }

    Span parse_now(String string, Options options) {
        options.setNow(time_2006_08_16_14_00_00);
        options.setCompatibilityMode(true);
        try {
            return Chronic.parse(string, options);
        } catch (ParseException e) {
            // skip
        }
        return null;
    }

}
