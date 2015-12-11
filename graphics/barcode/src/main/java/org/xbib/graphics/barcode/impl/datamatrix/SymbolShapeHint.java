package org.xbib.graphics.barcode.impl.datamatrix;

/**
 * Enumeration for DataMatrix symbol shape hint. It can be used to force square or rectangular
 * symbols.
 */
public class SymbolShapeHint {

    /** The human-readable part is suppressed. */
    public static final SymbolShapeHint FORCE_NONE
                                    = new SymbolShapeHint("force-none");
    /** The human-readable part is placed at the top of the barcode. */
    public static final SymbolShapeHint FORCE_SQUARE
                                    = new SymbolShapeHint("force-square");
    /** The human-readable part is placed at the bottom of the barcode. */
    public static final SymbolShapeHint FORCE_RECTANGLE
                                    = new SymbolShapeHint("force-rectangle");

    private String name;
    
    /**
     * Creates a new SymbolShapeHint instance.
     * @param name the name for the instance
     */
    protected SymbolShapeHint(String name) {
        this.name = name;
    }
    
    /**
     * @return the name of the instance.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns a SymbolShapeHint instance by name.
     * @param name the name of the instance
     * @return the requested instance
     */
    public static SymbolShapeHint byName(String name) {
        if (name.equalsIgnoreCase(SymbolShapeHint.FORCE_NONE.getName())) {
            return SymbolShapeHint.FORCE_NONE;
        } else if (name.equalsIgnoreCase(SymbolShapeHint.FORCE_SQUARE.getName())) {
            return SymbolShapeHint.FORCE_SQUARE;
        } else if (name.equalsIgnoreCase(SymbolShapeHint.FORCE_RECTANGLE.getName())) {
            return SymbolShapeHint.FORCE_RECTANGLE;
        } else {
            throw new IllegalArgumentException(
                "Invalid SymbolShapeHint: " + name);
        }
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        return getName();
    }
}