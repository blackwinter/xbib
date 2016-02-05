package org.xbib.graphics.chart.internal;

public class Utils {

    /**
     * Constructor
     */
    private Utils() {

    }

    /**
     * Gets the offset for the beginning of the tick marks
     *
     * @param workingSpace
     * @param tickSpace
     * @return
     */
    public static double getTickStartOffset(double workingSpace, double tickSpace) {

        double marginSpace = workingSpace - tickSpace;
        return marginSpace / 2.0;
    }

    public static double pow(double base, int exponent) {

        if (exponent > 0) {
            return Math.pow(base, exponent);
        } else {
            return 1.0 / Math.pow(base, -1 * exponent);

        }
    }

}
