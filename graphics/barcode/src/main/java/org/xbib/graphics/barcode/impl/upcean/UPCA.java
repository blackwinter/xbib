
package org.xbib.graphics.barcode.impl.upcean;

/**
 * This class is an implementation of the UPC-A barcode.
 */
public class UPCA extends UPCEAN {

    /** Create a new instance. */
    public UPCA() {
        this.bean = new UPCAGenerator();
    }
    
}