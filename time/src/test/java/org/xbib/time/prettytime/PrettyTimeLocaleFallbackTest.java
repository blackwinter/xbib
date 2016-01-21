package org.xbib.time.prettytime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbib.time.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PrettyTimeLocaleFallbackTest {
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

    // Stores current locale so that it can be restored
    private Locale locale;

    // Method setUp() is called automatically before every test method
    @Before
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(new Locale("Foo", "Bar"));
    }

    @Test
    public void testCeilingInterval() throws Exception {
        assertEquals(new Locale("Foo", "Bar"), Locale.getDefault());
        Date then = format.parse("5/20/2009");
        Date ref = format.parse("6/17/2009");
        PrettyTime t = new PrettyTime(ref.getTime());
        assertEquals("1 month ago", t.format(then));
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
