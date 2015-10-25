
package org.xbib.common.xcontent.support;

import org.xbib.common.Booleans;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractXContentParser implements XContentParser {

    protected boolean losslessDecimals;

    protected boolean base16Checks;

    @Override
    public boolean isBooleanValue() throws IOException {
        switch (currentToken()) {
            case VALUE_BOOLEAN:
                return true;
            case VALUE_NUMBER:
                NumberType numberType = numberType();
                return numberType == NumberType.LONG || numberType == NumberType.INT;
            case VALUE_STRING:
                return Booleans.isBoolean(textCharacters(), textOffset(), textLength());
            default:
                return false;
        }
    }

    public boolean booleanValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_NUMBER) {
            return intValue() != 0;
        } else if (token == Token.VALUE_STRING) {
            String s = new String(textCharacters(), textOffset(), textLength());
            return Boolean.parseBoolean(s);
        }
        return doBooleanValue();
    }

    protected abstract boolean doBooleanValue() throws IOException;

    public short shortValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Short.parseShort(text());
        }
        return doShortValue();
    }

    protected abstract short doShortValue() throws IOException;

    public int intValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Integer.parseInt(text());
        }
        return doIntValue();
    }

    protected abstract int doIntValue() throws IOException;

    public long longValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Long.parseLong(text());
        }
        return doLongValue();
    }

    protected abstract long doLongValue() throws IOException;

    public float floatValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Float.parseFloat(text());
        }
        return doFloatValue();
    }

    protected abstract float doFloatValue() throws IOException;

    public double doubleValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Double.parseDouble(text());
        }
        return doDoubleValue();
    }

    protected abstract double doDoubleValue() throws IOException;

    public XContentParser losslessDecimals(boolean losslessDecimals) {
        this.losslessDecimals = losslessDecimals;
        return this;
    }

    public boolean isLosslessDecimals() {
        return losslessDecimals;
    }

    public XContentParser enableBase16Checks(boolean base16Checks) {
        this.base16Checks = base16Checks;
        return this;
    }

    public boolean isBase16Checks() {
        return base16Checks;
    }

    public String textOrNull() throws IOException {
        if (currentToken() == Token.VALUE_NULL) {
            return null;
        }
        return text();
    }

    public Map<String, Object> map() throws IOException {
        return readMap(this);
    }

    public Map<String, Object> mapOrdered() throws IOException {
        return readOrderedMap(this);
    }

    public Map<String, Object> mapAndClose() throws IOException {
        try {
            return map();
        } finally {
            close();
        }
    }

    public Map<String, Object> mapOrderedAndClose() throws IOException {
        try {
            return mapOrdered();
        } finally {
            close();
        }
    }

    static interface MapFactory {
        Map<String, Object> newMap();
    }

    static final MapFactory SIMPLE_MAP_FACTORY = new MapFactory() {
        @Override
        public Map<String, Object> newMap() {
            return new HashMap<String, Object>();
        }
    };

    static final MapFactory ORDERED_MAP_FACTORY = new MapFactory() {
        @Override
        public Map<String, Object> newMap() {
            return new LinkedHashMap<String, Object>();
        }
    };

    static Map<String, Object> readMap(XContentParser parser) throws IOException {
        return readMap(parser, SIMPLE_MAP_FACTORY);
    }

    static Map<String, Object> readOrderedMap(XContentParser parser) throws IOException {
        return readMap(parser, ORDERED_MAP_FACTORY);
    }

    static Map<String, Object> readMap(XContentParser parser, MapFactory mapFactory) throws IOException {
        Map<String, Object> map = mapFactory.newMap();
        XContentParser.Token t = parser.currentToken();
        if (t == null) {
            t = parser.nextToken();
        }
        if (t == XContentParser.Token.START_OBJECT) {
            t = parser.nextToken();
        }
        for (; t == XContentParser.Token.FIELD_NAME; t = parser.nextToken()) {
            // Must point to field name
            String fieldName = parser.currentName();
            // And then the value...
            t = parser.nextToken();
            Object value = readValue(parser, mapFactory, t);
            map.put(fieldName, value);
        }
        return map;
    }

    private static List<Object> readList(XContentParser parser, MapFactory mapFactory, XContentParser.Token t) throws IOException {
        ArrayList<Object> list = new ArrayList<Object>();
        while ((t = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
            list.add(readValue(parser, mapFactory, t));
        }
        return list;
    }

    private static Object readValue(XContentParser parser, MapFactory mapFactory, XContentParser.Token t) throws IOException {
        if (t == XContentParser.Token.VALUE_NULL) {
            return null;
        } else if (t == XContentParser.Token.VALUE_STRING) {
            if (parser.isBase16Checks()) {
                try {
                    return XContentHelper.parseBase16(parser.text());
                } catch (Exception e) {
                    //
                }
            }
            return parser.text();
        } else if (t == XContentParser.Token.VALUE_NUMBER) {
            XContentParser.NumberType numberType = parser.numberType();
            if (numberType == XContentParser.NumberType.INT) {
                return parser.isLosslessDecimals() ? parser.bigIntegerValue() : parser.intValue();
            } else if (numberType == XContentParser.NumberType.LONG) {
                return  parser.isLosslessDecimals() ? parser.bigIntegerValue() : parser.longValue();
            } else if (numberType == XContentParser.NumberType.FLOAT) {
                return parser.isLosslessDecimals() ? parser.bigDecimalValue() : parser.floatValue();
            } else if (numberType == XContentParser.NumberType.DOUBLE) {
                return parser.isLosslessDecimals() ? parser.bigDecimalValue() : parser.doubleValue();
            } else if (numberType == NumberType.BIG_INTEGER) {
                return parser.bigIntegerValue();
            } else if (numberType == NumberType.BIG_DECIMAL) {
                return parser.bigDecimalValue();
            }
        } else if (t == XContentParser.Token.VALUE_BOOLEAN) {
            return parser.booleanValue();
        } else if (t == XContentParser.Token.START_OBJECT) {
            return readMap(parser, mapFactory);
        } else if (t == XContentParser.Token.START_ARRAY) {
            return readList(parser, mapFactory, t);
        } else if (t == XContentParser.Token.VALUE_EMBEDDED_OBJECT) {
            return parser.binaryValue();
        }
        return null;
    }
}
