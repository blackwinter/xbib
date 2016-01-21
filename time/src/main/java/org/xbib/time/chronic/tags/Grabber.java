package org.xbib.time.chronic.tags;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Grabber extends Tag<Grabber.Relative> {
    private static final Pattern THIS_PATTERN = Pattern.compile("this");
    private static final Pattern NEXT_PATTERN = Pattern.compile("next");
    private static final Pattern LAST_PATTERN = Pattern.compile("last");

    public Grabber(Relative type) {
        super(type);
    }

    public static List<Token> scan(List<Token> tokens, Options options) {
        for (Token token : tokens) {
            Grabber t = Grabber.scanForAll(token, options);
            if (t != null) {
                token.tag(t);
            }
        }
        return tokens;
    }

    public static Grabber scanForAll(Token token, Options options) {
        Map<Pattern, Relative> scanner = new HashMap<Pattern, Relative>();
        scanner.put(Grabber.LAST_PATTERN, Relative.LAST);
        scanner.put(Grabber.NEXT_PATTERN, Relative.NEXT);
        scanner.put(Grabber.THIS_PATTERN, Relative.THIS);
        for (Map.Entry<Pattern, Relative> entry : scanner.entrySet()) {
            Pattern scannerItem = entry.getKey();
            if (scannerItem.matcher(token.getWord()).matches()) {
                return new Grabber(scanner.get(scannerItem));
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "grabber-" + getType();
    }

    public enum Relative {
        LAST, NEXT, THIS
    }
}
