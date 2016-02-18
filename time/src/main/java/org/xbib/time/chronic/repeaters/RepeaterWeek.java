package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeaterWeek extends RepeaterUnit {
    public static final int WEEK_SECONDS = 604800; // (7 * 24 * 60 * 60);
    public static final int WEEK_DAYS = 7;

    private ZonedDateTime currentWeekStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentWeekStart == null) {
            if (pointer == PointerType.FUTURE) {
                RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
                sundayRepeater.setNow(getNow());
                Span nextSundaySpan = sundayRepeater.nextSpan(PointerType.FUTURE);
                currentWeekStart = nextSundaySpan.getBeginCalendar();
            } else if (pointer == PointerType.PAST) {
                RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
                sundayRepeater.setNow(getNow().plus(1, ChronoUnit.DAYS));
                sundayRepeater.nextSpan(PointerType.PAST);
                Span lastSundaySpan = sundayRepeater.nextSpan(PointerType.PAST);
                currentWeekStart = lastSundaySpan.getBeginCalendar();
            } else {
                throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }
        } else {
            int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
            currentWeekStart = currentWeekStart.plus(RepeaterWeek.WEEK_DAYS * direction, ChronoUnit.DAYS);
        }

        return new Span(currentWeekStart, ChronoUnit.DAYS, RepeaterWeek.WEEK_DAYS);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        Span thisWeekSpan;
        ZonedDateTime thisWeekStart;
        ZonedDateTime thisWeekEnd;
        if (pointer == PointerType.FUTURE) {
            thisWeekStart = ymdh(getNow()).plus(1, ChronoUnit.HOURS);
            RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
            sundayRepeater.setNow(getNow());
            Span thisSundaySpan = sundayRepeater.thisSpan(PointerType.FUTURE);
            thisWeekEnd = thisSundaySpan.getBeginCalendar();
            thisWeekSpan = new Span(thisWeekStart, thisWeekEnd);
        } else if (pointer == PointerType.PAST) {
            thisWeekEnd = ymdh(getNow());
            RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
            sundayRepeater.setNow(getNow());
            Span lastSundaySpan = sundayRepeater.nextSpan(PointerType.PAST);
            thisWeekStart = lastSundaySpan.getBeginCalendar();
            thisWeekSpan = new Span(thisWeekStart, thisWeekEnd);
        } else if (pointer == PointerType.NONE) {
            RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
            sundayRepeater.setNow(getNow());
            Span lastSundaySpan = sundayRepeater.nextSpan(PointerType.PAST);
            thisWeekStart = lastSundaySpan.getBeginCalendar();
            thisWeekEnd = thisWeekStart.plus(RepeaterWeek.WEEK_DAYS, ChronoUnit.DAYS);
            thisWeekSpan = new Span(thisWeekStart, thisWeekEnd);
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return thisWeekSpan;
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        long direction = pointer == PointerType.FUTURE ? 1L : -1L;
        return span.add(direction * amount * RepeaterWeek.WEEK_SECONDS);
    }

    @Override
    public int getWidth() {
        return RepeaterWeek.WEEK_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-week";
    }

    private static ZonedDateTime ymdh(ZonedDateTime zonedDateTime) {
        return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth(),
                zonedDateTime.getHour(), 0, 0, 0, zonedDateTime.getZone());
    }

}
