package org.xbib.graphics.barcode.impl.code128;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;
import org.xbib.graphics.barcode.BarcodeGenerator;
import org.xbib.graphics.barcode.impl.DefaultCanvasLogicHandler;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;

/**
 * This class is an implementation of the Code 128 barcode.
 * 
 */
public class EAN128Generator extends Code128Generator {

    /** Defines the default group separator character */
    public static final char DEFAULT_GROUP_SEPARATOR = '\u001D'; //ASCII: GS
    /** Defines the default character for the check digit marker */
    public static final char DEFAULT_CHECK_DIGIT_MARKER = '\u00F0';

    private EAN128LogicImpl impl;

    private ChecksumMode checksumMode = ChecksumMode.CP_AUTO;
    private String template = null;
    private char groupSeparator = DEFAULT_GROUP_SEPARATOR; //GroupSeperator not Code128LogicImpl.FNC_1; 
    private char checkDigitMarker = DEFAULT_CHECK_DIGIT_MARKER; 
    private boolean omitBrackets = false;

    /** Create a new instance. */
    public EAN128Generator() {
        super();
        impl = new EAN128LogicImpl(checksumMode, template, groupSeparator);
    }
    
    /**
     * @see BarcodeGenerator#calcDimensions(String)
     */
    public BarcodeDimension calcDimensions(String msg) {
        int msgLen = impl.getEncodedMessage(msg).length + 1; 
        //TODO If the output is able to calculate text lenghts (e.g. awt, fop), and 
        //the human readable part is longer then barcode the size should be enlarged!
        final double width = ((msgLen * 11) + 13) * getModuleWidth();
        final double qz = (hasQuietZone() ? quietZone : 0);
        return new BarcodeDimension(width, getHeight(), 
                width + (2 * qz), getHeight(), 
                quietZone, 0.0);
    }

    /**
     * @see BarcodeGenerator#generateBarcode(CanvasProvider, String)
     */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null) || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }

        ClassicBarcodeLogicHandler handler = 
                new DefaultCanvasLogicHandler(this, new Canvas(canvas));
        //handler = new LoggingLogicHandlerProxy(handler);
        
        impl.generateBarcodeLogic(handler, msg);
    }
    
    /**
     * Sets the checksum mode
     * @param mode the checksum mode
     */
    public void setChecksumMode(ChecksumMode mode) {
        this.checksumMode = mode;
        impl.setChecksumMode(mode);
    }

    /**
     * Returns the current checksum mode.
     * @return ChecksumMode the checksum mode
     */
    public ChecksumMode getChecksumMode() {
        return this.checksumMode;
    }


    /**
     * @return the group separator character
     */
    public char getGroupSeparator() {
        return groupSeparator;
    }

    /**
     * Sets the group separator character. Normally, either ASCII GS or 0xF1 is used.
     * @param c the group separator character.
     */
    public void setGroupSeparator(char c) {
        groupSeparator = c;
        impl.setGroupSeparator(c);
    }

    /**
     * @return the message template with the fields for the EAN message
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the message template with the fields for the EAN message.
     * <p>
     * The format of the templates here is a repeating set of AI number (in brackets)
     * followed by a field description. The allowed data types are "n" (numeric), 
     * "an" (alpha-numeric), "d" (date) and "cd" (check digit). Examples: "n13" defines a numeric
     * field with exactly 13 digits. "n13+cd" defines a numeric field with exactly 13 digits plus
     * a check digit. "an1-9" defines an alpha-numeric field with 1 to 9 characters.
     * @param string a template like "(01)n13+cd(421)n3+an1-9(10)an1-20"
     */
    public void setTemplate(String string) {
        template = string;
        impl.setTemplate(string);
    }

    /**
     * @return the character used as the check digit marker.
     */
    public char getCheckDigitMarker() {
        return checkDigitMarker;
    }

    /**
     * Sets the character that will be used as the check digit marker.
     * @param c the character for the check digit marker
     */
    public void setCheckDigitMarker(char c) {
        checkDigitMarker = c;
        impl.setCheckDigitMarker(c); 
    }
    
    /**
     * @return true if the brackets in the human-readable part should be omitted
     */
    public boolean isOmitBrackets() {
        return omitBrackets;
    }

    /**
     * Indicates whether brackets should be used in the human-readable part around the AIs.
     * @param b true if the brackets in the human-readable part should be omitted
     */
    public void setOmitBrackets(boolean b) {
        omitBrackets = b;
        impl.setOmitBrackets(b);
    }
}