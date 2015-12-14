package org.xbib.io.http.client.request.body.multipart;

import io.netty.handler.codec.http.HttpHeaders;
import org.xbib.io.http.client.request.body.multipart.part.ByteArrayMultipartPart;
import org.xbib.io.http.client.request.body.multipart.part.FileMultipartPart;
import org.xbib.io.http.client.request.body.multipart.part.MessageEndMultipartPart;
import org.xbib.io.http.client.request.body.multipart.part.MultipartPart;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.xbib.io.http.client.util.MiscUtils.isNonEmpty;

public class MultipartUtils {

    /**
     * The Content-Type for multipart/form-data.
     */
    private static final String MULTIPART_FORM_CONTENT_TYPE = "multipart/form-data";

    /**
     * The pool of ASCII chars to be used for generating a multipart boundary.
     */
    private static byte[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes(US_ASCII);

    /**
     * Creates a new multipart entity containing the given parts.
     *
     * @param parts          the parts to include.
     * @param requestHeaders the request headers
     * @return a MultipartBody
     */
    public static MultipartBody newMultipartBody(List<Part> parts, HttpHeaders requestHeaders) {

        byte[] boundary;
        String contentType;

        String contentTypeHeader = requestHeaders.get(HttpHeaders.Names.CONTENT_TYPE);
        if (isNonEmpty(contentTypeHeader)) {
            int boundaryLocation = contentTypeHeader.indexOf("boundary=");
            if (boundaryLocation != -1) {
                // boundary defined in existing Content-Type
                contentType = contentTypeHeader;
                boundary = (contentTypeHeader.substring(boundaryLocation + "boundary=".length()).trim()).getBytes(US_ASCII);
            } else {
                // generate boundary and append it to existing Content-Type
                boundary = generateBoundary();
                contentType = computeContentType(contentTypeHeader, boundary);
            }
        } else {
            boundary = generateBoundary();
            contentType = computeContentType(MULTIPART_FORM_CONTENT_TYPE, boundary);
        }

        List<MultipartPart<? extends Part>> multipartParts = generateMultipartParts(parts, boundary);

        return new MultipartBody(multipartParts, contentType, boundary);
    }

    public static List<MultipartPart<? extends Part>> generateMultipartParts(List<Part> parts, byte[] boundary) {
        List<MultipartPart<? extends Part>> multipartParts = new ArrayList<MultipartPart<? extends Part>>(parts.size());
        for (Part part : parts) {
            if (part instanceof FilePart) {
                multipartParts.add(new FileMultipartPart((FilePart) part, boundary));

            } else if (part instanceof ByteArrayPart) {
                multipartParts.add(new ByteArrayMultipartPart((ByteArrayPart) part, boundary));

            } else if (part instanceof StringPart) {
                // convert to a byte array
                StringPart stringPart = (StringPart) part;
                byte[] bytes = stringPart.getValue().getBytes(stringPart.getCharset());
                ByteArrayPart byteArrayPart = new ByteArrayPart(//
                        stringPart.getName(),//
                        bytes, //
                        stringPart.getContentType(), //
                        stringPart.getCharset(), //
                        null, //
                        stringPart.getContentId(), //
                        stringPart.getTransferEncoding());
                byteArrayPart.setCustomHeaders(stringPart.getCustomHeaders());
                multipartParts.add(new ByteArrayMultipartPart(byteArrayPart, boundary));

            } else {
                throw new IllegalArgumentException("Unknown part type: " + part);
            }
        }
        // add an extra fake part for terminating the message
        multipartParts.add(new MessageEndMultipartPart(boundary));

        return multipartParts;
    }

    // a random size from 30 to 40
    private static byte[] generateBoundary() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        byte[] bytes = new byte[random.nextInt(11) + 30];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = MULTIPART_CHARS[random.nextInt(MULTIPART_CHARS.length)];
        }
        return bytes;
    }

    private static String computeContentType(String base, byte[] boundary) {
        StringBuilder buffer = new StringBuilder(); // StringUtils.stringBuilder().append(base);
        buffer.append(base);
        if (!base.endsWith(";")) {
            buffer.append(';');
        }
        return buffer.append(" boundary=").append(new String(boundary, US_ASCII)).toString();
    }
}
