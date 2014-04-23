
package org.xbib.common.xcontent;

import com.fasterxml.jackson.dataformat.smile.SmileConstants;
import org.xbib.io.BytesArray;
import org.xbib.io.BytesReference;
import org.xbib.common.xcontent.json.JsonXContent;
import org.xbib.common.xcontent.smile.SmileXContent;
import org.xbib.common.xcontent.xml.XmlXContent;
import org.xbib.common.xcontent.xml.XmlXParams;
import org.xbib.common.xcontent.yaml.YamlXContent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A one stop to use {@link XContent} and {@link XContentBuilder}.
 */
public class XContentFactory {

    private static int GUESS_HEADER_LENGTH = 20;

    /**
     * Returns a content builder using JSON format ({@link XContentType#JSON}.
     */
    public static XContentBuilder jsonBuilder() throws IOException {
        return contentBuilder(XContentType.JSON);
    }

    /**
     * Constructs a new json builder that will output the result into the provided output stream.
     */
    public static XContentBuilder jsonBuilder(OutputStream os) throws IOException {
        return new XContentBuilder(JsonXContent.jsonXContent, os);
    }

    /**
     * Returns a content builder using SMILE format ({@link XContentType#SMILE}.
     */
    public static XContentBuilder smileBuilder() throws IOException {
        return contentBuilder(XContentType.SMILE);
    }

    /**
     * Constructs a new json builder that will output the result into the provided output stream.
     */
    public static XContentBuilder smileBuilder(OutputStream os) throws IOException {
        return new XContentBuilder(SmileXContent.smileXContent, os);
    }

    /**
     * Constructs a new yaml builder that will output the result into the provided output stream.
     */
    public static XContentBuilder yamlBuilder(OutputStream os) throws IOException {
        return new XContentBuilder(YamlXContent.yamlXContent, os);
    }

    /**
     * Constructs a new xml builder using XML.
     */
    public static XContentBuilder xmlBuilder() throws IOException {
        return XmlXContent.contentBuilder();
    }

    /**
     * Constructs a new xml builder using XML.
     */
    public static XContentBuilder xmlBuilder(XmlXParams params) throws IOException {
        return XmlXContent.contentBuilder(params);
    }

    /**
     * Constructs a new xml builder that will output the result into the provided output stream.
     */
    public static XContentBuilder xmlBuilder(OutputStream os) throws IOException {
        return new XContentBuilder(XmlXContent.xmlXContent(), os);
    }

    /**
     * Constructs a xcontent builder that will output the result into the provided output stream.
     */
    public static XContentBuilder contentBuilder(XContentType type, OutputStream outputStream) throws IOException {
        if (type == XContentType.JSON) {
            return jsonBuilder(outputStream);
        } else if (type == XContentType.SMILE) {
            return smileBuilder(outputStream);
        } else if (type == XContentType.YAML) {
            return yamlBuilder(outputStream);
        } else if (type == XContentType.XML) {
            return xmlBuilder(outputStream);
        }
        throw new IllegalArgumentException("No matching content type for " + type);
    }

    /**
     * Returns a binary content builder for the provided content type.
     */
    public static XContentBuilder contentBuilder(XContentType type) throws IOException {
        if (type == XContentType.JSON) {
            return JsonXContent.contentBuilder();
        } else if (type == XContentType.SMILE) {
            return SmileXContent.contentBuilder();
        } else if (type == XContentType.YAML) {
            return YamlXContent.contentBuilder();
        } else if (type == XContentType.XML) {
            return XmlXContent.contentBuilder();
        }
        throw new IllegalArgumentException("No matching content type for " + type);
    }

    /**
     * Returns the {@link XContent} for the provided content type.
     */
    public static XContent xContent(XContentType type) {
        return type.xContent();
    }

    /**
     * Guesses the content type based on the provided char sequence.
     */
    public static XContentType xContentType(CharSequence content) {
        int length = content.length() < GUESS_HEADER_LENGTH ? content.length() : GUESS_HEADER_LENGTH;
        for (int i = 0; i < length; i++) {
            char c = content.charAt(i);
            if (c == '{') {
                return XContentType.JSON;
            }
        }
        return null;
    }

    /**
     * Guesses the content (type) based on the provided char sequence.
     */
    public static XContent xContent(CharSequence content) {
        XContentType type = xContentType(content);
        if (type == null) {
            throw new IllegalArgumentException("Failed to derive xcontent from " + content);
        }
        return xContent(type);
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XContent xContent(byte[] data) {
        return xContent(data, 0, data.length);
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XContent xContent(byte[] data, int offset, int length) {
        XContentType type = xContentType(data, offset, length);
        if (type == null) {
            throw new IllegalArgumentException("Failed to derive xcontent from (offset=" + offset + ", length=" + length + "): " + Arrays.toString(data));
        }
        return xContent(type);
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XContentType xContentType(byte[] data) {
        return xContentType(data, 0, data.length);
    }

    /**
     * Guesses the content type based on the provided input stream.
     */
    public static XContentType xContentType(InputStream si) throws IOException {
        int first = si.read();
        if (first == -1) {
            return null;
        }
        int second = si.read();
        if (second == -1) {
            return null;
        }
        if (first == SmileConstants.HEADER_BYTE_1 && second == SmileConstants.HEADER_BYTE_2) {
            int third = si.read();
            if (third == SmileConstants.HEADER_BYTE_3) {
                return XContentType.SMILE;
            }
        }
        if (first == '{' || second == '{') {
            return XContentType.JSON;
        }
        if (first == '-' && second == '-') {
            int third = si.read();
            if (third == '-') {
                return XContentType.YAML;
            }
        }
        if (first == '<' && second == '?') {
            int third = si.read();
            if (third == 'x') {
                return XContentType.XML;
            }
        }
        for (int i = 2; i < GUESS_HEADER_LENGTH; i++) {
            int val = si.read();
            if (val == -1) {
                return null;
            }
            if (val == '{') {
                return XContentType.JSON;
            }
        }
        return null;
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XContentType xContentType(byte[] data, int offset, int length) {
        return xContentType(new BytesArray(data, offset, length));
    }

    public static XContent xContent(BytesReference bytes) {
        XContentType type = xContentType(bytes);
        if (type == null) {
            throw new IllegalArgumentException("Failed to derive xcontent from " + bytes);
        }
        return xContent(type);
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XContentType xContentType(BytesReference bytes) {
        int length = bytes.length() < GUESS_HEADER_LENGTH ? bytes.length() : GUESS_HEADER_LENGTH;
        if (length == 0) {
            return null;
        }
        byte first = bytes.get(0);
        if (first == '{') {
            return XContentType.JSON;
        }
        if (length > 2 && first == SmileConstants.HEADER_BYTE_1 && bytes.get(1) == SmileConstants.HEADER_BYTE_2 && bytes.get(2) == SmileConstants.HEADER_BYTE_3) {
            return XContentType.SMILE;
        }
        if (length > 2 && first == '-' && bytes.get(1) == '-' && bytes.get(2) == '-') {
            return XContentType.YAML;
        }
        if (length > 2 && first == '<' && bytes.get(1) == '?' && bytes.get(2) == 'x') {
            return XContentType.XML;
        }
        for (int i = 0; i < length; i++) {
            if (bytes.get(i) == '{') {
                return XContentType.JSON;
            }
        }
        return null;
    }
}
