
package org.xbib.graphics.barcode.impl.upcean;

/**
 * This class implements the EAN13 barcode.
 */
public class EAN13 extends UPCEAN {

    /** Create a new instance. */
    public EAN13() {
        this.bean = new EAN13Generator();
    }

}