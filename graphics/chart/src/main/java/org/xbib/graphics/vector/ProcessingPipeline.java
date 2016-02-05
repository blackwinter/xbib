package org.xbib.graphics.vector;

import org.xbib.graphics.vector.util.PageSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for convenience implementations of {@code VectorGraphics2D}.
 */
public abstract class ProcessingPipeline extends VectorGraphics2D {
    private final PageSize pageSize;

    /**
     * Initializes a processing pipeline.
     *
     * @param x      Left offset.
     * @param y      Top offset
     * @param width  Width.
     * @param height Height.
     */
    public ProcessingPipeline(double x, double y, double width, double height) {
        pageSize = new PageSize(x, y, width, height);
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    protected abstract Processor getProcessor();

    public void writeTo(OutputStream out) throws IOException {
        Document doc = getProcessor().process(getCommands(), getPageSize());
        doc.write(out);
    }

    public byte[] getBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writeTo(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return out.toByteArray();
    }
}
