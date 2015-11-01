
package org.xbib.common.xcontent;

import org.xbib.io.BytesArray;
import org.xbib.io.BytesReference;
import org.xbib.common.xcontent.xml.XmlXParams;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XContentHelper {

    public static XContentParser createParser(BytesReference bytes) throws IOException {
        if (bytes.hasArray()) {
            return createParser(bytes.array(), bytes.arrayOffset(), bytes.length());
        }
        return XContentFactory.xContent(bytes).createParser(bytes.streamInput());
    }

    public static XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        return XContentFactory.xContent(data, offset, length).createParser(data, offset, length);
    }

    public static Map<String, Object> convertFromJsonToMap(Reader reader) {
        try {
            return XContentFactory.xContent(XContentType.JSON).createParser(reader).mapOrderedAndClose();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse content to map", e);
        }
    }

    public static Map<String, Object> convertFromXmlToMap(Reader reader) {
        try {
            return XContentFactory.xContent(XContentType.XML).createParser(reader).mapOrderedAndClose();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse content to map", e);
        }
    }

    public static Map<String, Object> convertFromXmlToMap(String data) {
        try {
            return XContentFactory.xContent(XContentType.XML).createParser(data).mapOrderedAndClose();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse content to map", e);
        }
    }

    public static Map<String, Object> convertToMap(String data) {
        try {
            return XContentFactory.xContent(XContentFactory.xContentType(data)).createParser(data).mapOrderedAndClose();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse content to map", e);
        }
    }

    public static Map<String, Object> convertToMap(BytesReference bytes, boolean ordered) {
        if (bytes.hasArray()) {
            return convertToMap(bytes.array(), bytes.arrayOffset(), bytes.length(), ordered);
        }
        try {
            XContentType contentType = XContentFactory.xContentType(bytes);
            XContentParser parser = XContentFactory.xContent(contentType).createParser(bytes.streamInput());
            if (ordered) {
                return parser.mapOrderedAndClose();
            } else {
                return parser.mapAndClose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse content to map", e);
        }
    }

    public static Map<String, Object> convertToMap(byte[] data, boolean ordered) {
        return convertToMap(data, 0, data.length, ordered);
    }

    public static Map<String, Object> convertToMap(byte[] data, int offset, int length, boolean ordered){
        try {
            XContentParser parser;
            XContentType contentType;
                contentType = XContentFactory.xContentType(data, offset, length);
                parser = XContentFactory.xContent(contentType).createParser(data, offset, length);
            if (ordered) {
                return parser.mapOrderedAndClose();
            } else {
                return parser.mapAndClose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse content to map", e);
        }
    }

    public static String convertToJson(BytesReference bytes, boolean reformatJson) throws IOException {
        return convertToJson(bytes, reformatJson, false);
    }

    public static String convertToJson(BytesReference bytes, boolean reformatJson, boolean prettyPrint) throws IOException {
        if (bytes.hasArray()) {
            return convertToJson(bytes.array(), bytes.arrayOffset(), bytes.length(), reformatJson, prettyPrint);
        }
        XContentType xContentType = XContentFactory.xContentType(bytes);
        if (xContentType == XContentType.JSON && !reformatJson) {
            BytesArray bytesArray = bytes.toBytesArray();
            return new String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length(), "UTF-8");
        }
        XContentParser parser = null;
        try {
            parser = XContentFactory.xContent(xContentType).createParser(bytes.streamInput());
            parser.nextToken();
            XContentBuilder builder = XContentFactory.jsonBuilder();
            if (prettyPrint) {
                builder.prettyPrint();
            }
            builder.copyCurrentStructure(parser);
            return builder.string();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public static String convertToJson(byte[] data, int offset, int length, boolean reformatJson) throws IOException {
        return convertToJson(data, offset, length, reformatJson, false);
    }

    public static String convertToJson(byte[] data, int offset, int length, boolean reformatJson, boolean prettyPrint) throws IOException {
        XContentType xContentType = XContentFactory.xContentType(data, offset, length);
        if (xContentType == XContentType.JSON && !reformatJson) {
            return new String(data, offset, length, "UTF-8");
        }
        XContentParser parser = null;
        try {
            parser = XContentFactory.xContent(xContentType).createParser(data, offset, length);
            parser.nextToken();
            XContentBuilder builder = XContentFactory.jsonBuilder();
            if (prettyPrint) {
                builder.prettyPrint();
            }
            builder.copyCurrentStructure(parser);
            return builder.string();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public static String convertToXml(byte[] data, int offset, int length) throws IOException {
        return convertToXml(XmlXParams.getDefaultParams(), data, offset, length, false);
    }

    public static String convertToXml(byte[] data, int offset, int length, boolean prettyprint) throws IOException {
        return convertToXml(XmlXParams.getDefaultParams(), data, offset, length, prettyprint);
    }

    public static String convertToXml(XmlXParams params, byte[] data, int offset, int length) throws IOException {
        return convertToXml(params, data, offset, length, false);
    }

    public static String convertToXml(XmlXParams params, byte[] data, int offset, int length, boolean prettyPrint) throws IOException {
        XContentType xContentType = XContentFactory.xContentType(data, offset, length);
        XContentParser parser = null;
        try {
            parser = XContentFactory.xContent(xContentType).createParser(data, offset, length);
            parser.nextToken();
            XContentBuilder builder = XContentFactory.xmlBuilder(params);
            if (prettyPrint) {
                builder.prettyPrint();
            }
            builder.copyCurrentStructure(parser);
            return builder.string();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    /**
     * Updates the provided changes into the source. If the key exists in the changes, it overrides the one in source
     * unless both are Maps, in which case it recuersively updated it.
     */
    public static void update(Map<String, Object> source, Map<String, Object> changes) {
        for (Map.Entry<String, Object> changesEntry : changes.entrySet()) {
            if (!source.containsKey(changesEntry.getKey())) {
                // safe to copy, change does not exist in source
                source.put(changesEntry.getKey(), changesEntry.getValue());
            } else {
                if (source.get(changesEntry.getKey()) instanceof Map && changesEntry.getValue() instanceof Map) {
                    // recursive merge maps
                    update((Map<String, Object>) source.get(changesEntry.getKey()), (Map<String, Object>) changesEntry.getValue());
                } else {
                    // update the field
                    source.put(changesEntry.getKey(), changesEntry.getValue());
                }
            }
        }
    }

    /**
     * Merges the defaults provided as the second parameter into the content of the first. Only does recursive merge
     * for inner maps.
     */
    @SuppressWarnings({"unchecked"})
    public static void mergeDefaults(Map<String, Object> content, Map<String, Object> defaults) {
        for (Map.Entry<String, Object> defaultEntry : defaults.entrySet()) {
            if (!content.containsKey(defaultEntry.getKey())) {
                // copy it over, it does not exists in the content
                content.put(defaultEntry.getKey(), defaultEntry.getValue());
            } else {
                // in the content and in the default, only merge compound ones (maps)
                if (content.get(defaultEntry.getKey()) instanceof Map && defaultEntry.getValue() instanceof Map) {
                    mergeDefaults((Map<String, Object>) content.get(defaultEntry.getKey()), (Map<String, Object>) defaultEntry.getValue());
                } else if (content.get(defaultEntry.getKey()) instanceof List && defaultEntry.getValue() instanceof List) {
                    List defaultList = (List) defaultEntry.getValue();
                    List contentList = (List) content.get(defaultEntry.getKey());

                    List mergedList = new ArrayList();
                    if (allListValuesAreMapsOfOne(defaultList) && allListValuesAreMapsOfOne(contentList)) {
                        // all are in the form of [ {"key1" : {}}, {"key2" : {}} ], merge based on keys
                        Map<String, Map<String, Object>> processed = new LinkedHashMap<>();
                        for (Object o : contentList) {
                            Map<String, Object> map = (Map<String, Object>) o;
                            Map.Entry<String, Object> entry = map.entrySet().iterator().next();
                            processed.put(entry.getKey(), map);
                        }
                        for (Object o : defaultList) {
                            Map<String, Object> map = (Map<String, Object>) o;
                            Map.Entry<String, Object> entry = map.entrySet().iterator().next();
                            if (processed.containsKey(entry.getKey())) {
                                mergeDefaults(processed.get(entry.getKey()), map);
                            }
                        }
                        for (Map<String, Object> map : processed.values()) {
                            mergedList.add(map);
                        }
                    } else {
                        // if both are lists, simply combine them, first the defaults, then the content
                        // just make sure not to add the same value twice
                        mergedList.addAll(defaultList);
                        for (Object o : contentList) {
                            if (!mergedList.contains(o)) {
                                mergedList.add(o);
                            }
                        }
                    }
                    content.put(defaultEntry.getKey(), mergedList);
                }
            }
        }
    }

    private static boolean allListValuesAreMapsOfOne(List list) {
        for (Object o : list) {
            if (!(o instanceof Map)) {
                return false;
            }
            if (((Map) o).size() != 1) {
                return false;
            }
        }
        return true;
    }

    public static void copyCurrentStructure(XContentGenerator generator, XContentParser parser) throws IOException {
        XContentParser.Token t = parser.currentToken();

        // Let's handle field-name separately first
        if (t == XContentParser.Token.FIELD_NAME) {
            generator.writeFieldName(parser.currentName());
            t = parser.nextToken();
            // fall-through to copy the associated value
        }

        switch (t) {
            case START_ARRAY:
                generator.writeStartArray();
                while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                    copyCurrentStructure(generator, parser);
                }
                generator.writeEndArray();
                break;
            case START_OBJECT:
                generator.writeStartObject();
                while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                    copyCurrentStructure(generator, parser);
                }
                generator.writeEndObject();
                break;
            default: // others are simple:
                copyCurrentEvent(generator, parser);
        }
    }

    public static void copyCurrentEvent(XContentGenerator generator, XContentParser parser) throws IOException {
        switch (parser.currentToken()) {
            case START_OBJECT:
                generator.writeStartObject();
                break;
            case END_OBJECT:
                generator.writeEndObject();
                break;
            case START_ARRAY:
                generator.writeStartArray();
                break;
            case END_ARRAY:
                generator.writeEndArray();
                break;
            case FIELD_NAME:
                generator.writeFieldName(parser.currentName());
                break;
            case VALUE_STRING:
                if (parser.hasTextCharacters()) {
                    generator.writeString(parser.textCharacters(), parser.textOffset(), parser.textLength());
                } else {
                    if (parser.isBase16Checks()) {
                        try {
                            generator.writeBinary(parseBase16(parser.text()));
                        } catch (Exception e) {
                            generator.writeString(parser.text());
                        }
                    } else {
                        generator.writeString(parser.text());
                    }
                }
                break;
            case VALUE_NUMBER:
                switch (parser.numberType()) {
                    case INT:
                        generator.writeNumber(parser.intValue());
                        break;
                    case LONG:
                        generator.writeNumber(parser.longValue());
                        break;
                    case FLOAT:
                        generator.writeNumber(parser.floatValue());
                        break;
                    case DOUBLE:
                        if (parser.isLosslessDecimals()) {
                            generator.writeNumber(parser.bigDecimalValue());
                        } else {
                            generator.writeNumber(parser.doubleValue());
                        }
                        break;
                    case BIG_INTEGER:
                        generator.writeNumber(parser.bigIntegerValue());
                        break;
                    case BIG_DECIMAL:
                        generator.writeNumber(parser.bigDecimalValue());
                }
                break;
            case VALUE_BOOLEAN:
                generator.writeBoolean(parser.booleanValue());
                break;
            case VALUE_NULL:
                generator.writeNull();
                break;
            case VALUE_EMBEDDED_OBJECT:
                generator.writeBinary(parser.binaryValue());
        }
    }

    public static byte[] parseBase16(String s) {
        final int len = s.length();
        if( len%2 != 0 ) {
            throw new IllegalArgumentException("hex string needs to be of even length: " + s);
        }
        byte[] out = new byte[len/2];
        for (int i = 0; i < len; i += 2) {
            int h = hexToBin(s.charAt(i));
            int l = hexToBin(s.charAt(i+1));
            if( h==-1 || l==-1 ) {
                throw new IllegalArgumentException("contains illegal character for hex string: " + s);
            }
            out[i/2] = (byte)(h*16+l);
        }
        return out;
    }

    private static int hexToBin(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch-'0';
        }
        if ('A' <= ch && ch <= 'F') {
            return ch-'A'+10;
        }
        if ('a' <= ch && ch <= 'f') {
            return ch-'a'+10;
        }
        return -1;
    }

}
