package org.xbib.graphics.barcode.impl.upcean;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;
import org.xbib.graphics.barcode.BarcodeGenerator;
import org.xbib.graphics.barcode.impl.AbstractBarcodeGenerator;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;

/**
 * This is an abstract base class for UPC and EAN barcodes.
 */
public abstract class UPCEANGenerator extends AbstractBarcodeGenerator {

    /** The default module width for UPC and EAN. */
    protected static final double DEFAULT_MODULE_WIDTH = 0.33f; //mm
    
    private ChecksumMode checksumMode = ChecksumMode.CP_AUTO;

    /** Create a new instance. */
    public UPCEANGenerator() {
        super();
        this.moduleWidth = DEFAULT_MODULE_WIDTH;
        setQuietZone(10 * this.moduleWidth);
        setVerticalQuietZone(0); //1D barcodes don't have vertical quiet zones
    }
    
    /**
     * Sets the checksum mode
     * @param mode the checksum mode
     */
    public void setChecksumMode(ChecksumMode mode) {
        this.checksumMode = mode;
    }

    /**
     * Returns the current checksum mode.
     * @return ChecksumMode the checksum mode
     */
    public ChecksumMode getChecksumMode() {
        return this.checksumMode;
    }

    /**
     * @see AbstractBarcodeGenerator#getBarWidth(int)
     */
    public double getBarWidth(int width) {
        if ((width >= 1) && (width <= 4)) {
            return width * moduleWidth;
        } else {
            throw new IllegalArgumentException("Only widths 1 to 4 allowed");
        }
    }
    
    
    /**
     * Factory method for the logic implementation.
     * @return the newly created logic implementation instance
     */
    public abstract UPCEANLogicImpl createLogicImpl();
       

    /**
     * @see BarcodeGenerator#generateBarcode(CanvasProvider, String)
     */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null) || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }

        ClassicBarcodeLogicHandler handler = new UPCEANCanvasLogicHandler(this, new Canvas(canvas));
        //handler = new LoggingLogicHandlerProxy(handler);

        UPCEANLogicImpl impl = createLogicImpl();
        impl.generateBarcodeLogic(handler, msg);
    }

    /**
     * Calculates the width for the optional supplemental part.
     * @param msg the full message
     * @return the width of the supplemental part
     */
    protected double supplementalWidth(String msg) {
        double width = 0;
        int suppLen = UPCEANLogicImpl.getSupplementalLength(msg);
        if (suppLen > 0) {
            //Supplemental
            width += quietZone;
            width += 4 * moduleWidth; //left guard
            width += suppLen * 7 * moduleWidth;
            width += (suppLen - 1) * 2 * moduleWidth;
        }
        return width;
    }

    /**
     * @see BarcodeGenerator#calcDimensions(String)
     */
    public BarcodeDimension calcDimensions(String msg) {
        double width = 3 * moduleWidth; //left guard
        width += 6 * 7 * moduleWidth;
        width += 5 * moduleWidth; //center guard
        width += 6 * 7 * moduleWidth;
        width += 3 * moduleWidth; //right guard
        width += supplementalWidth(msg);
        final double qz = (hasQuietZone() ? quietZone : 0);
        return new BarcodeDimension(width, getHeight(), 
                width + (2 * qz), getHeight(), 
                quietZone, 0.0);
    }


}