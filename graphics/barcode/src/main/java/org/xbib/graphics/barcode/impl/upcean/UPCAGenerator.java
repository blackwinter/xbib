
package org.xbib.graphics.barcode.impl.upcean;

/**
 * This class is an implementation of the UPC-A barcode.
 */
public class UPCAGenerator extends UPCEANGenerator {

    public UPCEANLogicImpl createLogicImpl() {
        return new UPCALogicImpl(getChecksumMode());
    }

}