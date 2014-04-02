
package org.xbib.common.xcontent;

import org.xbib.common.Strings;

public class XContentBuilderString {

    private final XContentString underscore;

    private final XContentString camelCase;

    public XContentBuilderString(String value) {
        underscore = new XContentString(Strings.toUnderscoreCase(value));
        camelCase = new XContentString(Strings.toCamelCase(value));
    }

    public XContentString underscore() {
        return underscore;
    }

    public XContentString camelCase() {
        return camelCase;
    }
}
