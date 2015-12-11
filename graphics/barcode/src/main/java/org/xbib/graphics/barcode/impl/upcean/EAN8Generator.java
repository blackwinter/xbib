
package org.xbib.graphics.barcode.impl.upcean;

import org.xbib.common.settings.Settings;
import org.xbib.graphics.barcode.BarcodeDimension;

/**
 * This class implements the EAN-8 barcode.
 * 
 */
public class EAN8Generator extends UPCEANGenerator {

    /** @see org.xbib.graphics.barcode.impl.upcean.UPCEAN */
    public UPCEANLogicImpl createLogicImpl() {
        return new EAN8LogicImpl(getChecksumMode());
    }

    @Override
    public void configure(Settings settings) throws Exception {

    }

    /** @see org.xbib.graphics.barcode.impl.upcean.UPCEAN */
    public BarcodeDimension calcDimensions(String msg) {
        double width = 3 * moduleWidth; //left guard
        width += 4 * 7 * moduleWidth;
        width += 5 * moduleWidth; //center guard
        width += 4 * 7 * moduleWidth;
        width += 3 * moduleWidth; //right guard
        width += supplementalWidth(msg);
        final double qz = (hasQuietZone() ? quietZone : 0);
        return new BarcodeDimension(width, getHeight(), 
                width + (2 * qz), getHeight(), 
                quietZone, 0.0);
    }

}