package org.xbib.graphics.barcode.impl.fourstate;

import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.HumanReadablePlacement;
import org.xbib.graphics.barcode.impl.HeightVariableBarcodeGenerator;

/**
 * Abstract base class for four state barcode beans.
 */
public abstract class AbstractFourStateGenerator extends HeightVariableBarcodeGenerator {

    private ChecksumMode checksumMode = ChecksumMode.CP_AUTO;

    private double intercharGapWidth;
    private double trackHeight;
    private double ascenderHeight;
    
    /** Create a new instance. */
    public AbstractFourStateGenerator() {
        super();
        this.msgPos = HumanReadablePlacement.HRP_NONE; //Different default than normal
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

    /** @return the height of the vertical quiet zone (in mm) */
    public double getVerticalQuietZone() {
        return getQuietZone(); //Same as horizontal
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
     * Returns the height of the track.
     * @return the height of the track
     */
    public double getTrackHeight() {
        return this.trackHeight;
    }
    
    /**
     * Sets the height of the track.
     * @param height the height of the track
     */
    public void setTrackHeight(double height) {
        this.trackHeight = height;
        updateHeight();
    }
    
    /**
     * Returns the height of the ascender/descender.
     * @return the height of the ascender/descender
     */
    public double getAscenderHeight() {
        return this.ascenderHeight;
    }
    
    /**
     * Sets the height of the ascender/descender.
     * @param height the height of the ascender/descender
     */
    public void setAscenderHeight(double height) {
        this.ascenderHeight = height;
        updateHeight();
    }
    
    /**
     * Updates the height variable of the barcode.
     */
    protected void updateHeight() {
        setBarHeight(getTrackHeight() + (2 * getAscenderHeight()));
    }
    
    /** {@inheritDoc} */
    public double getBarWidth(int width) {
        if (width == 1) {
            return moduleWidth;
        } else if (width == -1) {
            return this.intercharGapWidth;
        } else {
            throw new IllegalArgumentException("Only width 1 allowed");
        }
    }
    
    /** {@inheritDoc} */
    public double getBarHeight(int height) {
        switch (height) {
        case 0: return trackHeight;
        case 1: return trackHeight + ascenderHeight;
        case 2: return trackHeight + ascenderHeight;
        case 3: return trackHeight + (2 * ascenderHeight);
        default: throw new IllegalArgumentException("Only height 0-3 allowed");
        }
    }
    
}