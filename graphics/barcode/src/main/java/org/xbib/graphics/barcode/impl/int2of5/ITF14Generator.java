package org.xbib.graphics.barcode.impl.int2of5;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;

/**
 * This class is an implementation of ITF-14 (as defined by the
 * <a href="http://www.gs1.org">GS1 standards organization</a>).
 * ITF-14 is basically an Interleaved 2 of 5 barcode with an added, so-called bearer bar.
 *
 */
public class ITF14Generator extends Interleaved2Of5Generator {

    static final double DEFAULT_MODULE_WIDTH = 1.016; //mm (0.04in)
    static final double DEFAULT_WIDE_FACTORY = 2.5;
    static final double DEFAULT_BAR_HEIGHT = 32; //mm (1.25in)
    static final double DEFAULT_BEARER_BAR_WIDTH = 4.8; //mm (0.19in)
    static final boolean DEFAULT_BEARER_BOX = true;

    private double bearerBarWidth;
    private boolean bearerBox;

    /**
     * Default constructor.
     */
    public ITF14Generator() {
        super();
        setModuleWidth(DEFAULT_MODULE_WIDTH);
        setQuietZone(10 * this.moduleWidth);
        doQuietZone(true);
        setWideFactor(DEFAULT_WIDE_FACTORY);
        setBarHeight(DEFAULT_BAR_HEIGHT);
        setBearerBarWidth(DEFAULT_BEARER_BAR_WIDTH);
        setBearerBox(DEFAULT_BEARER_BOX);
        setFontSize(3 * getBarWidth(2));
        setDisplayChecksum(true);
    }

    /**
     * Indicates whether a bearer box is generated or just horizontal bearer bars.
     * @return true if a bearer box is generated, false if horizontal bearer bars are generated.
     */
    public boolean isBearerBox() {
        return this.bearerBox;
    }

    /**
     * Controls whether a bearer box is generated or just horizontal bearer bars.
     * @param value true for a bearer box, false for horizontal bearer bars.
     */
    public void setBearerBox(boolean value) {
        this.bearerBox = value;
    }

    /**
     * Returns the bearer bar width.
     * @return the bearer bar width (in millimeters)
     */
    public double getBearerBarWidth() {
        return this.bearerBarWidth;
    }

    /**
     * Sets the bearer bar width. The nominal value is 4.8mm (0.19in).
     * @param width the bearer bar width (in millimeters)
     */
    public void setBearerBarWidth(double width) {
        this.bearerBarWidth = width;
    }

    /** {@inheritDoc} */
    public void doQuietZone(boolean value) {
        if (!value) {
            throw new IllegalArgumentException("Quiet zone may not be disabled for ITF-14!");
        }
        super.doQuietZone(value);
    }

    /**
     * Validates the barcode bean's settings.
     */
    protected void validate() {
        if (getQuietZone() < 10 * getModuleWidth()) {
            throw new IllegalStateException(
                    "Quiet zone must be at least 10 times the module width!");
        }
    }

    /** {@inheritDoc} */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null)
                || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }
        validate();

        ClassicBarcodeLogicHandler handler =
                new ITF14CanvasLogicHandler(this, new Canvas(canvas));

        ITF14LogicImpl impl = new ITF14LogicImpl(
                getChecksumMode(), isDisplayChecksum());
        impl.generateBarcodeLogic(handler, msg);
    }

    /** {@inheritDoc} */
    public BarcodeDimension calcDimensions(String msg) {
        int msgLen = msg.length();
        if (getChecksumMode() == ChecksumMode.CP_ADD) {
            msgLen++;
        }
        if ((msgLen % 2) != 0) {
            msgLen++; //Compensate for odd number of characters
        }
        final double charwidth = 2 * getWideFactor() + 3;
        final double width = ((msgLen * charwidth) + 6 + getWideFactor()) * getModuleWidth();
        final double qz = getQuietZone();
        final double vBearerBar = (isBearerBox() ? getBearerBarWidth() : 0.0);
        return new BarcodeDimension(width, getHeight(),
                width + (2 * qz) + (2 * vBearerBar), getHeight() + (2 * getBearerBarWidth()),
                vBearerBar + getQuietZone(), getBearerBarWidth());
    }
}
