package org.xbib.time.prettytime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbib.time.PrettyTime;

import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * All the tests for PrettyTime.
 */
public class PrettyTimeI18n_FR_Test {

    /*
     * A note when you want to use the YourKit profiler: To use the YourKit
     * profiler (http://yourkit.com), run with VM argument for profiling:
     * -agentlib:yjpagent=onexit=snapshot,tracing
     */

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
        assertEquals("à l'instant", p.format(new Date()));
    }

    @Test
    public void testPrettyTimeFRENCHCenturies() {
        PrettyTime p = new PrettyTime((3155692597470L * 3L), Locale.FRENCH);
        assertEquals(p.format(new Date(0)), "il y a 3 siècles");
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleFRENCH() {
        // The FRENCH resource bundle should be used
        Locale.setDefault(Locale.FRENCH);
        PrettyTime p = new PrettyTime();
        assertEquals(p.format(new Date()), "à l'instant");
    }

    @Test
    public void testPrettyTimeFRENCHLocale() {
        long t = 1L;
        PrettyTime p = new PrettyTime((0), Locale.FRENCH);
        while (1000L * 60L * 60L * 24L * 365L * 1000000L > t) {
            assertTrue(p.format(new Date(0)).startsWith("dans") || p.format(new Date(0)).startsWith("à l'instant"));
            t *= 2L;
        }
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
