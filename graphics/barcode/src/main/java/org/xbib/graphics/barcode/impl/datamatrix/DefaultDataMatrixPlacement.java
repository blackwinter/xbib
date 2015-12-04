
package org.xbib.graphics.barcode.impl.datamatrix;

import java.util.Arrays;

/**
 * Default implementation of DataMatrixPlacement which uses a byte array to store the bits
 * (one bit per byte to allow for checking whether a bit has been set or not).
 */
class DefaultDataMatrixPlacement extends DataMatrixPlacement {
    
    /** Buffer for the bits */
    protected byte[] bits;
    
    /**
     * Main constructor
     * @param codewords the codewords to place
     * @param numcols the number of columns
     * @param numrows the number of rows
     */
    public DefaultDataMatrixPlacement(String codewords, int numcols, int numrows) {
        super(codewords, numcols, numrows);
        this.bits = new byte[numcols * numrows];
        Arrays.fill(this.bits, (byte)-1); //Initialize with "not set" value
    }
    
    /** @see org.xbib.graphics.barcode.impl.datamatrix.DataMatrixPlacement#getBit(int, int) */
    protected boolean getBit(int col, int row) {
        return bits[row * numcols + col] == 1;
    }

    /** @see org.xbib.graphics.barcode.impl.datamatrix.DataMatrixPlacement#setBit(int, int, boolean) */
    protected void setBit(int col, int row, boolean bit) {
        bits[row * numcols + col] = (bit ? (byte)1 : (byte)0);
    }

    /** @see org.xbib.graphics.barcode.impl.datamatrix.DataMatrixPlacement#hasBit(int, int) */
    protected boolean hasBit(int col, int row) {
        return bits[row * numcols + col] >= 0;
    }
    
}