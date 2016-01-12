package org.xbib.time.chronic.tags;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SeparatorIn extends Separator {
    private static final Pattern IN_PATTERN = Pattern.compile("^in$");

    public SeparatorIn(SeparatorType type) {
        super(type);
    }

    public static SeparatorIn scan(Token token, Options options) {
        Map<Pattern, SeparatorType> scanner = new HashMap<>();
        scanner.put(SeparatorIn.IN_PATTERN, SeparatorType.IN);
        for (Pattern scannerItem : scanner.keySet()) {
            if (scannerItem.matcher(token.getWord()).matches()) {
                return new SeparatorIn(scanner.get(scannerItem));
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "-in";
    }
}
