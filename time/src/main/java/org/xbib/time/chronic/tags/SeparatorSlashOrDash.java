package org.xbib.time.chronic.tags;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SeparatorSlashOrDash extends Separator {
    private static final Pattern SLASH_PATTERN = Pattern.compile("^/$");
    private static final Pattern DASH_PATTERN = Pattern.compile("^-$");

    public SeparatorSlashOrDash(SeparatorType type) {
        super(type);
    }

    public static SeparatorSlashOrDash scan(Token token, Options options) {
        Map<Pattern, SeparatorType> scanner = new HashMap<>();
        scanner.put(SeparatorSlashOrDash.DASH_PATTERN, SeparatorType.DASH);
        scanner.put(SeparatorSlashOrDash.SLASH_PATTERN, SeparatorType.SLASH);
        for (Pattern scannerItem : scanner.keySet()) {
            if (scannerItem.matcher(token.getWord()).matches()) {
                return new SeparatorSlashOrDash(scanner.get(scannerItem));
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "-slashordash-" + getType();
    }
}
