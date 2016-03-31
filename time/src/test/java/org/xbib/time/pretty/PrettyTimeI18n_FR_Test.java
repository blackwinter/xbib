package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * All the tests for PrettyTime.
 */
public class PrettyTimeI18n_FR_Test {

    // Stores current locale so that it can be restored
    private Locale locale;

    // Method setUp() is called automatically before every test method
    @Before
    public void setUp() throws Exception {
        locale = Locale.getDefault();
    }

    @Test
    public void testPrettyTimeFRENCH() {
        // The FRENCH resource bundle should be used
        PrettyTime p = new PrettyTime(Locale.FRENCH);
        assertEquals("à l'instant", p.format(LocalDateTime.now()));
    }

    @Test
    public void testPrettyTimeFRENCHCenturies() {
        PrettyTime p = new PrettyTime((3155692597470L * 3L), Locale.FRENCH);
        assertEquals(p.format(0), "il y a 3 siècles");
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleFRENCH() {
        // The FRENCH resource bundle should be used
        Locale.setDefault(Locale.FRENCH);
        PrettyTime p = new PrettyTime();
        assertEquals(p.format(LocalDateTime.now()), "à l'instant");
    }

    @Test
    public void testPrettyTimeFRENCHLocale() {
        long t = 1L;
        PrettyTime p = new PrettyTime((0), Locale.FRENCH);
        while (1000L * 60L * 60L * 24L * 365L * 1000000L > t) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
            assertTrue(p.format(localDateTime).startsWith("dans") || p.format(localDateTime).startsWith("à l'instant"));
            t *= 2L;
        }
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
