package org.xbib.graphics.barcode.impl.codabar;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;
import org.xbib.graphics.barcode.impl.AbstractBarcodeGenerator;
import org.xbib.graphics.barcode.impl.DefaultCanvasLogicHandler;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;
import org.xbib.common.settings.Settings;

/**
 * This class is an implementation of the Codabar barcode.
 */
public class CodabarGenerator extends AbstractBarcodeGenerator {

    /** The default module width for Codabar. */
    protected static final double DEFAULT_MODULE_WIDTH = 0.21f; //mm

    /** The default wide factor for Codabar. */
    protected static final double DEFAULT_WIDE_FACTOR = 3.0;

    /** The default display start/stop value for Codabar. */
    protected static final boolean DEFAULT_DISPLAY_START_STOP = false;

    private ChecksumMode checksumMode = ChecksumMode.CP_AUTO;
    private boolean displayStartStop = DEFAULT_DISPLAY_START_STOP;
    private double wideFactor = DEFAULT_WIDE_FACTOR; //Width of binary one

    /** Create a new instance. */
    public CodabarGenerator() {
        this.moduleWidth = DEFAULT_MODULE_WIDTH;
        setQuietZone(10 * this.moduleWidth);
        setVerticalQuietZone(0); //1D barcodes don't have vertical quiet zones
    }

    @Override
    public void configure(Settings settings) throws Exception {
        // do nothing
    }

    /**
     * Returns the current checksum mode.
     * @return ChecksumMode the checksum mode
     */
    public ChecksumMode getChecksumMode() {
        return this.checksumMode;
    }

    /**
     * Sets the checksum mode
     * @param mode the checksum mode
     */
    public void setChecksumMode(ChecksumMode mode) {
        this.checksumMode = mode;
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

    /** {@inheritDoc} */
    public double getBarWidth(int width) {
        if (width == 1) {
            return moduleWidth;
        } else if (width == 2) {
            return moduleWidth * wideFactor;
        } else {
            throw new IllegalArgumentException("Only widths 1 and 2 allowed");
        }
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
    }


    /** {@inheritDoc} */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null)
                || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }

        ClassicBarcodeLogicHandler handler =
                new DefaultCanvasLogicHandler(this, new Canvas(canvas));

        CodabarLogicImpl impl = new CodabarLogicImpl(getChecksumMode(), isDisplayStartStop());
        impl.generateBarcodeLogic(handler, msg);
    }

    private double calcCharWidth(char c) {
        final int idx = CodabarLogicImpl.getCharIndex(c);
        if (idx >= 0) {
            int narrow = 0;
            int wide = 0;
            for (int i = 0; i < 7; i++) {
                final byte width = CodabarLogicImpl.CHARSET[idx][i];
                if (width == 0) {
                    narrow++;
                } else {
                    wide++;
                }
            }
            return (narrow * moduleWidth) + (wide * moduleWidth * wideFactor);
        } else {
            throw new IllegalArgumentException("Invalid character: " + c);
        }
    }

    public BarcodeDimension calcDimensions(String msg) {
        double width = 0.0;
        for (int i = 0; i < msg.length(); i++) {
            if (i > 0) {
                width += moduleWidth; //Intercharacter gap
            }
            width += calcCharWidth(msg.charAt(i));
        }
        final double qz = (hasQuietZone() ? quietZone : 0);
        return new BarcodeDimension(width, getHeight(),
                width + (2 * qz), getHeight(),
                quietZone, 0.0);
    }

}