package org.xbib.time.chronic.tags;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Token;

import java.util.regex.Pattern;

public class ScalarMonth extends Scalar {
    private static final Pattern MONTH_PATTERN = Pattern.compile("^\\d\\d?$");

    public ScalarMonth(Integer type) {
        super(type);
    }

    public static ScalarMonth scan(Token token, Token postToken, Options options) {
        if (ScalarMonth.MONTH_PATTERN.matcher(token.getWord()).matches()) {
            int scalarValue = Integer.parseInt(token.getWord());
            if (!(scalarValue > 12 || (postToken != null && Scalar.TIMES.contains(postToken.getWord())))) {
                return new ScalarMonth(scalarValue);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "-month-" + getType();
    }
}
