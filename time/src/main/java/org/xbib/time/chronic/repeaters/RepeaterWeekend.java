package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.util.Calendar;

public class RepeaterWeekend extends RepeaterUnit {
    public static final long WEEKEND_SECONDS = 172800L; // (2 * 24 * 60 * 60);

    private Calendar currentWeekStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentWeekStart == null) {
            if (pointer == PointerType.FUTURE) {
                RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
                saturdayRepeater.setStart((Calendar) getNow().clone());
                Span nextSaturdaySpan = saturdayRepeater.nextSpan(PointerType.FUTURE);
                currentWeekStart = nextSaturdaySpan.getBeginCalendar();
            } else if (pointer == PointerType.PAST) {
                RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
                saturdayRepeater.setStart(Time.cloneAndAdd(getNow(), Calendar.SECOND, RepeaterDay.DAY_SECONDS));
                Span lastSaturdaySpan = saturdayRepeater.nextSpan(PointerType.PAST);
                currentWeekStart = lastSaturdaySpan.getBeginCalendar();
            }
        } else {
            long direction = pointer == PointerType.FUTURE ? 1L : -1L;
            currentWeekStart = Time.cloneAndAdd(currentWeekStart, Calendar.SECOND, direction * RepeaterWeek.WEEK_SECONDS);
        }
        assert currentWeekStart != null;
        Calendar c = Time.cloneAndAdd(currentWeekStart, Calendar.SECOND, RepeaterWeekend.WEEKEND_SECONDS);
        return new Span(currentWeekStart, c);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        Span thisSpan;
        if (pointer == PointerType.FUTURE || pointer == PointerType.NONE) {
            RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
            saturdayRepeater.setStart((Calendar) getNow().clone());
            Span thisSaturdaySpan = saturdayRepeater.nextSpan(PointerType.FUTURE);
            thisSpan = new Span(thisSaturdaySpan.getBeginCalendar(), Time.cloneAndAdd(thisSaturdaySpan.getBeginCalendar(), Calendar.SECOND, RepeaterWeekend.WEEKEND_SECONDS));
        } else if (pointer == PointerType.PAST) {
            RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
            saturdayRepeater.setStart((Calendar) getNow().clone());
            Span lastSaturdaySpan = saturdayRepeater.nextSpan(PointerType.PAST);
            thisSpan = new Span(lastSaturdaySpan.getBeginCalendar(), Time.cloneAndAdd(lastSaturdaySpan.getBeginCalendar(), Calendar.SECOND, RepeaterWeekend.WEEKEND_SECONDS));
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return thisSpan;
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        long direction = pointer == PointerType.FUTURE ? 1L : -1L;
        RepeaterWeekend weekend = new RepeaterWeekend();
        weekend.setStart(span.getBeginCalendar());
        Calendar start = Time.cloneAndAdd(weekend.nextSpan(pointer).getBeginCalendar(), Calendar.SECOND, (amount - 1) * direction * RepeaterWeek.WEEK_SECONDS);
        return new Span(start, Time.cloneAndAdd(start, Calendar.SECOND, span.getWidth()));
    }

    @Override
    public int getWidth() {
        // WARN: Does not use Calendar
        return (int)RepeaterWeekend.WEEKEND_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-weekend";
    }
}
