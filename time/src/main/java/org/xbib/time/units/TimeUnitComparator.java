package org.xbib.time.units;

import org.xbib.time.TimeUnit;

import java.util.Comparator;


/**
 * Compares two {@link TimeUnit} objects
 */
public class TimeUnitComparator implements Comparator<TimeUnit> {

    public int compare(final TimeUnit left, final TimeUnit right) {
        if (left.getMillisPerUnit() < right.getMillisPerUnit()) {
            return -1;
        } else if (left.getMillisPerUnit() > right.getMillisPerUnit()) {
            return 1;
        }
        return 0;
    }
}
