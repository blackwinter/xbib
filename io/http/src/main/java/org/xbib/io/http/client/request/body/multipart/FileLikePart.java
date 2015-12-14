package org.xbib.io.http.client.request.body.multipart;

import java.nio.charset.Charset;

/**
 * This class is an adaptation of the Apache HttpClient implementation
 */
public abstract class FileLikePart extends PartBase {

    /**
     * Default content encoding of file attachments.
     */
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /**
     * Default transfer encoding of file attachments.
     */
    public static final String DEFAULT_TRANSFER_ENCODING = "binary";

    private String fileName;

    /**
     * FilePart Constructor.
     *
     * @param name              the name for this part
     * @param contentType       the content type for this part, if <code>null</code> the {@link #DEFAULT_CONTENT_TYPE
     *                          default} is used
     * @param charset           the charset encoding for this part
     * @param contentId         the content id
     * @param transfertEncoding the transfer encoding
     */
    public FileLikePart(String name, String contentType, Charset charset, String contentId, String transfertEncoding) {
        super(name,//
                contentType == null ? DEFAULT_CONTENT_TYPE : contentType,//
                charset,//
                contentId,//
                transfertEncoding == null ? DEFAULT_TRANSFER_ENCODING : transfertEncoding);
    }

    public String getFileName() {
        return fileName;
    }

    public final void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return new StringBuilder()//
                .append(super.toString())//
                .append(" filename=").append(fileName)//
                .toString();
    }
}
