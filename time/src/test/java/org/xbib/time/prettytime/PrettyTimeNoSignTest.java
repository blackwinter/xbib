package org.xbib.time.prettytime;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.PrettyTime;
import org.xbib.time.TimeFormat;
import org.xbib.time.TimeUnit;
import org.xbib.time.format.SimpleTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrettyTimeNoSignTest {
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

    @Test
    public void testNoSuffixes() throws Exception {
        Date then = format.parse("8/20/2009");
        Date ref = format.parse("5/17/2009");
        PrettyTime p = new PrettyTime(ref.getTime(), Locale.ENGLISH);

        List<TimeUnit> units = p.getUnits();
        for (TimeUnit unit : units) {
            TimeFormat fmt = p.getFormat(unit);
            if (fmt instanceof SimpleTimeFormat) {
                ((SimpleTimeFormat) fmt).setFuturePrefix("").setFutureSuffix("").setPastPrefix("").setPastSuffix("");
            }
        }

        Assert.assertEquals("3 months", p.format(then));
    }
}
