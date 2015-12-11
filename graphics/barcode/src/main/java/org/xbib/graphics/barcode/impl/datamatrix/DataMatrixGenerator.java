package org.xbib.graphics.barcode.impl.datamatrix;

import java.awt.Dimension;
import java.io.IOException;

import org.xbib.common.settings.Settings;
import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.TwoDimBarcodeLogicHandler;
import org.xbib.graphics.barcode.impl.AbstractBarcodeGenerator;
import org.xbib.graphics.barcode.impl.DefaultTwoDimCanvasLogicHandler;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;
import org.xbib.graphics.barcode.tools.UnitConv;

/**
 * This class is an implementation of DataMatrix (ISO 16022:2000(E)).
 */
public class DataMatrixGenerator extends AbstractBarcodeGenerator {

    /** The default module width (dot size) for DataMatrix. */
    protected static final double DEFAULT_MODULE_WIDTH = UnitConv.in2mm(1.0 / 72); //1px at 72dpi

    /**
     * The requested shape. May be <code>FORCE_NONE</code>,
     * <code>FORCE_SQUARE</code> or <code>FORCE_RECTANGLE</code>.
     */
    private SymbolShapeHint shape;

    /** Optional: the minimum size of the symbol. */
    private Dimension minSize;
    /** Optional: the maximum size of the symbol. */
    private Dimension maxSize;

    /** Create a new instance. */
    public DataMatrixGenerator() {
        this.height = 0.0; //not used by DataMatrix
        this.moduleWidth = DEFAULT_MODULE_WIDTH;
        setQuietZone(1 * moduleWidth);
        this.shape = SymbolShapeHint.FORCE_NONE;
    }

    /**
     * Sets the requested shape for the generated barcodes.
     * @param shape requested shape. May be <code>SymbolShapeHint.FORCE_NONE</code>,
     * <code>SymbolShapeHint.FORCE_SQUARE</code> or <code>SymbolShapeHint.FORCE_RECTANGLE</code>.
     */
    public void setShape(SymbolShapeHint shape) {
        this.shape = shape;
    }

    /**
     * Gets the requested shape for the generated barcodes.
     * @return the requested shape (one of SymbolShapeHint.*).
     */
    public SymbolShapeHint getShape() {
        return shape;
    }

    /**
     * Sets the minimum symbol size that is to be produced.
     * @param minSize the minimum size (in pixels), or null for no constraint
     */
    public void setMinSize(Dimension minSize) {
        this.minSize = new Dimension(minSize);
    }

    /**
     * Returns the minimum symbol size that is to be produced. If the method returns null,
     * there's no constraint on the symbol size.
     * @return the minimum symbol size (in pixels), or null if there's no size constraint
     */
    public Dimension getMinSize() {
        if (this.minSize != null) {
            return new Dimension(this.minSize);
        } else {
            return null;
        }
    }

    /**
     * Sets the maximum symbol size that is to be produced.
     * @param maxSize the maximum size (in pixels), or null for no constraint
     */
    public void setMaxSize(Dimension maxSize) {
        this.maxSize = new Dimension(maxSize);
    }

    /**
     * Returns the maximum symbol size that is to be produced. If the method returns null,
     * there's no constraint on the symbol size.
     * @return the maximum symbol size (in pixels), or null if there's no size constraint
     */
    public Dimension getMaxSize() {
        if (this.maxSize != null) {
            return new Dimension(this.maxSize);
        } else {
            return null;
        }
    }

    @Override
    public void configure(Settings settings) throws Exception {

    }

    /** {@inheritDoc} */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null)
                || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }

        TwoDimBarcodeLogicHandler handler =
                new DefaultTwoDimCanvasLogicHandler(this, new Canvas(canvas));

        DataMatrixLogicImpl impl = new DataMatrixLogicImpl();
        impl.generateBarcodeLogic(handler, msg, getShape(), getMinSize(), getMaxSize());
    }

    /** {@inheritDoc} */
    public BarcodeDimension calcDimensions(String msg) {
        String encoded;
        try {
            encoded = DataMatrixHighLevelEncoder.encodeHighLevel(msg,
                    shape, getMinSize(), getMaxSize());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot fetch data: " + e.getLocalizedMessage());
        }
        DataMatrixSymbolInfo symbolInfo = DataMatrixSymbolInfo.lookup(encoded.length(), shape);

        double width = symbolInfo.getSymbolWidth() * getModuleWidth();
        double height = symbolInfo.getSymbolHeight() * getBarHeight();
        double qzh = (hasQuietZone() ? getQuietZone() : 0);
        double qzv = (hasQuietZone() ? getVerticalQuietZone() : 0);
        return new BarcodeDimension(width, height,
                width + (2 * qzh), height + (2 * qzv),
                qzh, qzv);
    }

    /** {@inheritDoc} */
    public double getVerticalQuietZone() {
        return getQuietZone();
    }

    /** {@inheritDoc} */
    public double getBarWidth(int width) {
        return moduleWidth;
    }

    /** {@inheritDoc} */
    public double getBarHeight() {
        return moduleWidth;
    }

}