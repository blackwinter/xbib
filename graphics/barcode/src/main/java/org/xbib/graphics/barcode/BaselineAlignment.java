package org.xbib.graphics.barcode;

/**
 * Enumeration for the alignment of bars when the heights are not uniform.
 * 
 */
public class BaselineAlignment {

    /** The bars are aligned to be even along the top. */
    public static final BaselineAlignment ALIGN_TOP = new BaselineAlignment("top");
    /** The bars are aligned to be even along the bottom. */
    public static final BaselineAlignment ALIGN_BOTTOM = new BaselineAlignment("bottom");

    private String name;
    
    /**
     * Creates a new BaselineAlignment instance.
     * @param name the name for the instance
     */
    protected BaselineAlignment(String name) {
        this.name = name;
    }

    /**
     * @return the name of the instance.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns a BaselineAlignment instance by name.
     * @param name the name of the instance
     * @return the requested instance
     */
    public static BaselineAlignment byName(String name) {
        if (name.equalsIgnoreCase(BaselineAlignment.ALIGN_TOP.getName())) {
            return BaselineAlignment.ALIGN_TOP;
        } else if (name.equalsIgnoreCase(BaselineAlignment.ALIGN_BOTTOM.getName())) {
            return BaselineAlignment.ALIGN_BOTTOM;
        } else {
            throw new IllegalArgumentException(
                "Invalid BaselineAlignment: " + name);
        }
    }
    



}
