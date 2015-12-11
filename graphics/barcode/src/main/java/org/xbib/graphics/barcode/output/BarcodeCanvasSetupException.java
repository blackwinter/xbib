
package org.xbib.graphics.barcode.output;

import org.xbib.graphics.barcode.BarcodeException;

/**
 * This exception is use during the setup of a barcode canvas.
 */
public class BarcodeCanvasSetupException extends BarcodeException {

    /**
     * Constructor for BarcodeCanvasSetupException.
     * 
     * @param message the detail message for this exception.
     */
    public BarcodeCanvasSetupException(String message) {
        super(message);
    }

}
