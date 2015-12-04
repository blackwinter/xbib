
package org.xbib.graphics.barcode.tools;

/**
 * Utility class for unit conversions.
 */
public class UnitConv {

    /**
     * Utility class: Constructor prevents instantiating when subclassed.
     */
    protected UnitConv() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Converts millimeters (mm) to points (pt)
     * @param mm the value in mm
     * @return the value in pt
     */
    public static double mm2pt(double mm) {
        return mm * 2.835;
    }

    /**
     * Converts points (pt) to millimeters (mm)
     * @param pt the value in pt
     * @return the value in mm
     */
    public static double pt2mm(double pt) {
        return pt / 2.835;
    }
    
    /**
     * Converts millimeters (mm) to inches (in)
     * @param mm the value in mm
     * @return the value in inches
     */
    public static double mm2in(double mm) {
        return mm / 25.4;
    }
    
    /**
     * Converts inches (in) to millimeters (mm)
     * @param in the value in inches
     * @return the value in mm
     */
    public static double in2mm(double in) {
        return in * 25.4;
    }
    
    /**
     * Converts millimeters (mm) to pixels (px)
     * @param mm the value in mm
     * @param resolution the resolution in dpi (dots per inch)
     * @return the value in pixels
     */
    public static int mm2px(double mm, int resolution) {
        return (int)Math.round(mm2in(mm) * resolution);
    }

}
