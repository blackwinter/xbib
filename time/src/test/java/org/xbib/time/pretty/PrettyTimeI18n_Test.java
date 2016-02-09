package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PrettyTimeI18n_Test {

    // Stores current locale so that it can be restored
    private Locale locale;

    // Method setUp() is called automatically before every test method
    @Before
    public void setUp() throws Exception {
        locale = Locale.getDefault();
    }

    @Test
    public void testPrettyTimeDefault() {
        // The default resource bundle should be used
        PrettyTime p = new PrettyTime(0, Locale.ROOT);
        assertEquals("moments from now", p.format(new Date(1)));
    }

    @Test
    public void testPrettyTimeGerman() {
        // The German resource bundle should be used
        PrettyTime p = new PrettyTime(Locale.GERMAN);
        p.setReference(0);
        assertEquals("Jetzt", p.format(new Date(1)));
    }

    @Test
    public void testPrettyTimeSpanish() {
        // The Spanish resource bundle should be used
        PrettyTime p = new PrettyTime(new Locale("es"));
        assertEquals("en un instante", p.format(new Date()));
    }

    @Test
    public void testPrettyTimeDefaultCenturies() {
        // The default resource bundle should be used
        PrettyTime p = new PrettyTime((3155692597470L * 3L), Locale.ROOT);
        assertEquals("3 centuries ago", p.format(new Date(0)));
    }

    @Test
    public void testPrettyTimeGermanCenturies() {
        // The default resource bundle should be used
        PrettyTime p = new PrettyTime((3155692597470L * 3L), Locale.GERMAN);
        assertEquals(p.format(new Date(0)), "vor 3 Jahrhunderten");
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleDefault() {
        // The default resource bundle should be used
        Locale.setDefault(Locale.ROOT);
        PrettyTime p = new PrettyTime((0));
        assertEquals(p.format(new Date(1)), "moments from now");
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleGerman() {
        // The German resource bundle should be used
        Locale.setDefault(Locale.GERMAN);
        PrettyTime p = new PrettyTime((0));
        assertEquals(p.format(new Date(1)), "Jetzt");
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleDefaultCenturies() {
        // The default resource bundle should be used
        Locale.setDefault(Locale.ROOT);
        PrettyTime p = new PrettyTime((3155692597470L * 3L));
        assertEquals(p.format(new Date(0)), "3 centuries ago");
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleGermanCenturies() {
        // The default resource bundle should be used
        Locale.setDefault(Locale.GERMAN);
        PrettyTime p = new PrettyTime((3155692597470L * 3L));
        assertEquals(p.format(new Date(0)), "vor 3 Jahrhunderten");
    }

    @Test
    public void testPrettyTimeRootLocale() {
        long t = 1L;
        PrettyTime p = new PrettyTime(0, Locale.ROOT);
        while (1000L * 60L * 60L * 24L * 365L * 1000000L > t) {
            assertEquals(p.format(new Date(0)).endsWith("now"), true);
            t *= 2L;
        }
    }

    @Test
    public void testPrettyTimeGermanLocale() {
        long t = 1L;
        PrettyTime p = new PrettyTime(0, Locale.GERMAN);
        while (1000L * 60L * 60L * 24L * 365L * 1000000L > t) {
            assertEquals(p.format(new Date(0)).startsWith("in") || p.format(new Date(0)).startsWith("Jetzt"), true);
            t *= 2L;
        }
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
