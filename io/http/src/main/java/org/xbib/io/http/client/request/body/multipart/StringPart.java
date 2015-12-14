package org.xbib.io.http.client.request.body.multipart;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class StringPart extends PartBase {

    /**
     * Default content encoding of string parameters.
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";

    /**
     * Default charset of string parameters
     */
    public static final Charset DEFAULT_CHARSET = US_ASCII;

    /**
     * Default transfer encoding of string parameters
     */
    public static final String DEFAULT_TRANSFER_ENCODING = "8bit";

    /**
     * Contents of this StringPart.
     */
    private final String value;

    public StringPart(String name, String value) {
        this(name, value, null);
    }

    public StringPart(String name, String value, String contentType) {
        this(name, value, contentType, null);
    }

    public StringPart(String name, String value, String contentType, Charset charset) {
        this(name, value, contentType, charset, null);
    }

    public StringPart(String name, String value, String contentType, Charset charset, String contentId) {
        this(name, value, contentType, charset, contentId, null);
    }

    public StringPart(String name, String value, String contentType, Charset charset, String contentId, String transferEncoding) {
        super(name, contentTypeOrDefault(contentType), charsetOrDefault(charset), contentId, transferEncodingOrDefault(transferEncoding));
        if (value.indexOf(0) != -1)
        // See RFC 2048, 2.8. "8bit Data"
        {
            throw new IllegalArgumentException("NULs may not be present in string parts");
        }

        this.value = value;
    }

    private static Charset charsetOrDefault(Charset charset) {
        return charset == null ? DEFAULT_CHARSET : charset;
    }

    private static String contentTypeOrDefault(String contentType) {
        return contentType == null ? DEFAULT_CONTENT_TYPE : contentType;
    }

    private static String transferEncodingOrDefault(String transferEncoding) {
        return transferEncoding == null ? DEFAULT_TRANSFER_ENCODING : transferEncoding;
    }

    public String getValue() {
        return value;
    }
}
