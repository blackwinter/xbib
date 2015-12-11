
package org.xbib.graphics.barcode.impl.fourstate;

import org.xbib.graphics.barcode.BarGroup;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;

/**
 * Provides a base class for "four-state" barcodes.
 */
public abstract class AbstractFourStateLogicImpl {


    private ChecksumMode checksumMode = ChecksumMode.CP_AUTO;

    /**
     * Main constructor
     * @param mode checksum mode
     */
    public AbstractFourStateLogicImpl(ChecksumMode mode) {
        this.checksumMode = mode;
    }

    /**
     * Returns the currently active checksum mode.
     * @return the checksum mode
     */
    public ChecksumMode getChecksumMode() {
        return this.checksumMode;
    }

    /**
     * Calculates the checksum for a message to be encoded as an 
     * one of the foru-state barcode symbologies.
     * @param msg message to calculate the check digit for
     * @return char the check digit
     */
    public abstract char calcChecksum(String msg);


    /**
     * Verifies the checksum for a message.
     * @param msg message (check digit included)
     * @return boolean True, if the checksum is correct
     */
    public boolean validateChecksum(String msg) {
        char actual = msg.charAt(msg.length() - 1);
        char expected = calcChecksum(msg.substring(0, msg.length() - 1));
        return (actual == expected);
    }

    /**
     * Checks if a character is an ignored character (such as a '-' (dash)).
     * @param c character to check
     * @return True if the character is ignored
     */
    public static boolean isIgnoredChar(char c) {
        return false;
    }
    
    /**
     * Turns the given message into a normalize representation. Some subclasses may update/add
     * parentheses around the message and/or handle the checksum as necessary.
     * @param msg the message
     * @return the normalized message to be encoded
     */
    protected abstract String normalizeMessage(String msg);
    
    /**
     * Does the high-level encoding of the message into codewords.
     * @param msg the message
     * @return an array of Strings with codewords
     */
    protected abstract String[] encodeHighLevel(String msg);
    
    /**
     * Encodes a single character.
     * @param logic the logic handler to receive generated events
     * @param c the character to encode
     * @param codeword the codeword belonging to the character
     */
    protected void encodeCodeword(ClassicBarcodeLogicHandler logic, char c, String codeword) {
        logic.startBarGroup(BarGroup.MSG_CHARACTER, new Character(c).toString());
        for (int i = 0, count = codeword.length(); i < count; i++) {
            int height = Integer.parseInt(codeword.substring(i, i + 1));
            logic.addBar(true, height);
        }
        logic.endBarGroup();
    }

    /**
     * Generates the barcode logic
     * @param logic the logic handler to receive generated events
     * @param msg the message to encode
     */
    public void generateBarcodeLogic(ClassicBarcodeLogicHandler logic, String msg) {
        String normalizedMsg = normalizeMessage(msg);
        String[] encodedMsg = encodeHighLevel(normalizedMsg);

        logic.startBarcode(msg, normalizedMsg);
        
        // encode message
        for (int i = 0; i < encodedMsg.length; i++) {
            final char ch = normalizedMsg.charAt(i);
            encodeCodeword(logic, ch, encodedMsg[i]);
        }

        logic.endBarcode();
    }

}
