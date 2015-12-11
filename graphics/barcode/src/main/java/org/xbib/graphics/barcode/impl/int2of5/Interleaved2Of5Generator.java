package org.xbib.graphics.barcode.impl.int2of5;

import org.xbib.common.settings.Settings;
import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;
import org.xbib.graphics.barcode.BarcodeGenerator;
import org.xbib.graphics.barcode.impl.AbstractBarcodeGenerator;
import org.xbib.graphics.barcode.impl.DefaultCanvasLogicHandler;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;

/**
 * This class is an implementation of the Interleaved 2 of 5 barcode.
 */
public class Interleaved2Of5Generator extends AbstractBarcodeGenerator {

    /** The default module width for Interleaved 2 of 5. */
    protected static final double DEFAULT_MODULE_WIDTH = 0.21f; //mm

    /** The default wide factor for Interleaved 2 of 5. */
    public static final double DEFAULT_WIDE_FACTOR = 3.0;

    private ChecksumMode checksumMode = ChecksumMode.CP_AUTO;
    private double wideFactor = DEFAULT_WIDE_FACTOR; //Determines the width of wide bar
    private boolean displayChecksum = false;

    /** Create a new instance. */
    public Interleaved2Of5Generator() {
        this.moduleWidth = DEFAULT_MODULE_WIDTH;
        setQuietZone(10 * this.moduleWidth);
        setVerticalQuietZone(0); //1D barcodes don't have vertical quiet zones
    }
    
    /**
     * Indicates whether the checksum will be displayed as
     * part of the human-readable message.
     * @return true if checksum will be included in the human-readable message
     */
    public boolean isDisplayChecksum() {
        return this.displayChecksum;
    }
    
    /**
     * Enables or disables the use of the checksum in the
     * human-readable message.
     * @param value true to include the checksum in the human-readable message, 
     *   false to ignore
     */
    public void setDisplayChecksum(boolean value) {
        this.displayChecksum = value;
    }

    @Override
    public void configure(Settings settings) throws Exception {

    }

    /**
     * @see BarcodeGenerator#generateBarcode(CanvasProvider, String)
     */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null) 
                || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }

        ClassicBarcodeLogicHandler handler = 
                new DefaultCanvasLogicHandler(this, new Canvas(canvas));
        //handler = new LoggingLogicHandlerProxy(handler);

        Interleaved2Of5LogicImpl impl = new Interleaved2Of5LogicImpl(
                getChecksumMode(), isDisplayChecksum());
        impl.generateBarcodeLogic(handler, msg);
    }
    
    /**
     * @see BarcodeGenerator#calcDimensions(String)
     */
    public BarcodeDimension calcDimensions(String msg) {
        int msgLen = msg.length();
        if (getChecksumMode() == ChecksumMode.CP_ADD) {
            msgLen++;
        }
        if ((msgLen % 2) != 0) {
            msgLen++; //Compensate for odd number of characters
        }
        final double charwidth = 2 * wideFactor + 3;
        final double width = ((msgLen * charwidth) + 6 + wideFactor) * moduleWidth;
        final double qz = (hasQuietZone() ? quietZone : 0);
        return new BarcodeDimension(width, getHeight(), 
                width + (2 * qz), getHeight(), 
                quietZone, 0.0);
    }

    /**
     * @see AbstractBarcodeGenerator#getBarWidth(int)
     */
    public double getBarWidth(int width) {
        if (width == 1) {
            return moduleWidth;
        } else if (width == 2) {
            return moduleWidth * wideFactor;
        } else throw new IllegalArgumentException("Only widths 1 and 2 allowed");
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
     * Returns the factor by which wide bars are broader than narrow bars.
     * @return the wide factor
     */
    public double getWideFactor() {
        return this.wideFactor;
    }
    
    /**
     * Sets the factor by which wide bars are broader than narrow bars.
     * @param value the wide factory (should be > 1.0)
     */
    public void setWideFactor(double value) {
        if (value <= 1.0) {
            throw new IllegalArgumentException("wide factor must be > 1.0");
        }
        this.wideFactor = value;
    }
    
}