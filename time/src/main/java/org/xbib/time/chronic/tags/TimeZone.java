package org.xbib.time.chronic.tags;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TimeZone extends Tag<Object> {
    private static final Pattern TIMEZONE_PATTERN = Pattern.compile("[pmce][ds]t");

    public TimeZone() {
        super(null);
    }

    public static List<Token> scan(List<Token> tokens, Options options) {
        for (Token token : tokens) {
            TimeZone t = TimeZone.scanForAll(token, options);
            if (t != null) {
                token.tag(t);
            }
        }
        return tokens;
    }

    public static TimeZone scanForAll(Token token, Options options) {
        Map<Pattern, Object> scanner = new HashMap<>();
        scanner.put(TimeZone.TIMEZONE_PATTERN, null);
        for (Pattern scannerItem : scanner.keySet()) {
            if (scannerItem.matcher(token.getWord()).matches()) {
                return new TimeZone();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "timezone";
    }
}
