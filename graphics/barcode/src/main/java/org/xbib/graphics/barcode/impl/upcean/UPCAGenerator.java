
package org.xbib.graphics.barcode.impl.upcean;

import org.xbib.common.settings.Settings;

/**
 * This class is an implementation of the UPC-A barcode.
 */
public class UPCAGenerator extends UPCEANGenerator {

    public UPCEANLogicImpl createLogicImpl() {
        return new UPCALogicImpl(getChecksumMode());
    }

    @Override
    public void configure(Settings settings) throws Exception {

    }
}