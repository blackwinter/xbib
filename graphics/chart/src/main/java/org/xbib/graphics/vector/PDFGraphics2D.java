package org.xbib.graphics.vector;

import org.xbib.graphics.vector.pdf.PDFProcessor;

import java.awt.*;

/**
 * {@code Graphics2D} implementation that saves all operations to a string
 * in the <i>Portable Document Format</i> (PDF).
 */
public class PDFGraphics2D extends ProcessingPipeline {
    private final Processor processor;

    /**
     * Initializes a new VectorGraphics2D pipeline for translating Graphics2D
     * commands to PDF data. The document dimensions must be specified as
     * parameters.
     *
     * @param x      Left offset.
     * @param y      Top offset
     * @param width  Width.
     * @param height Height.
     */
    public PDFGraphics2D(double x, double y, double width, double height) {
        super(x, y, width, height);
        processor = new PDFProcessor();

        // TODO: Default graphics state does not need to be printed in the document
        setColor(Color.BLACK);
        setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, null, 0f));
    }

    @Override
    protected Processor getProcessor() {
        return processor;
    }
}
