package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeaterWeekend extends RepeaterUnit {
    public static final long WEEKEND_SECONDS = 172800L; // (2 * 24 * 60 * 60);

    private ZonedDateTime currentWeekStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentWeekStart == null) {
            if (pointer == PointerType.FUTURE) {
                RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
                saturdayRepeater.setNow(getNow());
                Span nextSaturdaySpan = saturdayRepeater.nextSpan(PointerType.FUTURE);
                currentWeekStart = nextSaturdaySpan.getBeginCalendar();
            } else if (pointer == PointerType.PAST) {
                RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
                saturdayRepeater.setNow(getNow().plus(RepeaterDay.DAY_SECONDS, ChronoUnit.SECONDS));
                Span lastSaturdaySpan = saturdayRepeater.nextSpan(PointerType.PAST);
                currentWeekStart = lastSaturdaySpan.getBeginCalendar();
            }
        } else {
            long direction = pointer == PointerType.FUTURE ? 1L : -1L;
            currentWeekStart = currentWeekStart.plus(direction * RepeaterWeek.WEEK_SECONDS, ChronoUnit.SECONDS);
        }
        assert currentWeekStart != null;
        ZonedDateTime c = currentWeekStart.plus(RepeaterWeekend.WEEKEND_SECONDS, ChronoUnit.SECONDS);
        return new Span(currentWeekStart, c);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        Span thisSpan;
        if (pointer == PointerType.FUTURE || pointer == PointerType.NONE) {
            RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
            saturdayRepeater.setNow(getNow());
            Span thisSaturdaySpan = saturdayRepeater.nextSpan(PointerType.FUTURE);
            thisSpan = new Span(thisSaturdaySpan.getBeginCalendar(), thisSaturdaySpan.getBeginCalendar().plus(RepeaterWeekend.WEEKEND_SECONDS, ChronoUnit.SECONDS));
        } else if (pointer == PointerType.PAST) {
            RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
            saturdayRepeater.setNow(getNow());
            Span lastSaturdaySpan = saturdayRepeater.nextSpan(PointerType.PAST);
            thisSpan = new Span(lastSaturdaySpan.getBeginCalendar(), lastSaturdaySpan.getBeginCalendar().plus(RepeaterWeekend.WEEKEND_SECONDS, ChronoUnit.SECONDS));
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return thisSpan;
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        long direction = pointer == PointerType.FUTURE ? 1L : -1L;
        RepeaterWeekend weekend = new RepeaterWeekend();
        weekend.setNow(span.getBeginCalendar());
        ZonedDateTime start = weekend.nextSpan(pointer).getBeginCalendar().plus((amount - 1) * direction * RepeaterWeek.WEEK_SECONDS, ChronoUnit.SECONDS);
        return new Span(start, start.plus(span.getWidth(), ChronoUnit.SECONDS));
    }

    @Override
    public int getWidth() {
        return (int)RepeaterWeekend.WEEKEND_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-weekend";
    }
}
