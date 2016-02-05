package org.xbib.graphics.vector;

import org.xbib.graphics.vector.svg.SVGProcessor;

import java.awt.*;

/**
 * {@code Graphics2D} implementation that saves all operations to a string
 * in the <i>Scaled Vector Graphics</i> (SVG) format.
 */
public class SVGGraphics2D extends ProcessingPipeline {
    private final Processor processor;

    /**
     * Initializes a new VectorGraphics2D pipeline for translating Graphics2D
     * commands to SVG data. The document dimensions must be specified as
     * parameters.
     *
     * @param x      Left offset.
     * @param y      Top offset
     * @param width  Width.
     * @param height Height.
     */
    public SVGGraphics2D(double x, double y, double width, double height) {
        super(x, y, width, height);
        processor = new SVGProcessor();

        // Make graphics state match default state of Graphics2D
        setColor(Color.BLACK);
    }

    @Override
    protected Processor getProcessor() {
        return processor;
    }
}
