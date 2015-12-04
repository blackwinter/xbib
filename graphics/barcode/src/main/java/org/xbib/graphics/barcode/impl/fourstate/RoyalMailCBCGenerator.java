
package org.xbib.graphics.barcode.impl.fourstate;

import org.xbib.common.settings.Settings;
import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.HumanReadablePlacement;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;

/**
 * Implements the Royal Mail Customer Barcode.
 */
public class RoyalMailCBCGenerator extends AbstractFourStateGenerator {

    /** The default module width for RoyalMail. */
    protected static final double DEFAULT_MODULE_WIDTH = 0.53; //mm

    /** Create a new instance. */
    public RoyalMailCBCGenerator() {
        super();
        this.msgPos = HumanReadablePlacement.HRP_NONE; //Different default than normal
        setModuleWidth(DEFAULT_MODULE_WIDTH);
        setTrackHeight(1.25f); //mm
        setAscenderHeight(1.8f); //mm
        setQuietZone(2.0); //mm
        setIntercharGapWidth(getModuleWidth());
        updateHeight();
    }
    
    /** {@inheritDoc} */
    public void setMsgPosition(HumanReadablePlacement placement) {
        //nop, no human-readable with this symbology!!!
    }

    @Override
    public void configure(Settings settings) throws Exception {

    }

    /** {@inheritDoc} */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null) 
                || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }

        FourStateLogicHandler handler = 
                new FourStateLogicHandler(this, new Canvas(canvas));

        RoyalMailCBCLogicImpl impl = new RoyalMailCBCLogicImpl(
                getChecksumMode());
        impl.generateBarcodeLogic(handler, msg);
    }

    /** {@inheritDoc} */
    public BarcodeDimension calcDimensions(String msg) {
        String modMsg = RoyalMailCBCLogicImpl.removeStartStop(msg);
        int additional = (getChecksumMode() == ChecksumMode.CP_ADD 
                || getChecksumMode() == ChecksumMode.CP_AUTO) ? 1 : 0;
        final int len = modMsg.length() + additional;
        final double width = (((len * 4) + 2) * moduleWidth) 
                + (((len * 4) + 1) * getIntercharGapWidth());
        final double qzh = (hasQuietZone() ? getQuietZone() : 0);        
        final double qzv = (hasQuietZone() ? getVerticalQuietZone() : 0);        
        return new BarcodeDimension(width, getBarHeight(), 
                width + (2 * qzh), getBarHeight() + (2 * qzv), 
                qzh, qzv);
    }

}