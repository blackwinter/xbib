package org.xbib.time.prettytime.units;

import org.junit.Test;
import org.xbib.time.units.Day;
import org.xbib.time.units.Hour;
import org.xbib.time.units.TimeUnitComparator;

import static org.junit.Assert.assertEquals;

public class TimeUnitComparatorTest {

    @Test
    public void testComparingOrder() throws Exception {
        TimeUnitComparator comparator = new TimeUnitComparator();
        assertEquals(-1, comparator.compare(new Hour(), new Day()));
    }

}
