package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.repeaters.EnumRepeaterDayPortion;
import org.xbib.time.chronic.repeaters.IntegerRepeaterDayPortion;
import org.xbib.time.chronic.repeaters.Repeater;
import org.xbib.time.chronic.repeaters.RepeaterDayName;
import org.xbib.time.chronic.repeaters.RepeaterDayPortion;
import org.xbib.time.chronic.repeaters.RepeaterMonthName;
import org.xbib.time.chronic.repeaters.RepeaterTime;
import org.xbib.time.chronic.tags.Grabber;
import org.xbib.time.chronic.tags.Ordinal;
import org.xbib.time.chronic.tags.OrdinalDay;
import org.xbib.time.chronic.tags.Pointer;
import org.xbib.time.chronic.tags.Pointer.PointerType;
import org.xbib.time.chronic.tags.Scalar;
import org.xbib.time.chronic.tags.ScalarDay;
import org.xbib.time.chronic.tags.ScalarMonth;
import org.xbib.time.chronic.tags.ScalarYear;
import org.xbib.time.chronic.tags.Separator;
import org.xbib.time.chronic.tags.SeparatorAt;
import org.xbib.time.chronic.tags.SeparatorComma;
import org.xbib.time.chronic.tags.SeparatorIn;
import org.xbib.time.chronic.tags.SeparatorSlashOrDash;
import org.xbib.time.chronic.tags.Tag;
import org.xbib.time.chronic.tags.TimeZone;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Handler {
    private static Map<HandlerType, List<Handler>> definitions;
    private HandlerPattern[] patterns;
    private IHandler handler;
    private boolean compatible;

    public Handler(IHandler handler, HandlerPattern... patterns) {
        this(handler, true, patterns);
    }

    public Handler(IHandler handler, boolean compatible, HandlerPattern... patterns) {
        this.handler = handler;
        this.compatible = compatible;
        this.patterns = patterns;
    }

    public static synchronized Map<HandlerType, List<Handler>> definitions() {
        if (definitions == null) {
            Map<HandlerType, List<Handler>> definitions = new HashMap<>();

            List<Handler> timeHandlers = new LinkedList<>();
            timeHandlers.add(new Handler(null, new TagPattern(RepeaterTime.class), new TagPattern(RepeaterDayPortion.class, true)));
            definitions.put(HandlerType.TIME, timeHandlers);

            List<Handler> dateHandlers = new LinkedList<>();
            dateHandlers.add(new Handler(new RdnRmnSdTTzSyHandler(), new TagPattern(RepeaterDayName.class), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(RepeaterTime.class), new TagPattern(TimeZone.class), new TagPattern(ScalarYear.class)));
            // DIFF: We add an optional comma to MDY
            dateHandlers.add(new Handler(new RmnSdSyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorComma.class, true), new TagPattern(ScalarYear.class)));
            dateHandlers.add(new Handler(new RmnSdSyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(HandlerType.TIME, true)));
            dateHandlers.add(new Handler(new RmnSdHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(HandlerType.TIME, true)));
            dateHandlers.add(new Handler(new RmnOdHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(OrdinalDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(HandlerType.TIME, true)));
            dateHandlers.add(new Handler(new RmnSyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarYear.class)));
            dateHandlers.add(new Handler(new SdRmnSyHandler(), new TagPattern(ScalarDay.class), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(HandlerType.TIME, true)));
            dateHandlers.add(new Handler(new SmSdSyHandler(), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(HandlerType.TIME, true)));
            dateHandlers.add(new Handler(new SdSmSyHandler(), new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(HandlerType.TIME, true)));
            dateHandlers.add(new Handler(new SySmSdHandler(), new TagPattern(ScalarYear.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(HandlerType.TIME, true)));
            // DIFF: We make 05/06 interpret as month/day before month/year
            dateHandlers.add(new Handler(new SmSdHandler(), false, new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class)));
            dateHandlers.add(new Handler(new SmSyHandler(), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class)));
            definitions.put(HandlerType.DATE, dateHandlers);

            // tonight at 7pm
            List<Handler> anchorHandlers = new LinkedList<>();
            anchorHandlers.add(new Handler(new RHandler(), new TagPattern(Grabber.class, true), new TagPattern(Repeater.class), new TagPattern(SeparatorAt.class, true), new TagPattern(Repeater.class, true), new TagPattern(Repeater.class, true)));
            anchorHandlers.add(new Handler(new RHandler(), new TagPattern(Grabber.class, true), new TagPattern(Repeater.class), new TagPattern(Repeater.class), new TagPattern(SeparatorAt.class, true), new TagPattern(Repeater.class, true), new TagPattern(Repeater.class, true)));
            anchorHandlers.add(new Handler(new RGRHandler(), new TagPattern(Repeater.class), new TagPattern(Grabber.class), new TagPattern(Repeater.class)));
            definitions.put(HandlerType.ANCHOR, anchorHandlers);

            // 3 weeks from now, in 2 months
            List<Handler> arrowHandlers = new LinkedList<>();
            arrowHandlers.add(new Handler(new SRPHandler(), new TagPattern(Scalar.class), new TagPattern(Repeater.class), new TagPattern(Pointer.class)));
            arrowHandlers.add(new Handler(new PSRHandler(), new TagPattern(Pointer.class), new TagPattern(Scalar.class), new TagPattern(Repeater.class)));
            arrowHandlers.add(new Handler(new SRPAHandler(), new TagPattern(Scalar.class), new TagPattern(Repeater.class), new TagPattern(Pointer.class), new HandlerTypePattern(HandlerType.ANCHOR)));
            definitions.put(HandlerType.ARROW, arrowHandlers);

            // 3rd week in march
            List<Handler> narrowHandlers = new LinkedList<>();
            narrowHandlers.add(new Handler(new ORSRHandler(), new TagPattern(Ordinal.class), new TagPattern(Repeater.class), new TagPattern(SeparatorIn.class), new TagPattern(Repeater.class)));
            narrowHandlers.add(new Handler(new ORGRHandler(), new TagPattern(Ordinal.class), new TagPattern(Repeater.class), new TagPattern(Grabber.class), new TagPattern(Repeater.class)));
            definitions.put(HandlerType.NARROW, narrowHandlers);
            Handler.definitions = definitions;
        }
        return definitions;
    }

    public static Span tokensToSpan(List<Token> tokens, Options options) throws ParseException {
        // maybe it's a specific date
        Map<HandlerType, List<Handler>> definitions = definitions();
        for (Handler handler : definitions.get(HandlerType.DATE)) {
            if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
                List<Token> goodTokens = new LinkedList<>();
                for (Token token : tokens) {
                    if (token.getTag(Separator.class) == null) {
                        goodTokens.add(token);
                    }
                }
                return handler.getHandler().handle(goodTokens, options);
            }
        }
        // I guess it's not a specific date, maybe it's just an anchor
        for (Handler handler : definitions.get(HandlerType.ANCHOR)) {
            if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
                List<Token> goodTokens = new LinkedList<>();
                for (Token token : tokens) {
                    if (token.getTag(Separator.class) == null) {
                        goodTokens.add(token);
                    }
                }
                return handler.getHandler().handle(goodTokens, options);
            }
        }
        // not an anchor, perhaps it's an arrow
        for (Handler handler : definitions.get(HandlerType.ARROW)) {
            if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
                List<Token> goodTokens = new LinkedList<>();
                for (Token token : tokens) {
                    if (token.getTag(SeparatorAt.class) == null && token.getTag(SeparatorSlashOrDash.class) == null && token.getTag(SeparatorComma.class) == null) {
                        goodTokens.add(token);
                    }
                }
                return handler.getHandler().handle(goodTokens, options);
            }
        }
        // not an arrow, let's hope it's a narrow
        for (Handler handler : definitions.get(HandlerType.NARROW)) {
            if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
                return handler.getHandler().handle(tokens, options);
            }
        }
        throw new ParseException("no span found for " + tokens, 0);
    }

    public static List<Repeater<?>> getRepeaters(List<Token> tokens) {
        List<Repeater<?>> repeaters = new LinkedList<>();
        for (Token token : tokens) {
            Repeater<?> tag = token.getTag(Repeater.class);
            if (tag != null) {
                repeaters.add(tag);
            }
        }
        Collections.sort(repeaters);
        Collections.reverse(repeaters);
        return repeaters;
    }

    public static Span getAnchor(List<Token> tokens, Options options) {
        Grabber grabber = new Grabber(Grabber.Relative.THIS);
        PointerType pointer = PointerType.FUTURE;

        List<Repeater<?>> repeaters = getRepeaters(tokens);
        for (int i = 0; i < repeaters.size(); i++) {
            tokens.remove(tokens.size() - 1);
        }

        if (!tokens.isEmpty() && tokens.get(0).getTag(Grabber.class) != null) {
            grabber = tokens.get(0).getTag(Grabber.class);
            tokens.remove(tokens.size() - 1);
        }

        Repeater<?> head = repeaters.remove(0);
        head.setNow(options.getNow());

        Span outerSpan;
        Grabber.Relative grabberType = grabber.getType();
        if (grabberType == Grabber.Relative.LAST) {
            outerSpan = head.nextSpan(PointerType.PAST);
        } else if (grabberType == Grabber.Relative.THIS) {
            if (repeaters.size() > 0) {
                outerSpan = head.thisSpan(PointerType.NONE);
            } else {
                outerSpan = head.thisSpan(options.getContext());
            }
        } else if (grabberType == Grabber.Relative.NEXT) {
            outerSpan = head.nextSpan(PointerType.FUTURE);
        } else {
            throw new IllegalArgumentException("Invalid grabber type " + grabberType + ".");
        }
        return findWithin(repeaters, outerSpan, pointer);
    }

    public static Span dayOrTime(ZonedDateTime dayStart, List<Token> timeTokens, Options options) {
        Span outerSpan = new Span(dayStart, dayStart.plus(1, ChronoUnit.DAYS));
        if (!timeTokens.isEmpty()) {
            options.setNow(outerSpan.getBeginCalendar());
            return getAnchor(dealiasAndDisambiguateTimes(timeTokens, options), options);
        }
        return outerSpan;
    }

    /**
     * Recursively finds repeaters within other repeaters.
     * Returns a Span representing the innermost time span
     * or nil if no repeater union could be found
     */
    public static Span findWithin(List<Repeater<?>> tags, Span span, PointerType pointer) {
        if (tags.isEmpty()) {
            return span;
        }
        Repeater<?> head = tags.get(0);
        List<Repeater<?>> rest = (tags.size() > 1) ? tags.subList(1, tags.size()) : new LinkedList<>();
        head.setNow((pointer == PointerType.FUTURE) ? span.getBeginCalendar() : span.getEndCalendar());
        Span h = head.thisSpan(PointerType.NONE);
        if (span.contains(h.getBegin()) || span.contains(h.getEnd())) {
            return findWithin(rest, h, pointer);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<Token> dealiasAndDisambiguateTimes(List<Token> tokens, Options options) {
        // handle aliases of am/pm
        // 5:00 in the morning => 5:00 am
        // 7:00 in the evening => 7:00 pm

        int dayPortionIndex = -1;
        int tokenSize = tokens.size();
        for (int i = 0; dayPortionIndex == -1 && i < tokenSize; i++) {
            Token t = tokens.get(i);
            if (t.getTag(RepeaterDayPortion.class) != null) {
                dayPortionIndex = i;
            }
        }

        int timeIndex = -1;
        for (int i = 0; timeIndex == -1 && i < tokenSize; i++) {
            Token t = tokens.get(i);
            if (t.getTag(RepeaterTime.class) != null) {
                timeIndex = i;
            }
        }

        if (dayPortionIndex != -1 && timeIndex != -1) {
            Token t1 = tokens.get(dayPortionIndex);
            Tag<RepeaterDayPortion<?>> t1Tag = t1.getTag(RepeaterDayPortion.class);

            Object t1TagType = t1Tag.getType();
            if (RepeaterDayPortion.DayPortion.MORNING.equals(t1TagType)) {
                t1.untag(RepeaterDayPortion.class);
                t1.tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.AM));
            } else if (RepeaterDayPortion.DayPortion.AFTERNOON.equals(t1TagType) || RepeaterDayPortion.DayPortion.EVENING.equals(t1TagType) || RepeaterDayPortion.DayPortion.NIGHT.equals(t1TagType)) {
                t1.untag(RepeaterDayPortion.class);
                t1.tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.PM));
            }
        }

        // handle ambiguous times if :ambiguous_time_range is specified
        if (options.getAmbiguousTimeRange() != 0) {
            List<Token> ttokens = new LinkedList<Token>();
            for (int i = 0; i < tokenSize; i++) {
                Token t0 = tokens.get(i);
                ttokens.add(t0);
                Token t1 = null;
                if (i < tokenSize - 1) {
                    t1 = tokens.get(i + 1);
                }
                if (t0.getTag(RepeaterTime.class) != null && t0.getTag(RepeaterTime.class).getType().isAmbiguous() && (t1 == null || t1.getTag(RepeaterDayPortion.class) == null)) {
                    Token distoken = new Token("disambiguator");
                    distoken.tag(new IntegerRepeaterDayPortion(options.getAmbiguousTimeRange()));
                    ttokens.add(distoken);
                }
            }
            tokens = ttokens;
        }
        return tokens;
    }

    public boolean isCompatible(Options options) {
        return !options.isCompatibilityMode() || compatible;
    }

    public IHandler getHandler() {
        return handler;
    }

    public boolean match(List<Token> tokens, Map<HandlerType, List<Handler>> definitions) {
        int tokenIndex = 0;
        for (HandlerPattern pattern : patterns) {
            boolean optional = pattern.isOptional();
            if (pattern instanceof TagPattern) {
                boolean match = (tokenIndex < tokens.size() && tokens.get(tokenIndex).getTags(((TagPattern) pattern).getTagClass()).size() > 0);
                if (!match && !optional) {
                    return false;
                }
                if (match) {
                    tokenIndex++;
                }
                // next if !match && optional ?
            } else if (pattern instanceof HandlerTypePattern) {
                if (optional && tokenIndex == tokens.size()) {
                    return true;
                }
                List<Handler> subHandlers = definitions.get(((HandlerTypePattern) pattern).getType());
                for (Handler subHandler : subHandlers) {
                    if (subHandler.match(tokens.subList(tokenIndex, tokens.size()), definitions)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return tokenIndex == tokens.size();
    }

    @Override
    public String toString() {
        return "[Handler: " + handler + "]";
    }

    public enum HandlerType {
        TIME, DATE, ANCHOR, ARROW, NARROW
    }
}
