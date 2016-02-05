package org.xbib.time.chronic.tags;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SeparatorAt extends Separator {
    private static final Pattern AT_PATTERN = Pattern.compile("^(at|@)$");

    public SeparatorAt(SeparatorType type) {
        super(type);
    }

    public static SeparatorAt scan(Token token, Options options) {
        Map<Pattern, SeparatorType> scanner = new HashMap<>();
        scanner.put(SeparatorAt.AT_PATTERN, SeparatorType.AT);
        for (Map.Entry<Pattern, SeparatorType> entry : scanner.entrySet()) {
            Pattern scannerItem = entry.getKey();
            if (scannerItem.matcher(token.getWord()).matches()) {
                return new SeparatorAt(scanner.get(scannerItem));
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "-at";
    }
}
