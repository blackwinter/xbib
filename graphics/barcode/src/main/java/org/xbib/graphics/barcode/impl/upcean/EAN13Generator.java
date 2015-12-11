
package org.xbib.graphics.barcode.impl.upcean;

import org.xbib.common.settings.Settings;

/**
 * This class implements the EAN13 barcode.
 */
public class EAN13Generator extends UPCEANGenerator {

    /** @see org.xbib.graphics.barcode.impl.upcean.UPCEAN */
    public UPCEANLogicImpl createLogicImpl() {
        return new EAN13LogicImpl(getChecksumMode());
    }

    @Override
    public void configure(Settings settings) throws Exception {

    }
}