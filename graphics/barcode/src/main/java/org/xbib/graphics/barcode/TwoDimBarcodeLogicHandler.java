package org.xbib.graphics.barcode;

/**
 * This interface provides an interface to generate basic 2D barcodes like PDF417 and DataMatrix.
 */
public interface TwoDimBarcodeLogicHandler extends ClassicBarcodeLogicHandler {

    /**
     * Signals the start of a new row in the barcode.
     */
    void startRow();
    
    /**
     * Signals the end of a row in the barcode.
     */
    void endRow();
    
}
