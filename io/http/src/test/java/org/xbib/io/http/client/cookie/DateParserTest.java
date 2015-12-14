package org.xbib.io.http.client.cookie;

import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DateParserTest {

    @Test(groups = "standalone")
    public void testRFC822() throws ParseException {
        Date date = DateParser.parse("Sun, 06 Nov 1994 08:49:37 GMT");
        assertNotNull(date);

        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(cal.get(Calendar.DAY_OF_WEEK), Calendar.SUNDAY);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 6);
        assertEquals(cal.get(Calendar.MONTH), Calendar.NOVEMBER);
        assertEquals(cal.get(Calendar.YEAR), 1994);
        assertEquals(cal.get(Calendar.HOUR), 8);
        assertEquals(cal.get(Calendar.MINUTE), 49);
        assertEquals(cal.get(Calendar.SECOND), 37);
    }

    @Test(groups = "standalone")
    public void testRFC822SingleDigitDayOfMonth() throws ParseException {
        Date date = DateParser.parse("Sun, 6 Nov 1994 08:49:37 GMT");
        assertNotNull(date);

        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(cal.get(Calendar.DAY_OF_WEEK), Calendar.SUNDAY);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 6);
        assertEquals(cal.get(Calendar.MONTH), Calendar.NOVEMBER);
        assertEquals(cal.get(Calendar.YEAR), 1994);
        assertEquals(cal.get(Calendar.HOUR), 8);
        assertEquals(cal.get(Calendar.MINUTE), 49);
        assertEquals(cal.get(Calendar.SECOND), 37);
    }

    @Test(groups = "standalone")
    public void testRFC822SingleDigitHour() throws ParseException {
        Date date = DateParser.parse("Sun, 6 Nov 1994 8:49:37 GMT");
        assertNotNull(date);

        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(cal.get(Calendar.DAY_OF_WEEK), Calendar.SUNDAY);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 6);
        assertEquals(cal.get(Calendar.MONTH), Calendar.NOVEMBER);
        assertEquals(cal.get(Calendar.YEAR), 1994);
        assertEquals(cal.get(Calendar.HOUR), 8);
        assertEquals(cal.get(Calendar.MINUTE), 49);
        assertEquals(cal.get(Calendar.SECOND), 37);
    }

    @Test(groups = "standalone")
    public void testRFC850() throws ParseException {
        Date date = DateParser.parse("Saturday, 06-Nov-94 08:49:37 GMT");
        assertNotNull(date);

        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(cal.get(Calendar.DAY_OF_WEEK), Calendar.SATURDAY);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 6);
        assertEquals(cal.get(Calendar.MONTH), Calendar.NOVEMBER);
        assertEquals(cal.get(Calendar.YEAR), 2094);
        assertEquals(cal.get(Calendar.HOUR), 8);
        assertEquals(cal.get(Calendar.MINUTE), 49);
        assertEquals(cal.get(Calendar.SECOND), 37);
    }

    @Test(groups = "standalone")
    public void testANSIC() throws ParseException {
        Date date = DateParser.parse("Sun Nov 6 08:49:37 1994");
        assertNotNull(date);

        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(cal.get(Calendar.DAY_OF_WEEK), Calendar.SUNDAY);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 6);
        assertEquals(cal.get(Calendar.MONTH), Calendar.NOVEMBER);
        assertEquals(cal.get(Calendar.YEAR), 1994);
        assertEquals(cal.get(Calendar.HOUR), 8);
        assertEquals(cal.get(Calendar.MINUTE), 49);
        assertEquals(cal.get(Calendar.SECOND), 37);
    }
}
