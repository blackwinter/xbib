package org.xbib.time.format;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeriodAmount implements TemporalAmount {

    private Map<TemporalUnit,Long> amounts = new HashMap<>();

    public void set(TemporalUnit unit, Long value) {
        amounts.put(unit, value);
    }

    @Override
    public long get(TemporalUnit unit) {
        return amounts.get(unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return new ArrayList<>(amounts.keySet());
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(this);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(this);
    }
}
