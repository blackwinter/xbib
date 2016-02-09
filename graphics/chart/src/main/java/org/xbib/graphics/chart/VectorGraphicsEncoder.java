package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.component.Chart;
import org.xbib.graphics.vector.EPSGraphics2D;
import org.xbib.graphics.vector.PDFGraphics2D;
import org.xbib.graphics.vector.ProcessingPipeline;
import org.xbib.graphics.vector.SVGGraphics2D;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A helper class with static methods for saving Charts as bitmaps
 */
public final class VectorGraphicsEncoder {

    public enum VectorGraphicsFormat {
        EPS, PDF, SVG
    }

    private VectorGraphicsEncoder() {
    }

    public static void write(Chart chart, OutputStream outputStream, VectorGraphicsFormat vectorGraphicsFormat)
            throws IOException {
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
        if (outputStream != null) {
            outputStream.write(g.getBytes());
        }
    }
}
