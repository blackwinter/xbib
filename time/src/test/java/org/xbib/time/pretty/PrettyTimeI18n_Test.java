package org.xbib.time.pretty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1), ZoneId.systemDefault());
        assertEquals("moments from now", p.format(localDateTime));
    }

    @Test
    public void testPrettyTimeGerman() {
        // The German resource bundle should be used
        PrettyTime p = new PrettyTime(Locale.GERMAN);
        LocalDateTime ref = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        p.setReference(ref);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1), ZoneId.systemDefault());
        assertEquals("Jetzt", p.format(localDateTime));
    }

    @Test
    public void testPrettyTimeSpanish() {
        // The Spanish resource bundle should be used
        PrettyTime p = new PrettyTime(new Locale("es"));
        assertEquals("en un instante", p.format(LocalDateTime.now()));
    }

    @Test
    public void testPrettyTimeDefaultCenturies() {
        // The default resource bundle should be used
        PrettyTime p = new PrettyTime((3155692597470L * 3L), Locale.ROOT);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        assertEquals("3 centuries ago", p.format(localDateTime));
    }

    @Test
    public void testPrettyTimeGermanCenturies() {
        // The default resource bundle should be used
        PrettyTime p = new PrettyTime((3155692597470L * 3L), Locale.GERMAN);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        assertEquals( "vor 3 Jahrhunderten", p.format(localDateTime));
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleDefault() {
        // The default resource bundle should be used
        Locale.setDefault(Locale.ROOT);
        PrettyTime p = new PrettyTime((0));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1), ZoneId.systemDefault());
        assertEquals("moments from now", p.format(localDateTime));
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleGerman() {
        // The German resource bundle should be used
        Locale.setDefault(Locale.GERMAN);
        PrettyTime p = new PrettyTime((0));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1), ZoneId.systemDefault());
        assertEquals( "Jetzt", p.format(localDateTime));
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleDefaultCenturies() {
        // The default resource bundle should be used
        Locale.setDefault(Locale.ROOT);
        PrettyTime p = new PrettyTime((3155692597470L * 3L));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        assertEquals("3 centuries ago", p.format(localDateTime));
    }

    @Test
    public void testPrettyTimeViaDefaultLocaleGermanCenturies() {
        // The default resource bundle should be used
        Locale.setDefault(Locale.GERMAN);
        PrettyTime p = new PrettyTime((3155692597470L * 3L));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        assertEquals("vor 3 Jahrhunderten", p.format(localDateTime));
    }

    @Test
    public void testPrettyTimeRootLocale() {
        long t = 1L;
        PrettyTime p = new PrettyTime(0, Locale.ROOT);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        while (1000L * 60L * 60L * 24L * 365L * 1000000L > t) {
            assertEquals(p.format(localDateTime).endsWith("now"), true);
            t *= 2L;
        }
    }

    @Test
    public void testPrettyTimeGermanLocale() {
        long t = 1L;
        PrettyTime p = new PrettyTime(0, Locale.GERMAN);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        while (1000L * 60L * 60L * 24L * 365L * 1000000L > t) {
            assertEquals(p.format(localDateTime).startsWith("in") || p.format(localDateTime).startsWith("Jetzt"), true);
            t *= 2L;
        }
    }

    // Method tearDown() is called automatically after every test method
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

}
