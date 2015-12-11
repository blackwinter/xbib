package org.xbib.graphics.barcode;

/**
 * Base exception class for Barcodes.
 */
public class BarcodeException extends Exception {

    /**
     * Constructor for BarcodeException.
     * 
     * @param message the detail message for this exception.
     */
    public BarcodeException(String message) {
        super(message);
    }

    public BarcodeException(String message, Throwable t) {
        super(message, t);
    }
}
