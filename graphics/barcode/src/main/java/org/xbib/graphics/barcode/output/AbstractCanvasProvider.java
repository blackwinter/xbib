
package org.xbib.graphics.barcode.output;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.TextAlignment;

/**
 * Abstract base class for most CanvasProvider implementations.
 */
public abstract class AbstractCanvasProvider implements CanvasProvider {

    /** the cached barcode dimensions */
    protected BarcodeDimension bardim;

    /** the barcode orientation (0, 90, 180, 270) */
    private int orientation;
    
    /**
     * Main constructor.
     * @param orientation the orientation of the barcode
     */
    public AbstractCanvasProvider(int orientation) {
        this.orientation = BarcodeDimension.normalizeOrientation(orientation);
    }
    
    /** {@inheritDoc} */
    public void establishDimensions(BarcodeDimension dim) {
        this.bardim = dim;
    }

    /** {@inheritDoc} */
    public BarcodeDimension getDimensions() {
        return this.bardim;
    }
    
    /** {@inheritDoc} */
    public int getOrientation() {
        return this.orientation;
    }

    /** {@inheritDoc} */
    public void deviceJustifiedText(String text,
            double x1, double x2, double y1,
            String fontName, double fontSize) {
        deviceText(text, x1, x2, y1, fontName, fontSize, TextAlignment.TA_JUSTIFY);
    }

    /** {@inheritDoc} */
    public void deviceCenteredText(String text,
            double x1, double x2, double y1,
            String fontName, double fontSize) {
        deviceText(text, x1, x2, y1, fontName, fontSize, TextAlignment.TA_CENTER);
    }

}
