package org.xbib.common.xcontent.xml;

import org.xbib.common.xcontent.XContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentService;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class XmlXContentHelper {

    public static Map<String, Object> convertFromXmlToMap(Reader reader) {
        try {
            return XmlXContent.xmlXContent().createParser(reader).mapOrderedAndClose();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse content to map", e);
        }
    }

    public static Map<String, Object> convertFromXmlToMap(String data) {
        try {
            return  XmlXContent.xmlXContent().createParser(data).mapOrderedAndClose();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse content to map", e);
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
        XContent xContent = XContentService.xContent(data, offset, length);
        XContentParser parser = null;
        try {
            parser = xContent.createParser(data, offset, length);
            parser.nextToken();
            XContentBuilder builder = XmlXContent.contentBuilder(params);
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

}
