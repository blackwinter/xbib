package org.xbib.time.chronic.tags;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Pointer extends Tag<Pointer.PointerType> {
    private static final Pattern IN_PATTERN = Pattern.compile("\\bin\\b");
    private static final Pattern FUTURE_PATTERN = Pattern.compile("\\bfuture\\b");
    private static final Pattern PAST_PATTERN = Pattern.compile("\\bpast\\b");

    public Pointer(PointerType type) {
        super(type);
    }

    public static List<Token> scan(List<Token> tokens, Options options) {
        for (Token token : tokens) {
            Pointer t = Pointer.scanForAll(token, options);
            if (t != null) {
                token.tag(t);
            }
        }
        return tokens;
    }

    public static Pointer scanForAll(Token token, Options options) {
        Map<Pattern, PointerType> scanner = new HashMap<>();
        scanner.put(Pointer.PAST_PATTERN, PointerType.PAST);
        scanner.put(Pointer.FUTURE_PATTERN, PointerType.FUTURE);
        scanner.put(Pointer.IN_PATTERN, PointerType.FUTURE);
        for (Pattern scannerItem : scanner.keySet()) {
            if (scannerItem.matcher(token.getWord()).matches()) {
                return new Pointer(scanner.get(scannerItem));
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "pointer-" + getType();
    }

    public enum PointerType {
        PAST, FUTURE, NONE
    }
}
