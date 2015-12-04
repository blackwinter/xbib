package org.xbib.graphics.barcode.impl.upcean;

import org.xbib.common.settings.Settings;
import org.xbib.graphics.barcode.BarcodeDimension;

/**
 * This class is an implementation of the UPC-E barcode.
 */
public class UPCEGenerator extends UPCEANGenerator {

    public UPCEANLogicImpl createLogicImpl() {
        return new UPCELogicImpl(getChecksumMode());
    }

    @Override
    public void configure(Settings settings) throws Exception {

    }

    /** @see org.xbib.graphics.barcode.impl.upcean.UPCEAN */
    public BarcodeDimension calcDimensions(String msg) {
        double width = 3 * moduleWidth; //left guard
        width += 6 * 7 * moduleWidth;
        width += 6 * moduleWidth; //right guard
        width += supplementalWidth(msg);
        final double qz = (hasQuietZone() ? quietZone : 0);
        return new BarcodeDimension(width, getHeight(), 
                width + (2 * qz), getHeight(), 
                quietZone, 0.0);
    }



}