
package org.xbib.graphics.barcode;

import org.xbib.graphics.barcode.output.CanvasProvider;
import org.xbib.common.settings.Settings;

/**
 * This interface is used to generate whole barcodes.
 * 
 */
public interface BarcodeGenerator {

    void configure(Settings settings) throws Exception;

    /**
     * Generates a barcode using the given Canvas to render the barcode to its
     * output format.
     * @param canvas CanvasProvider that the barcode is to be rendered on.
     * @param msg message to encode
     */
    void generateBarcode(CanvasProvider canvas, String msg);

    /**
     * Calculates the dimension of a barcode with the given message. The 
     * dimensions are dependant on the configuration of the barcode generator.
     * @param msg message to use for calculation.
     * @return BarcodeDimension a BarcodeDimension object containing the 
     * barcode's dimensions
     */
    BarcodeDimension calcDimensions(String msg);
}