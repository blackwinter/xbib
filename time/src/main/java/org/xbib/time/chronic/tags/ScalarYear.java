package org.xbib.time.chronic.tags;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Token;

import java.util.regex.Pattern;

public class ScalarYear extends Scalar {
    public static final Pattern YEAR_PATTERN = Pattern.compile("^([1-9]\\d)?\\d\\d?$");

    public ScalarYear(Integer type) {
        super(type);
    }

    public static ScalarYear scan(Token token, Token postToken, Options options) {
        if (ScalarYear.YEAR_PATTERN.matcher(token.getWord()).matches()) {
            int scalarValue = Integer.parseInt(token.getWord());
            if (!(postToken != null && Scalar.TIMES.contains(postToken.getWord()))) {
                if (scalarValue <= 37) {
                    scalarValue += 2000;
                } else if (scalarValue <= 137 && scalarValue >= 69) {
                    scalarValue += 1900;
                }
                return new ScalarYear(scalarValue);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "-year-" + getType();
    }
}
