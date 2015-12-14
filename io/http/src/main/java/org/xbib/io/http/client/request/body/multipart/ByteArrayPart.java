package org.xbib.io.http.client.request.body.multipart;

import java.nio.charset.Charset;

public class ByteArrayPart extends FileLikePart {

    private final byte[] bytes;

    public ByteArrayPart(String name, byte[] bytes) {
        this(name, bytes, null);
    }

    public ByteArrayPart(String name, byte[] bytes, String contentType) {
        this(name, bytes, contentType, null);
    }

    public ByteArrayPart(String name, byte[] bytes, String contentType, Charset charset) {
        this(name, bytes, contentType, charset, null);
    }

    public ByteArrayPart(String name, byte[] bytes, String contentType, Charset charset, String fileName) {
        this(name, bytes, contentType, charset, fileName, null);
    }

    public ByteArrayPart(String name, byte[] bytes, String contentType, Charset charset, String fileName, String contentId) {
        this(name, bytes, contentType, charset, fileName, contentId, null);
    }

    public ByteArrayPart(String name, byte[] bytes, String contentType, Charset charset, String fileName, String contentId, String transferEncoding) {
        super(name, contentType, charset, contentId, transferEncoding);
        this.bytes = bytes;
        setFileName(fileName);
    }

    public byte[] getBytes() {
        return bytes;
    }
}
