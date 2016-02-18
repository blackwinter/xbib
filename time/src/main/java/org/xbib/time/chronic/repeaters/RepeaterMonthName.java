package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RepeaterMonthName extends Repeater<RepeaterMonthName.MonthName> {
    private static final Pattern JAN_PATTERN = Pattern.compile("^jan\\.?(uary)?$");
    private static final Pattern FEB_PATTERN = Pattern.compile("^feb\\.?(ruary)?$");
    private static final Pattern MAR_PATTERN = Pattern.compile("^mar\\.?(ch)?$");
    private static final Pattern APR_PATTERN = Pattern.compile("^apr\\.?(il)?$");
    private static final Pattern MAY_PATTERN = Pattern.compile("^may$");
    private static final Pattern JUN_PATTERN = Pattern.compile("^jun\\.?e?$");
    private static final Pattern JUL_PATTERN = Pattern.compile("^jul\\.?y?$");
    private static final Pattern AUG_PATTERN = Pattern.compile("^aug\\.?(ust)?$");
    private static final Pattern SEP_PATTERN = Pattern.compile("^sep\\.?(t\\.?|tember)?$");
    private static final Pattern OCT_PATTERN = Pattern.compile("^oct\\.?(ober)?$");
    private static final Pattern NOV_PATTERN = Pattern.compile("^nov\\.?(ember)?$");
    private static final Pattern DEC_PATTERN = Pattern.compile("^dec\\.?(ember)?$");

    private static final int MONTH_SECONDS = 2592000; // 30 * 24 * 60 * 60
    private ZonedDateTime currentMonthBegin;

    public RepeaterMonthName(MonthName type) {
        super(type);
    }

    public static RepeaterMonthName scan(Token token) {
        Map<Pattern, MonthName> scanner = new HashMap<>();
        scanner.put(RepeaterMonthName.JAN_PATTERN, MonthName.JANUARY);
        scanner.put(RepeaterMonthName.FEB_PATTERN, MonthName.FEBRUARY);
        scanner.put(RepeaterMonthName.MAR_PATTERN, MonthName.MARCH);
        scanner.put(RepeaterMonthName.APR_PATTERN, MonthName.APRIL);
        scanner.put(RepeaterMonthName.MAY_PATTERN, MonthName.MAY);
        scanner.put(RepeaterMonthName.JUN_PATTERN, MonthName.JUNE);
        scanner.put(RepeaterMonthName.JUL_PATTERN, MonthName.JULY);
        scanner.put(RepeaterMonthName.AUG_PATTERN, MonthName.AUGUST);
        scanner.put(RepeaterMonthName.SEP_PATTERN, MonthName.SEPTEMBER);
        scanner.put(RepeaterMonthName.OCT_PATTERN, MonthName.OCTOBER);
        scanner.put(RepeaterMonthName.NOV_PATTERN, MonthName.NOVEMBER);
        scanner.put(RepeaterMonthName.DEC_PATTERN, MonthName.DECEMBER);
        for (Map.Entry<Pattern, MonthName> entry : scanner.entrySet()) {
            Pattern scannerItem = entry.getKey();
            if (scannerItem.matcher(token.getWord()).matches()) {
                return new RepeaterMonthName(scanner.get(scannerItem));
            }
        }
        return null;
    }

    public int getIndex() {
        return getType().ordinal();
    }

    @Override
    protected Span _nextSpan(PointerType pointer) {
        if (currentMonthBegin == null) {
            int targetMonth = getType().ordinal();
            int nowMonth = getNow().getMonthValue();
            if (pointer == PointerType.FUTURE) {
                if (nowMonth < targetMonth) {
                    currentMonthBegin = ZonedDateTime.of(getNow().getYear(), targetMonth, 1, 0, 0, 0, 0, getNow().getZone());
                } else if (nowMonth > targetMonth) {
                    currentMonthBegin = ZonedDateTime.of(getNow().getYear(), targetMonth, 1, 0, 0, 0, 0, getNow().getZone())
                            .plus(1, ChronoUnit.YEARS);
                }
            } else if (pointer == PointerType.NONE) {
                if (nowMonth <= targetMonth) {
                    currentMonthBegin = ZonedDateTime.of(getNow().getYear(), targetMonth, 1, 0, 0, 0, 0, getNow().getZone());
                } else if (nowMonth > targetMonth) {
                    currentMonthBegin = ZonedDateTime.of(getNow().getYear(), targetMonth, 1, 0, 0, 0, 0, getNow().getZone())
                            .plus(1, ChronoUnit.YEARS);
                }
            } else if (pointer == PointerType.PAST) {
                if (nowMonth > targetMonth) {
                    currentMonthBegin = ZonedDateTime.of(getNow().getYear(), targetMonth, 1, 0, 0, 0, 0, getNow().getZone());
                } else if (nowMonth <= targetMonth) {
                    currentMonthBegin = ZonedDateTime.of(getNow().getYear(), targetMonth, 1, 0, 0, 0, 0, getNow().getZone())
                            .minus(1, ChronoUnit.YEARS);
                }
            } else {
                throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }
            if (currentMonthBegin == null) {
                throw new IllegalStateException("Current month should be set by now.");
            }
        } else {
            if (pointer == PointerType.FUTURE) {
                currentMonthBegin = currentMonthBegin.plus(1, ChronoUnit.YEARS);
            } else if (pointer == PointerType.PAST) {
                currentMonthBegin = currentMonthBegin.minus(1, ChronoUnit.YEARS);
            } else {
                throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
            }
        }

        return new Span(currentMonthBegin, ChronoUnit.MONTHS, 1);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        Span span;
        if (pointer == PointerType.PAST) {
            span = nextSpan(pointer);
        } else if (pointer == PointerType.FUTURE || pointer == PointerType.NONE) {
            span = nextSpan(PointerType.NONE);
        } else {
            throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
        }
        return span;
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public int getWidth() {
        return RepeaterMonthName.MONTH_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-monthname-" + getType();
    }

    public enum MonthName {
        _ZERO_MONTH, JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
    }

}
