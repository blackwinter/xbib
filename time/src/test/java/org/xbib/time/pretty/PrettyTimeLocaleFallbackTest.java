package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PrettyTimeLocaleFallbackTest {

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
        LocalDateTime then = LocalDateTime.of(2009, 5, 20, 0, 0);
        LocalDateTime ref = LocalDateTime.of(2009, 6, 17, 0, 0);
        PrettyTime t = new PrettyTime(ref);
        assertEquals("1 month ago", t.format(then));
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
