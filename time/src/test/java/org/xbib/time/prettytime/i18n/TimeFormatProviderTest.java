package org.xbib.time.prettytime.i18n;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.PrettyTime;
import org.xbib.time.impl.TimeFormatProvider;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class TimeFormatProviderTest {
    @Test
    public void test() {
        ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.time.prettytime.i18n.Resources", new Locale("xx"));
        Assert.assertTrue(bundle instanceof TimeFormatProvider);
    }

    @Test
    public void testFormatFromDirectFormatOverride() throws Exception {
        Locale locale = new Locale("xx");
        Locale.setDefault(locale);
        PrettyTime prettyTime = new PrettyTime(locale);
        String result = prettyTime.format(new Date(System.currentTimeMillis() + 1000 * 60 * 6));
        Assert.assertEquals("6 minutes from now", result);
    }

}
