package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RepeaterDayName extends Repeater<RepeaterDayName.DayName> {
    public static final int DAY_SECONDS = 86400; // (24 * 60 * 60);
    private static final Pattern MON_PATTERN = Pattern.compile("^m[ou]n(day)?$");
    private static final Pattern TUE_PATTERN = Pattern.compile("^t(ue|eu|oo|u|)s(day)?$");
    private static final Pattern TUE_PATTERN_1 = Pattern.compile("^tue$");
    private static final Pattern WED_PATTERN_1 = Pattern.compile("^we(dnes|nds|nns)day$");
    private static final Pattern WED_PATTERN_2 = Pattern.compile("^wed$");
    private static final Pattern THU_PATTERN_1 = Pattern.compile("^th(urs|ers)day$");
    private static final Pattern THU_PATTERN_2 = Pattern.compile("^thu$");
    private static final Pattern FRI_PATTERN = Pattern.compile("^fr[iy](day)?$");
    private static final Pattern SAT_PATTERN = Pattern.compile("^sat(t?[ue]rday)?$");
    private static final Pattern SUN_PATTERN = Pattern.compile("^su[nm](day)?$");
    private ZonedDateTime currentDayStart;

    public RepeaterDayName(DayName type) {
        super(type);
    }

    public static RepeaterDayName scan(Token token) {
        Map<Pattern, DayName> scanner = new HashMap<>();
        scanner.put(RepeaterDayName.MON_PATTERN, DayName.MONDAY);
        scanner.put(RepeaterDayName.TUE_PATTERN, DayName.TUESDAY);
        scanner.put(RepeaterDayName.TUE_PATTERN_1, DayName.TUESDAY);
        scanner.put(RepeaterDayName.WED_PATTERN_1, DayName.WEDNESDAY);
        scanner.put(RepeaterDayName.WED_PATTERN_2, DayName.WEDNESDAY);
        scanner.put(RepeaterDayName.THU_PATTERN_1, DayName.THURSDAY);
        scanner.put(RepeaterDayName.THU_PATTERN_2, DayName.THURSDAY);
        scanner.put(RepeaterDayName.FRI_PATTERN, DayName.FRIDAY);
        scanner.put(RepeaterDayName.SAT_PATTERN, DayName.SATURDAY);
        scanner.put(RepeaterDayName.SUN_PATTERN, DayName.SUNDAY);
        for (Map.Entry<Pattern, DayName> entry : scanner.entrySet()) {
            Pattern scannerItem = entry.getKey();
            if (scannerItem.matcher(token.getWord()).matches()) {
                return new RepeaterDayName(scanner.get(scannerItem));
            }
        }
        return null;
    }

    @Override
    protected Span _nextSpan(PointerType pointer) {
        int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
        if (currentDayStart == null) {
            currentDayStart = ymd(getNow());
            currentDayStart = currentDayStart.plus(direction, ChronoUnit.DAYS);
            int dayNum = getType().ordinal();
            while ((currentDayStart.get(ChronoField.DAY_OF_WEEK) - 1) != dayNum) {
                currentDayStart = currentDayStart.plus(direction, ChronoUnit.DAYS);
            }
        } else {
            currentDayStart = currentDayStart.plus(direction * 7, ChronoUnit.DAYS);
        }
        return new Span(currentDayStart, ChronoUnit.DAYS, 1);
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        if (pointer == PointerType.NONE) {
            pointer = PointerType.FUTURE;
        }
        return super.nextSpan(pointer);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public int getWidth() {
        return RepeaterDayName.DAY_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-dayname-" + getType();
    }

    public enum DayName {
         MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    private static ZonedDateTime ymd(ZonedDateTime zonedDateTime) {
        return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth(),
                0, 0, 0, 0, zonedDateTime.getZone());
    }
}
