package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class RepeaterUnit extends Repeater<Object> {
    private static final Pattern YEAR_PATTERN = Pattern.compile("^years?$");
    private static final Pattern SEASON_PATTERN = Pattern.compile("^seasons?$");
    private static final Pattern MONTH_PATTERN = Pattern.compile("^months?$");
    private static final Pattern FORTNIGHT_PATTERN = Pattern.compile("^fortnights?$");
    private static final Pattern WEEK_PATTERN = Pattern.compile("^weeks?$");
    private static final Pattern WEEKEND_PATTERN = Pattern.compile("^weekends?$");
    private static final Pattern DAY_PATTERN = Pattern.compile("^days?$");
    private static final Pattern HOUR_PATTERN = Pattern.compile("^hours?$");
    private static final Pattern MINUTE_PATTERN = Pattern.compile("^minutes?$");
    private static final Pattern SECOND_PATTERN = Pattern.compile("^seconds?$");

    public RepeaterUnit() {
        super(null);
    }

    public static RepeaterUnit scan(Token token) {
        try {
            Map<Pattern, UnitName> scanner = new HashMap<>();
            scanner.put(RepeaterUnit.YEAR_PATTERN, UnitName.YEAR);
            scanner.put(RepeaterUnit.SEASON_PATTERN, UnitName.SEASON);
            scanner.put(RepeaterUnit.MONTH_PATTERN, UnitName.MONTH);
            scanner.put(RepeaterUnit.FORTNIGHT_PATTERN, UnitName.FORTNIGHT);
            scanner.put(RepeaterUnit.WEEK_PATTERN, UnitName.WEEK);
            scanner.put(RepeaterUnit.WEEKEND_PATTERN, UnitName.WEEKEND);
            scanner.put(RepeaterUnit.DAY_PATTERN, UnitName.DAY);
            scanner.put(RepeaterUnit.HOUR_PATTERN, UnitName.HOUR);
            scanner.put(RepeaterUnit.MINUTE_PATTERN, UnitName.MINUTE);
            scanner.put(RepeaterUnit.SECOND_PATTERN, UnitName.SECOND);
            for (Map.Entry<Pattern, UnitName> entry : scanner.entrySet()) {
                Pattern scannerItem = entry.getKey();
                if (scannerItem.matcher(token.getWord()).matches()) {
                    UnitName unitNameEnum = scanner.get(scannerItem);
                    String unitName = unitNameEnum.name();
                    String capitalizedUnitName = unitName.substring(0, 1) + unitName.substring(1).toLowerCase();
                    String repeaterClassName = RepeaterUnit.class.getPackage().getName() + ".Repeater" + capitalizedUnitName;
                    return Class.forName(repeaterClassName).asSubclass(RepeaterUnit.class).newInstance();
                }
            }
            return null;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to create RepeaterUnit.", t);
        }
    }

    public enum UnitName {
        YEAR, SEASON, MONTH, FORTNIGHT, WEEK, WEEKEND, DAY, HOUR, MINUTE, SECOND
    }
}
