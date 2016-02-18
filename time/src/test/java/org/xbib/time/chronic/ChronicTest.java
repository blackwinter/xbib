package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.chronic.handlers.Handler;
import org.xbib.time.chronic.repeaters.EnumRepeaterDayPortion;
import org.xbib.time.chronic.repeaters.RepeaterDayName;
import org.xbib.time.chronic.repeaters.RepeaterDayName.DayName;
import org.xbib.time.chronic.repeaters.RepeaterDayPortion;
import org.xbib.time.chronic.repeaters.RepeaterTime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

public class ChronicTest extends Assert {

    private final static ZoneId ZONE_ID = ZoneId.of("GMT");

    public static ZonedDateTime construct(int year, int month) {
        return ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour) {
        return ZonedDateTime.of(year, month, day, hour, 0, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour, int minutes) {
        return ZonedDateTime.of(year, month, day, hour, minutes, 0, 0, ZONE_ID);
    }

    public static ZonedDateTime construct(int year, int month, int day, int hour, int minutes, int seconds) {
        return ZonedDateTime.of(year, month, day, hour, minutes, seconds, 0, ZONE_ID);
    }

    @Test
    public void testPostNormalizeAmPmAliases() {
        List<Token> tokens = new LinkedList<>();

        // affect wanted patterns
        tokens.add(new Token("5:00"));
        tokens.add(new Token("morning"));
        tokens.get(0).tag(new RepeaterTime("5:00"));
        tokens.get(1).tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.MORNING));

        assertEquals(RepeaterDayPortion.DayPortion.MORNING, tokens.get(1).getTags().get(0).getType());

        tokens = Handler.dealiasAndDisambiguateTimes(tokens, new Options());

        assertEquals(RepeaterDayPortion.DayPortion.AM, tokens.get(1).getTags().get(0).getType());
        assertEquals(2, tokens.size());

        // don't affect unwanted patterns
        tokens = new LinkedList<>();
        tokens.add(new Token("friday"));
        tokens.add(new Token("morning"));
        tokens.get(0).tag(new RepeaterDayName(DayName.FRIDAY));
        tokens.get(1).tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.MORNING));

        assertEquals(RepeaterDayPortion.DayPortion.MORNING, tokens.get(1).getTags().get(0).getType());

        tokens = Handler.dealiasAndDisambiguateTimes(tokens, new Options());

        assertEquals(RepeaterDayPortion.DayPortion.MORNING, tokens.get(1).getTags().get(0).getType());
        assertEquals(2, tokens.size());
    }

    @Test
    public void testGuess() {
        Span span;

        span = new Span(construct(2006, 8, 16, 0), construct(2006, 8, 17, 0));
        assertEquals(construct(2006, 8, 16, 12), Chronic.guess(span).getBeginCalendar());

        span = new Span(construct(2006, 8, 16, 0), construct(2006, 8, 17, 0, 0, 1));
        assertEquals(construct(2006, 8, 16, 12), Chronic.guess(span).getBeginCalendar());

        span = new Span(construct(2006, 11), construct(2006, 12));
        assertEquals(construct(2006, 11, 16), Chronic.guess(span).getBeginCalendar());
    }
}
