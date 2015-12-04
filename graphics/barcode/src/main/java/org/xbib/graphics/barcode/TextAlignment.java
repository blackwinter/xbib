
package org.xbib.graphics.barcode;

/**
 * Enumeration for horizontal alignment of the human readable part of a barcode.
 * 
 */
public class TextAlignment {

    /** The human-readable part is left-aligned. */
    public static final TextAlignment TA_LEFT = new TextAlignment("left");
    /** The human-readable part is centered. */
    public static final TextAlignment TA_CENTER = new TextAlignment("center");
    /** The human-readable part is right-aligned. */
    public static final TextAlignment TA_RIGHT = new TextAlignment("right");
    /** The human-readable part is justified. */
    public static final TextAlignment TA_JUSTIFY = new TextAlignment("justify");

    private String name;
    
    /**
     * Creates a new TextAlignment instance.
     * @param name the name for the instance
     */
    protected TextAlignment(String name) {
        this.name = name;
    }
    
    /**
     * @return the name of the instance.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns a TextAlignment instance by name.
     * @param name the name of the instance
     * @return the requested instance
     */
    public static TextAlignment byName(String name) {
        if (name.equalsIgnoreCase(TextAlignment.TA_LEFT.getName())) {
            return TextAlignment.TA_LEFT;
        } else if (name.equalsIgnoreCase(TextAlignment.TA_CENTER.getName())) {
            return TextAlignment.TA_CENTER;
        } else if (name.equalsIgnoreCase(TextAlignment.TA_RIGHT.getName())) {
            return TextAlignment.TA_RIGHT;
        } else if (name.equalsIgnoreCase(TextAlignment.TA_JUSTIFY.getName())) {
            return TextAlignment.TA_JUSTIFY;
        } else {
            throw new IllegalArgumentException(
                "Invalid TextAlignment: " + name);
        }
    }
    

}