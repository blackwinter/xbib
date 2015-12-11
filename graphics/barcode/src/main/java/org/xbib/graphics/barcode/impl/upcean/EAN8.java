
package org.xbib.graphics.barcode.impl.upcean;

/**
 * This class implements the EAN-8 barcode.
 * 
 */
public class EAN8 extends UPCEAN {

    /** Create a new instance. */
    public EAN8() {
        this.bean = new EAN8Generator();
    }

}