package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.chartpart.Chart;
import org.xbib.graphics.vector.EPSGraphics2D;
import org.xbib.graphics.vector.PDFGraphics2D;
import org.xbib.graphics.vector.ProcessingPipeline;
import org.xbib.graphics.vector.SVGGraphics2D;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A helper class with static methods for saving Charts as bitmaps
 */
public final class VectorGraphicsEncoder {

    /**
     * Constructor - Private constructor to prevent instantiation
     */
    private VectorGraphicsEncoder() {

    }

    public static void saveVectorGraphic(Chart chart, String fileName, VectorGraphicsFormat vectorGraphicsFormat) throws IOException {

        ProcessingPipeline g = null;

        switch (vectorGraphicsFormat) {
            case EPS:
                g = new EPSGraphics2D(0.0, 0.0, chart.getWidth(), chart.getHeight());
                break;
            case PDF:
                g = new PDFGraphics2D(0.0, 0.0, chart.getWidth(), chart.getHeight());
                break;
            case SVG:
                g = new SVGGraphics2D(0.0, 0.0, chart.getWidth(), chart.getHeight());
                break;

            default:
                break;
        }

        chart.paint(g, chart.getWidth(), chart.getHeight());

        // Write the vector graphic output to a file
        FileOutputStream file = new FileOutputStream(fileName + "." + vectorGraphicsFormat.toString().toLowerCase());

        try {
            file.write(g.getBytes());
        } finally {
            file.close();
        }
    }

    public enum VectorGraphicsFormat {
        EPS, PDF, SVG;
    }

}
