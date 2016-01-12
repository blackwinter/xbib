package org.xbib.time.chronic.tags;

import org.xbib.time.chronic.Token;

import java.util.regex.Matcher;

public class OrdinalDay extends Ordinal {
    public OrdinalDay(Integer type) {
        super(type);
    }

    public static OrdinalDay scan(Token token) {
        Matcher ordinalMatcher = Ordinal.ORDINAL_PATTERN.matcher(token.getWord());
        if (ordinalMatcher.find()) {
            int ordinalValue = Integer.parseInt(ordinalMatcher.group(1));
            if (!(ordinalValue > 31)) {
                return new OrdinalDay(ordinalValue);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "-day-" + getType();
    }
}
