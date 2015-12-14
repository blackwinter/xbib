package org.xbib.io.http.client.request.body.multipart;

import org.xbib.io.http.client.Param;

import java.nio.charset.Charset;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;

public interface Part {

    /**
     * Carriage return/linefeed as a byte array
     */
    byte[] CRLF_BYTES = "\r\n".getBytes(US_ASCII);

    /**
     * Content dispostion as a byte
     */
    byte QUOTE_BYTE = '\"';

    /**
     * Extra characters as a byte array
     */
    byte[] EXTRA_BYTES = "--".getBytes(US_ASCII);

    /**
     * Content dispostion as a byte array
     */
    byte[] CONTENT_DISPOSITION_BYTES = "Content-Disposition: ".getBytes(US_ASCII);

    /**
     * form-data as a byte array
     */
    byte[] FORM_DATA_DISPOSITION_TYPE_BYTES = "form-data".getBytes(US_ASCII);

    /**
     * name as a byte array
     */
    byte[] NAME_BYTES = "; name=".getBytes(US_ASCII);

    /**
     * Content type header as a byte array
     */
    byte[] CONTENT_TYPE_BYTES = "Content-Type: ".getBytes(US_ASCII);

    /**
     * Content charset as a byte array
     */
    byte[] CHARSET_BYTES = "; charset=".getBytes(US_ASCII);

    /**
     * Content type header as a byte array
     */
    byte[] CONTENT_TRANSFER_ENCODING_BYTES = "Content-Transfer-Encoding: ".getBytes(US_ASCII);

    /**
     * Content type header as a byte array
     */
    byte[] CONTENT_ID_BYTES = "Content-ID: ".getBytes(US_ASCII);

    /**
     * Return the name of this part.
     *
     * @return The name.
     */
    String getName();

    /**
     * Returns the content type of this part.
     *
     * @return the content type, or <code>null</code> to exclude the content
     * type header
     */
    String getContentType();

    /**
     * Return the character encoding of this part.
     *
     * @return the character encoding, or <code>null</code> to exclude the
     * character encoding header
     */
    Charset getCharset();

    /**
     * Return the transfer encoding of this part.
     *
     * @return the transfer encoding, or <code>null</code> to exclude the
     * transfer encoding header
     */
    String getTransferEncoding();

    /**
     * Return the content ID of this part.
     *
     * @return the content ID, or <code>null</code> to exclude the content ID
     * header
     */
    String getContentId();

    /**
     * Gets the disposition-type to be used in Content-Disposition header
     *
     * @return the disposition-type
     */
    String getDispositionType();

    List<Param> getCustomHeaders();
}
