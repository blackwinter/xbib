
package org.xbib.graphics.barcode.impl.code128;

/**
 * Constants for Code128.
 */
public interface Code128Constants {

    /** Enables the codeset A */
    int CODESET_A = 1;
    /** Enables the codeset B */
    int CODESET_B = 2;
    /** Enables the codeset C */
    int CODESET_C = 4;
    /** Enables all codesets */
    int CODESET_ALL = CODESET_A | CODESET_B | CODESET_C;

}
