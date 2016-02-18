package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Range;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
        ZonedDateTime rangeStart;
        ZonedDateTime rangeEnd;
        if (currentSpan == null) {
            long nowSeconds = getNow().toInstant().getEpochSecond() - ymd(getNow()).toInstant().getEpochSecond();
            if (nowSeconds < range.getBegin()) {
                if (pointer == PointerType.FUTURE) {
                    rangeStart = ymd(getNow()).plus(range.getBegin(), ChronoUnit.SECONDS);
                } else if (pointer == PointerType.PAST) {
                    rangeStart = ymd(getNow()).minus(1, ChronoUnit.DAYS).plus(range.getBegin(), ChronoUnit.SECONDS);
                } else {
                    throw new IllegalArgumentException("unable to handle pointer type " + pointer);
                }
            } else if (nowSeconds > range.getBegin()) {
                if (pointer == PointerType.FUTURE) {
                    rangeStart = ymd(getNow()).plus(1, ChronoUnit.DAYS).plus(range.getBegin(), ChronoUnit.SECONDS);
                } else if (pointer == PointerType.PAST) {
                    rangeStart = ymd(getNow()).plus(range.getBegin(), ChronoUnit.SECONDS);
                } else {
                    throw new IllegalArgumentException("unable to handle pointer type " + pointer);
                }
            } else {
                if (pointer == PointerType.FUTURE) {
                    rangeStart = ymd(getNow()).plus(1, ChronoUnit.DAYS).plus(range.getBegin(), ChronoUnit.SECONDS);
                } else if (pointer == PointerType.PAST) {
                    rangeStart = ymd(getNow()).minus(1, ChronoUnit.DAYS).plus(range.getBegin(), ChronoUnit.SECONDS);
                } else {
                    throw new IllegalArgumentException("unable to handle pointer type " + pointer);
                }
            }
            currentSpan = new Span(rangeStart, rangeStart.plus(range.getWidth(), ChronoUnit.SECONDS));
        } else {
            if (pointer == PointerType.FUTURE) {
                currentSpan = currentSpan.add(RepeaterDayPortion.FULL_DAY_SECONDS);
            } else if (pointer == PointerType.PAST) {
                currentSpan = currentSpan.add(-RepeaterDayPortion.FULL_DAY_SECONDS);
            } else {
                throw new IllegalArgumentException("Unable to handle pointer type " + pointer);
            }
        }
        return currentSpan;
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        ZonedDateTime rangeStart = ymd(getNow()).plus(range.getBegin(), ChronoUnit.SECONDS);
        currentSpan = new Span(rangeStart, rangeStart.plus(range.getWidth(), ChronoUnit.SECONDS));
        return currentSpan;
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        setNow(span.getBeginCalendar());
        Span portionSpan = nextSpan(pointer);
        long direction = pointer == PointerType.FUTURE ? 1L : -1L;
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

    private static ZonedDateTime ymd(ZonedDateTime zonedDateTime) {
        return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth(),
                0, 0, 0, 0, zonedDateTime.getZone());
    }
}
