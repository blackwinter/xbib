
package org.xbib.graphics.barcode;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration type for bar groups.
 */
public class BarGroup {

    private static final Map<String, BarGroup> MAP = new HashMap<>();

    /** Bar group is represents a start character */
    public static final BarGroup START_CHARACTER = new BarGroup("start-char", MAP);
    /** Bar group is represents a stop character */
    public static final BarGroup STOP_CHARACTER  = new BarGroup("stop-char", MAP);
    /** Bar group is represents a message character or a part of the message */
    public static final BarGroup MSG_CHARACTER   = new BarGroup("msg-char", MAP);
    /** Bar group is represents a UPC/EAN guard */
    public static final BarGroup UPC_EAN_GUARD   = new BarGroup("upc-ean-guard", MAP);
    /** Bar group is represents a UPC/EAN lead */
    public static final BarGroup UPC_EAN_LEAD    = new BarGroup("upc-ean-lead", MAP);
    /** Bar group is represents a UPC/EAN character group */
    public static final BarGroup UPC_EAN_GROUP   = new BarGroup("upc-ean-group", MAP);
    /** Bar group is represents a UPC/EAN check character */
    public static final BarGroup UPC_EAN_CHECK   = new BarGroup("upc-ean-check", MAP);
    /** Bar group is represents a UPC/EAN supplemental */
    public static final BarGroup UPC_EAN_SUPP    = new BarGroup("upc-ean-supp", MAP);

    private String name;
    
    /**
     * Creates a new BarGroup instance.
     * @param name name of the BarGroup
     * @param map Map to register the instance in.
     */
    protected BarGroup(String name, final Map map) {
        this.name = name;
        MAP.put(name, this);
    }
    
    /**
     * @return the name of the instance.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns a BarGroup instance by name.
     * @param name the name of the desired BarGroup
     * @return the requested BarGroup instance
     */
    public static BarGroup byName(String name) {
        final BarGroup bg = MAP.get(name);
        if (bg == null) {
            throw new IllegalArgumentException("invalid bar group " + name);
        }
        return bg;
    }
    
}