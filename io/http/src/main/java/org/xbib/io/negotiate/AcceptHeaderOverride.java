package org.xbib.io.negotiate;

import java.util.regex.Pattern;

public class AcceptHeaderOverride {

    private Pattern userAgentPattern;

    private String original;

    private String replacement;

    AcceptHeaderOverride(Pattern userAgentPattern, String original, String replacement) {
        this.userAgentPattern = userAgentPattern;
        this.original = original;
        this.replacement = replacement;
    }

    boolean matches(String acceptHeader) {
        return matches(acceptHeader, null);
    }

    boolean matches(String acceptHeader, String userAgentHeader) {
        return (userAgentPattern == null
                || userAgentPattern.matcher(userAgentHeader).find())
                && (original == null || original.equals(acceptHeader));
    }

    String getReplacement() {
        return replacement;
    }
}