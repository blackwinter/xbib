package org.xbib.graphics.barcode.impl.code128;

/**
 * This interface is implemented by classes that encode a Code128 message into
 * an integer array representing character set indexes.
 */
public interface Code128Encoder {

    /**
     * Encodes a valid Code 128 message to an array of character set indexes.
     * @param msg the message to encode
     * @return the requested array of indexes
     */
    int[] encode(String msg);

}
