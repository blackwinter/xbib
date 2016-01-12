package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.util.Calendar;

public class RepeaterFortnight extends RepeaterUnit {
    public static final int FORTNIGHT_SECONDS = 1209600; // (14 * 24 * 60 * 60)

    private Calendar currentFortnightStart;

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentFortnightStart == null) {
            if (pointer == PointerType.FUTURE) {
                RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
                sundayRepeater.setStart(getNow());
                Span nextSundaySpan = sundayRepeater.nextSpan(PointerType.FUTURE);
                currentFortnightStart = nextSundaySpan.getBeginCalendar();
            } else if (pointer == PointerType.PAST) {
                RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
                sundayRepeater.setStart(Time.cloneAndAdd(getNow(), Calendar.SECOND, RepeaterDay.DAY_SECONDS));
                sundayRepeater.nextSpan(PointerType.PAST);
                sundayRepeater.nextSpan(PointerType.PAST);
                Span lastSundaySpan = sundayRepeater.nextSpan(PointerType.PAST);
                currentFortnightStart = lastSundaySpan.getBeginCalendar();
            } else {
                throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }
        } else {
            int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
            currentFortnightStart.add(Calendar.SECOND, direction * RepeaterFortnight.FORTNIGHT_SECONDS);
        }

        return new Span(currentFortnightStart, Calendar.SECOND, RepeaterFortnight.FORTNIGHT_SECONDS);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        if (pointer == null) {
            pointer = PointerType.FUTURE;
        }

        Span span;
        if (pointer == PointerType.FUTURE) {
            Calendar thisFortnightStart = Time.cloneAndAdd(Time.ymdh(getNow()), Calendar.SECOND, RepeaterHour.HOUR_SECONDS);
            RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
            sundayRepeater.setStart(getNow());
            sundayRepeater.thisSpan(PointerType.FUTURE);
            Span thisSundaySpan = sundayRepeater.thisSpan(PointerType.FUTURE);
            Calendar thisFortnightEnd = thisSundaySpan.getBeginCalendar();
            span = new Span(thisFortnightStart, thisFortnightEnd);
        } else if (pointer == PointerType.PAST) {
            Calendar thisFortnightEnd = Time.ymdh(getNow());
            RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
            sundayRepeater.setStart(getNow());
            Span lastSundaySpan = sundayRepeater.nextSpan(PointerType.PAST);
            Calendar thisFortnightStart = lastSundaySpan.getBeginCalendar();
            span = new Span(thisFortnightStart, thisFortnightEnd);
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }

        return span;
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
        return span.add(direction * amount * RepeaterFortnight.FORTNIGHT_SECONDS);
    }

    @Override
    public int getWidth() {
        return RepeaterFortnight.FORTNIGHT_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-fortnight";
    }

}
