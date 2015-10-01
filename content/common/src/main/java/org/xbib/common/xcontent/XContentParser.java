
package org.xbib.common.xcontent;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public interface XContentParser extends Closeable {

    enum Token {
        START_OBJECT {
            @Override
            public boolean isValue() {
                return false;
            }
        },

        END_OBJECT {
            @Override
            public boolean isValue() {
                return false;
            }
        },

        START_ARRAY {
            @Override
            public boolean isValue() {
                return false;
            }
        },

        END_ARRAY {
            @Override
            public boolean isValue() {
                return false;
            }
        },

        FIELD_NAME {
            @Override
            public boolean isValue() {
                return false;
            }
        },

        VALUE_STRING {
            @Override
            public boolean isValue() {
                return true;
            }
        },

        VALUE_NUMBER {
            @Override
            public boolean isValue() {
                return true;
            }
        },

        VALUE_BOOLEAN {
            @Override
            public boolean isValue() {
                return true;
            }
        },

        // usually a binary value
        VALUE_EMBEDDED_OBJECT {
            @Override
            public boolean isValue() {
                return true;
            }
        },

        VALUE_NULL {
            @Override
            public boolean isValue() {
                return false;
            }
        };

        public abstract boolean isValue();
    }

    enum NumberType {
        INT, LONG, FLOAT, DOUBLE, BIG_DECIMAL, BIG_INTEGER
    }

    XContentType contentType();

    Token nextToken() throws IOException;

    void skipChildren() throws IOException;

    Token currentToken();

    String currentName() throws IOException;

    Map<String, Object> map() throws IOException;

    Map<String, Object> mapOrdered() throws IOException;

    Map<String, Object> mapAndClose() throws IOException;

    Map<String, Object> mapOrderedAndClose() throws IOException;

    String text() throws IOException;

    String textOrNull() throws IOException;

    boolean hasTextCharacters();

    char[] textCharacters() throws IOException;

    int textLength() throws IOException;

    int textOffset() throws IOException;

    Number numberValue() throws IOException;

    NumberType numberType() throws IOException;

    /**
     * Is the number type estimated or not (i.e. an int might actually be a long, its just low enough
     * to be an int).
     */
    boolean estimatedNumberType();

    short shortValue() throws IOException;

    int intValue() throws IOException;

    long longValue() throws IOException;

    float floatValue() throws IOException;

    double doubleValue() throws IOException;

    XContentParser losslessDecimals(boolean b);

    boolean isLosslessDecimals();

    BigInteger bigIntegerValue() throws IOException;

    BigDecimal bigDecimalValue() throws IOException;

    boolean isBooleanValue() throws IOException;

    boolean booleanValue() throws IOException;

    byte[] binaryValue() throws IOException;

    XContentParser enableBase16Checks(boolean b);

    boolean isBase16Checks();

    void close();
}
