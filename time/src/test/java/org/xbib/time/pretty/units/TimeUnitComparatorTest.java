package org.xbib.time.pretty.units;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeUnitComparatorTest {

    @Test
    public void testComparingOrder() throws Exception {
        TimeUnitComparator comparator = new TimeUnitComparator();
        assertEquals(-1, comparator.compare(new Hour(), new Day()));
    }

}
