package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Range;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class RepeaterDayPortion<T> extends Repeater<T> {
    private static final Pattern AM_PATTERN = Pattern.compile("^ams?$");
    private static final Pattern PM_PATTERN = Pattern.compile("^pms?$");
    private static final Pattern MORNING_PATTERN = Pattern.compile("^mornings?$");
    private static final Pattern AFTERNOON_PATTERN = Pattern.compile("^afternoons?$");
    private static final Pattern EVENING_PATTERN = Pattern.compile("^evenings?$");
    private static final Pattern NIGHT_PATTERN = Pattern.compile("^(night|nite)s?$");

    private static final int FULL_DAY_SECONDS = 60 * 60 * 24;
    private Range range;
    private Span currentSpan;

    public RepeaterDayPortion(T type) {
        super(type);
        range = createRange(type);
    }

    public static EnumRepeaterDayPortion scan(Token token) {
        Map<Pattern, DayPortion> scanner = new HashMap<>();
        scanner.put(RepeaterDayPortion.AM_PATTERN, DayPortion.AM);
        scanner.put(RepeaterDayPortion.PM_PATTERN, DayPortion.PM);
        scanner.put(RepeaterDayPortion.MORNING_PATTERN, DayPortion.MORNING);
        scanner.put(RepeaterDayPortion.AFTERNOON_PATTERN, DayPortion.AFTERNOON);
        scanner.put(RepeaterDayPortion.EVENING_PATTERN, DayPortion.EVENING);
        scanner.put(RepeaterDayPortion.NIGHT_PATTERN, DayPortion.NIGHT);
        for (Map.Entry<Pattern, DayPortion> entry : scanner.entrySet()) {
            Pattern scannerItem = entry.getKey();
            if (scannerItem.matcher(token.getWord()).matches()) {
                return new EnumRepeaterDayPortion(scanner.get(scannerItem));
            }
        }
        return null;
    }

    @Override
    protected Span _nextSpan(PointerType pointer) {
        Calendar rangeStart;
        Calendar rangeEnd;
        if (currentSpan == null) {
            long nowSeconds = (getNow().getTimeInMillis() - Time.ymd(getNow()).getTimeInMillis()) / 1000;
            if (nowSeconds < range.getBegin()) {
                if (pointer == PointerType.FUTURE) {
                    rangeStart = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.SECOND, range.getBegin());
                } else if (pointer == PointerType.PAST) {
                    rangeStart = Time.cloneAndAdd(Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, -1), Calendar.SECOND, range.getBegin());
                } else {
                    throw new IllegalArgumentException("Unable to handle pointer type " + pointer);
                }
            } else if (nowSeconds > range.getBegin()) {
                if (pointer == PointerType.FUTURE) {
                    rangeStart = Time.cloneAndAdd(Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, 1), Calendar.SECOND, range.getBegin());
                } else if (pointer == PointerType.PAST) {
                    rangeStart = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.SECOND, range.getBegin());
                } else {
                    throw new IllegalArgumentException("Unable to handle pointer type " + pointer);
                }
            } else {
                if (pointer == PointerType.FUTURE) {
                    rangeStart = Time.cloneAndAdd(Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, 1), Calendar.SECOND, range.getBegin());
                } else if (pointer == PointerType.PAST) {
                    rangeStart = Time.cloneAndAdd(Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, -1), Calendar.SECOND, range.getBegin());
                } else {
                    throw new IllegalArgumentException("Unable to handle pointer type " + pointer);
                }
            }

            currentSpan = new Span(rangeStart, Time.cloneAndAdd(rangeStart, Calendar.SECOND, range.getWidth()));
        } else {
            if (pointer == PointerType.FUTURE) {
                // WARN: Does not use Calendar
                currentSpan = currentSpan.add(RepeaterDayPortion.FULL_DAY_SECONDS);
            } else if (pointer == PointerType.PAST) {
                // WARN: Does not use Calendar
                currentSpan = currentSpan.subtract(RepeaterDayPortion.FULL_DAY_SECONDS);
            } else {
                throw new IllegalArgumentException("Unable to handle pointer type " + pointer);
            }
        }
        return currentSpan;
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        Calendar rangeStart = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.SECOND, range.getBegin());
        currentSpan = new Span(rangeStart, Time.cloneAndAdd(rangeStart, Calendar.SECOND, range.getWidth()));
        return currentSpan;
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        setStart(span.getBeginCalendar());
        Span portionSpan = nextSpan(pointer);
        int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
        portionSpan = portionSpan.add(direction * (amount - 1) * RepeaterDay.DAY_SECONDS);
        return portionSpan;
    }

    @Override
    public int getWidth() {
        if (range == null) {
            throw new IllegalStateException("Range has not been set");
        }
        Long width;
        if (currentSpan != null) {
            width = currentSpan.getWidth();
        } else {
            width = _getWidth(range);
        }
        return width.intValue();
    }

    protected abstract long _getWidth(Range range);

    protected abstract Range createRange(T type);

    @Override
    public String toString() {
        return super.toString() + "-dayportion-" + getType();
    }

    public enum DayPortion {
        AM, PM, MORNING, AFTERNOON, EVENING, NIGHT
    }

}
