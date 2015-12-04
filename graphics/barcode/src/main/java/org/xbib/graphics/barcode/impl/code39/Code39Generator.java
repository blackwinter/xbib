
package org.xbib.graphics.barcode.impl.code39;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;
import org.xbib.graphics.barcode.impl.AbstractBarcodeGenerator;
import org.xbib.graphics.barcode.impl.DefaultCanvasLogicHandler;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;

/**
 * This class is an implementation of the Code39 barcode.
 */
public class Code39Generator extends AbstractBarcodeGenerator {

    /** The default module width for Code 39. */
    protected static final double DEFAULT_MODULE_WIDTH = 0.19f; //mm

    /** The default wide factor for Code 39 */
    protected static final double DEFAULT_WIDE_FACTOR = 2.5;

    private ChecksumMode checksumMode = ChecksumMode.CP_AUTO;
    private double intercharGapWidth;
    private double wideFactor = DEFAULT_WIDE_FACTOR; //Width of binary one
    private boolean displayStartStop = false;
    private boolean displayChecksum = false;
    private boolean extendedCharSet = false;

    /** Create a new instance. */
    public Code39Generator() {
        this.moduleWidth = DEFAULT_MODULE_WIDTH;
        this.intercharGapWidth = this.moduleWidth;
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
     * Returns the width between encoded characters.
     * @return the interchar gap width
     */
    public double getIntercharGapWidth() {
        return this.intercharGapWidth;
    }

    /**
     * Sets the width between encoded characters.
     * @param width the interchar gap width
     */
    public void setIntercharGapWidth(double width) {
        this.intercharGapWidth = width;
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

    /**
     * Indicates whether the start and stop character will be displayed as
     * part of the human-readable message.
     * @return true if leading and trailing "*" will be displayed
     */
    public boolean isDisplayStartStop() {
        return this.displayStartStop;
    }

    /**
     * Enables or disables the use of the start and stop characters in the
     * human-readable message.
     * @param value true to enable the start/stop character, false to disable
     */
    public void setDisplayStartStop(boolean value) {
        this.displayStartStop = value;
        if (value) {
            //Checksum must also be enabled if start/stop is shown
            setDisplayChecksum(true);
        }
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
        if (isDisplayStartStop() && !value) {
            return; //display-checksum may not be false if start/stop is displayed
        }
        this.displayChecksum = value;
    }

    /**
     * Indicates whether the extended character set is enabled.
     * @return true if the extended character set is enabled
     */
    public boolean isExtendedCharSetEnabled() {
        return this.extendedCharSet;
    }

    /**
     * Enables or disables the extended character set. The extended character set enables
     * the whole ASCII 7-bit character set for Code39.
     * @param value true to enable the extended character set
     */
    public void setExtendedCharSetEnabled(boolean value) {
        this.extendedCharSet = value;
    }

    /** {@inheritDoc} */
    public double getBarWidth(int width) {
        if (width == 1) {
            return moduleWidth;
        } else if (width == 2) {
            return moduleWidth * wideFactor;
        } else if (width == -1) {
            return this.intercharGapWidth;
        } else {
            throw new IllegalArgumentException("Only widths 1 and 2 allowed");
        }
    }

    /** {@inheritDoc} */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null)
                || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }

        ClassicBarcodeLogicHandler handler =
                new DefaultCanvasLogicHandler(this, new Canvas(canvas));

        Code39LogicImpl impl = createLogicImpl();
        impl.generateBarcodeLogic(handler, msg);
    }

    private Code39LogicImpl createLogicImpl() {
        return new Code39LogicImpl(getChecksumMode(),
                isDisplayStartStop(), isDisplayChecksum(), isExtendedCharSetEnabled());
    }

    /** {@inheritDoc} */
    public BarcodeDimension calcDimensions(String msg) {
        Code39LogicImpl impl = createLogicImpl();
        int msglen = impl.prepareMessage(msg).length();
        final double width = ((msglen + 2) * (3 * wideFactor + 6) * moduleWidth)
                + ((msglen + 1) * intercharGapWidth);
        final double qz = (hasQuietZone() ? quietZone : 0);
        return new BarcodeDimension(width, getHeight(),
                width + (2 * qz), getHeight(),
                quietZone, 0.0);
    }

}