package org.xbib.graphics.barcode.impl.fourstate;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.HumanReadablePlacement;
import org.xbib.graphics.barcode.TextAlignment;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;
import org.xbib.graphics.barcode.tools.UnitConv;

/**
 * Implements the USPS Intelligent Mail Barcode (Four State Customer Barcode).
 * 
 */
public class USPSIntelligentMailGenerator extends AbstractFourStateGenerator {

    static final double DEFAULT_MODULE_WIDTH_INCH = 0.020; //in
    static final double DEFAULT_INTERCHAR_GAP_WIDTH_INCH = 0.025; //in
    
    static final double DEFAULT_HORZ_QUIET_ZONE_INCH = 0.125; //in
    static final double DEFAULT_VERT_QUIET_ZONE_INCH = 0.028; //in

    static final double DEFAULT_TRACK_HEIGHT_INCH = 0.050; //in
    static final double DEFAULT_ASCENDER_HEIGHT_INCH = 0.050; //in
    
    private Double quietZoneVertical;
    
    /** Create a new instance. */
    public USPSIntelligentMailGenerator() {
        super();
        setMsgPosition(HumanReadablePlacement.HRP_NONE); //Different default than normal
        setModuleWidth(UnitConv.in2mm(DEFAULT_MODULE_WIDTH_INCH)); //0.015 - 0.025in
        setIntercharGapWidth(UnitConv.in2mm(DEFAULT_INTERCHAR_GAP_WIDTH_INCH)); //0.012 - 0.040in
        //Defaults result in a pitch of 0.045in (22.2 bars per inch)
        
        setQuietZone(UnitConv.in2mm(DEFAULT_HORZ_QUIET_ZONE_INCH));
        setVerticalQuietZone(UnitConv.in2mm(DEFAULT_VERT_QUIET_ZONE_INCH));
        
        setTrackHeight(UnitConv.in2mm(DEFAULT_TRACK_HEIGHT_INCH)); //0.039 - 0.057in
        setAscenderHeight(UnitConv.in2mm(DEFAULT_ASCENDER_HEIGHT_INCH)); //0.0435 - 0.0555in
    }

    /** {@inheritDoc} */
    public double getVerticalQuietZone() {
        if (this.quietZoneVertical != null) {
            return this.quietZoneVertical.doubleValue();
        } else {
            return getQuietZone();
        }
    }

    /**
     * Sets the height of the vertical quiet zone. If this value is not explicitely set the
     * vertical quiet zone has the same width as the horizontal quiet zone.
     * @param height the height of the vertical quiet zone (in mm)
     */
    public void setVerticalQuietZone(double height) {
        this.quietZoneVertical = new Double(height);
    }
    
    /** {@inheritDoc} */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null) 
                || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }

        FourStateLogicHandler handler = 
                new FourStateLogicHandler(this, new Canvas(canvas));
        handler.setTextAlignment(TextAlignment.TA_LEFT);

        USPSIntelligentMailLogicImpl impl = new USPSIntelligentMailLogicImpl();
        impl.generateBarcodeLogic(handler, msg);
    }

    /** {@inheritDoc} */
    public BarcodeDimension calcDimensions(String msg) {
        final int barCount = 65;
        final double width = (barCount * getModuleWidth()) 
                + ((barCount - 1) * getIntercharGapWidth());
        final double qzh = (hasQuietZone() ? getQuietZone() : 0);
        final double qzv = (hasQuietZone() ? getVerticalQuietZone() : 0);
        return new BarcodeDimension(width, getHeight(), 
                width + (2 * qzh), getHeight() + (2 * qzv), 
                qzh, qzv);
    }

    /**
     * Verifies whether the current settings of the bean are within the limits given by the
     * USPS Intelligent Mail specification.
     */
    public void verifySettings() {
        if (getBarHeight() < UnitConv.in2mm(0.125)) {
            throw new IllegalArgumentException("Resulting bar height is smaller than 0.125in!");
        }
        if (getBarHeight() > UnitConv.in2mm(0.165)) {
            throw new IllegalArgumentException("Resulting bar height is larger than 0.165in!");
        }

        if (getModuleWidth() < UnitConv.in2mm(0.015)) {
            throw new IllegalArgumentException("Module width is smaller than 0.015in!");
        }
        if (getModuleWidth() > UnitConv.in2mm(0.025)) {
            throw new IllegalArgumentException("Module width is larger than 0.025in!");
        }
        
        if (getIntercharGapWidth() < UnitConv.in2mm(0.012)) {
            throw new IllegalArgumentException("Space between bars is smaller than 0.012in!");
        }
        if (getIntercharGapWidth() > UnitConv.in2mm(0.040)) {
            throw new IllegalArgumentException("Space between bars is larger than 0.040in!");
        }
        
        double pitch = UnitConv.mm2in(getModuleWidth() + getIntercharGapWidth());
        double barsPerInch = 1 / pitch;
        if (barsPerInch < 20) {
            throw new IllegalArgumentException(
                    "Resulting barcode pitch is smaller than 20 bars per inch!");
        }
        if (barsPerInch > 24) {
            throw new IllegalArgumentException(
                    "Resulting barcode pitch is larger than 24 bars per inch");
        }
    }

}