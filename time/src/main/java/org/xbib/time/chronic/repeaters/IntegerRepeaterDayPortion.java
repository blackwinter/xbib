package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Range;

public class IntegerRepeaterDayPortion extends RepeaterDayPortion<Integer> {
    public IntegerRepeaterDayPortion(Integer type) {
        super(type);
    }

    @Override
    protected Range createRange(Integer type) {
        return new Range(type * 60L * 60L, (type + 12) * 60L * 60L);
    }

    @Override
    protected long _getWidth(Range range) {
        return 12 * 60L * 60L;
    }
}
