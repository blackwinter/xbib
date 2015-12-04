
package org.xbib.graphics.barcode.impl.datamatrix;

/**
 * Constants for DataMatrix.
 * 
 * @version $Id: DataMatrixConstants.java,v 1.4 2007/06/03 08:24:10 jmaerki Exp $
 */
public interface DataMatrixConstants {

    /** Padding character */
    char PAD = 129;
    /** mode latch to C40 encodation mode */
    char LATCH_TO_C40 = 230;
    /** mode latch to Base 256 encodation mode */
    char LATCH_TO_BASE256 = 231;
    /** FNC1 Codeword */
    char FNC1 = 232;
    /** Structured Append Codeword */
    char STRUCTURED_APPEND = 233;
    /** Reader Programming */
    char READER_PROGRAMMING = 234;
    /** Upper Shift */
    char UPPER_SHIFT = 235;
    /** 05 Macro */
    char MACRO_05 = 236;
    /** 06 Macro */
    char MACRO_06 = 237;
    /** mode latch to ANSI X.12 encodation mode */
    char LATCH_TO_ANSIX12 = 238;
    /** mode latch to Text encodation mode */
    char LATCH_TO_TEXT = 239;
    /** mode latch to EDIFACT encodation mode */
    char LATCH_TO_EDIFACT = 240;
    /** ECI character (Extended Channel Interpretation) */
    char ECI = 241;
    
    /** Unlatch from C40 encodation */
    char C40_UNLATCH = 254;
    /** Unlatch from X12 encodation */
    char X12_UNLATCH = 254;
    
    /** 05 Macro header */
    String MACRO_05_HEADER = "[)>\u001E05\u001D";
    /** 06 Macro header */
    String MACRO_06_HEADER = "[)>\u001E06\u001D";
    /** Macro trailer */
    String MACRO_TRAILER = "\u001E\u0004";
}
