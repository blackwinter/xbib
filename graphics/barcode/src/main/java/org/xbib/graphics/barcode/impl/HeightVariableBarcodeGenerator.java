
package org.xbib.graphics.barcode.impl;

/**
 * Base class for barcodes that encode information by varying the height
 * of the bars.
 */
public abstract class HeightVariableBarcodeGenerator extends AbstractBarcodeGenerator {

    /**
     * Returns the effective height of a bar with a given logical height.
     * @param height the logical height (1=short, 2=tall)
     * @return double
     */
    public abstract double getBarHeight(int height);

}
