package org.xbib.time.pretty.i18n;

import java.util.HashMap;
import java.util.Map;

public class Resources extends MapResourceBundle {
    private static final Map<String,Object> map = new HashMap<String,Object>() {{
        put("CenturyPattern", "%n %u");
        put("CenturyFuturePrefix", "");
        put("CenturyFutureSuffix", " from now");
        put("CenturyPastPrefix", "");
        put("CenturyPastSuffix", " ago");
        put("CenturySingularName", "century");
        put("CenturyPluralName", "centuries");
        put("DayPattern", "%n %u");
        put("DayFuturePrefix", "");
        put("DayFutureSuffix", " from now");
        put("DayPastPrefix", "");
        put("DayPastSuffix", " ago");
        put("DaySingularName", "day");
        put("DayPluralName", "days");
        put("DecadePattern", "%n %u");
        put("DecadeFuturePrefix", "");
        put("DecadeFutureSuffix", " from now");
        put("DecadePastPrefix", "");
        put("DecadePastSuffix", " ago");
        put("DecadeSingularName", "decade");
        put("DecadePluralName", "decades");
        put("HourPattern", "%n %u");
        put("HourFuturePrefix", "");
        put("HourFutureSuffix", " from now");
        put("HourPastPrefix", "");
        put("HourPastSuffix", " ago");
        put("HourSingularName", "hour");
        put("HourPluralName", "hours");
        put("JustNowPattern", "%u");
        put("JustNowFuturePrefix", "");
        put("JustNowFutureSuffix", "moments from now");
        put("JustNowPastPrefix", "moments ago");
        put("JustNowPastSuffix", "");
        put("JustNowSingularName", "");
        put("JustNowPluralName", "");
        put("MillenniumPattern", "%n %u");
        put("MillenniumFuturePrefix", "");
        put("MillenniumFutureSuffix", " from now");
        put("MillenniumPastPrefix", "");
        put("MillenniumPastSuffix", " ago");
        put("MillenniumSingularName", "millennium");
        put("MillenniumPluralName", "millennia");
        put("MillisecondPattern", "%n %u");
        put("MillisecondFuturePrefix", "");
        put("MillisecondFutureSuffix", " from now");
        put("MillisecondPastPrefix", "");
        put("MillisecondPastSuffix", " ago");
        put("MillisecondSingularName", "millisecond");
        put("MillisecondPluralName", "milliseconds");
        put("MinutePattern", "%n %u");
        put("MinuteFuturePrefix", "");
        put("MinuteFutureSuffix", " from now");
        put("MinutePastPrefix", "");
        put("MinutePastSuffix", " ago");
        put("MinuteSingularName", "minute");
        put("MinutePluralName", "minutes");
        put("MonthPattern", "%n %u");
        put("MonthFuturePrefix", "");
        put("MonthFutureSuffix", " from now");
        put("MonthPastPrefix", "");
        put("MonthPastSuffix", " ago");
        put("MonthSingularName", "month");
        put("MonthPluralName", "months");
        put("SecondPattern", "%n %u");
        put("SecondFuturePrefix", "");
        put("SecondFutureSuffix", " from now");
        put("SecondPastPrefix", "");
        put("SecondPastSuffix", " ago");
        put("SecondSingularName", "second");
        put("SecondPluralName", "seconds");
        put("WeekPattern", "%n %u");
        put("WeekFuturePrefix", "");
        put("WeekFutureSuffix", " from now");
        put("WeekPastPrefix", "");
        put("WeekPastSuffix", " ago");
        put("WeekSingularName", "week");
        put("WeekPluralName", "weeks");
        put("YearPattern", "%n %u");
        put("YearFuturePrefix", "");
        put("YearFutureSuffix", " from now");
        put("YearPastPrefix", "");
        put("YearPastSuffix", " ago");
        put("YearSingularName", "year");
        put("YearPluralName", "years");
        put("AbstractTimeUnitPattern", "");
        put("AbstractTimeUnitFuturePrefix", "");
        put("AbstractTimeUnitFutureSuffix", "");
        put("AbstractTimeUnitPastPrefix", "");
        put("AbstractTimeUnitPastSuffix", "");
        put("AbstractTimeUnitSingularName", "");
        put("AbstractTimeUnitPluralName", "");
    }};

    @Override
    public Map<String,Object> getContents() {
        return map;
    }

}
