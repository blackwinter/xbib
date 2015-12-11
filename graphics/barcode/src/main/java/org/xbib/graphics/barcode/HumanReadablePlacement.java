package org.xbib.graphics.barcode;

/**
 * Enumeration for placement of the human readable part of a barcode.
 * 
 */
public class HumanReadablePlacement {

    /** The human-readable part is suppressed. */
    public static final HumanReadablePlacement HRP_NONE
                                    = new HumanReadablePlacement("none");
    /** The human-readable part is placed at the top of the barcode. */
    public static final HumanReadablePlacement HRP_TOP
                                    = new HumanReadablePlacement("top");
    /** The human-readable part is placed at the bottom of the barcode. */
    public static final HumanReadablePlacement HRP_BOTTOM
                                    = new HumanReadablePlacement("bottom");

    private String name;
    
    /**
     * Creates a new HumanReadablePlacement instance.
     * @param name the name for the instance
     */
    protected HumanReadablePlacement(String name) {
        this.name = name;
    }
    
    /**
     * @return the name of the instance.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns a HumanReadablePlacement instance by name.
     * @param name the name of the instance
     * @return the requested instance
     */
    public static HumanReadablePlacement byName(String name) {
        if (name.equalsIgnoreCase(HumanReadablePlacement.HRP_NONE.getName())) {
            return HumanReadablePlacement.HRP_NONE;
        } else if (name.equalsIgnoreCase(HumanReadablePlacement.HRP_TOP.getName())) {
            return HumanReadablePlacement.HRP_TOP;
        } else if (name.equalsIgnoreCase(HumanReadablePlacement.HRP_BOTTOM.getName())) {
            return HumanReadablePlacement.HRP_BOTTOM;
        } else {
            throw new IllegalArgumentException(
                "Invalid HumanReadablePlacement: " + name);
        }
    }
    

}