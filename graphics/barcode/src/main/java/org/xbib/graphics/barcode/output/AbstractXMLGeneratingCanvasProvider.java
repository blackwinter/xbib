
package org.xbib.graphics.barcode.output;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Abstract base class that provides some commonly used methods for 
 * generating XML representations of barcodes.
 * 
 */
public abstract class AbstractXMLGeneratingCanvasProvider
        extends AbstractCanvasProvider {

    private DecimalFormat df;

    public AbstractXMLGeneratingCanvasProvider(int orientation) {
        super(orientation);
    }
    
    /**
     * Returns the DecimalFormat instance to use internally to format numbers.
     * @return a DecimalFormat instance
     */
    protected DecimalFormat getDecimalFormat() {
        if (this.df == null) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            this.df = new DecimalFormat("0.####", dfs);
        }
        return this.df;
    }

    /**
     * Formats a value and adds the unit specifier at the end.
     * @param value the value to format
     * @return the formatted value
     */
    protected String addUnit(double value) {
        return getDecimalFormat().format(value) + "mm"; //was mm
    }

}
