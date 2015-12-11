package org.xbib.graphics.barcode.impl.code128;

import org.xbib.common.settings.Settings;
import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;
import org.xbib.graphics.barcode.impl.AbstractBarcodeGenerator;
import org.xbib.graphics.barcode.impl.DefaultCanvasLogicHandler;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.output.CanvasProvider;

/**
 * This class is an implementation of the Code 128 barcode.
 */
public class Code128Generator extends AbstractBarcodeGenerator {

    /** The default module width for Code 128. */
    protected static final double DEFAULT_MODULE_WIDTH = 0.21f; //mm

    /** Default codeset. */
    protected static final int DEFAULT_CODESET = Code128Constants.CODESET_ALL;

    /** Codeset used to encode the message. */
    private int codeset = DEFAULT_CODESET;

    /** Create a new instance. */
    public Code128Generator() {
        this.moduleWidth = DEFAULT_MODULE_WIDTH;
        setQuietZone(10 * this.moduleWidth);
        setVerticalQuietZone(0); //1D barcodes don't have vertical quiet zones
    }

    /**
     * Sets the codesets to use. This can be used to restrict the Code 128 codesets
     * if an application requires that.
     * @param codeset the codesets to use (see {@link Code128Constants}.CODESET_*)
     */
    public void setCodeset(int codeset) {
        if (codeset == 0) {
            throw new IllegalArgumentException("At least one codeset must be allowed");
        }
        this.codeset = codeset;
    }

    /**
     * Returns the codeset to be used.
     * @return the codeset (see {@link Code128Constants}.CODESET_*)
     */
    public int getCodeset() {
        return this.codeset;
    }

    protected boolean hasFontDescender() {
        return true;
    }

    public double getBarWidth(int width) {
        if ((width >= 1) && (width <= 4)) {
            return width * moduleWidth;
        } else {
            throw new IllegalArgumentException("Only widths 1 and 2 allowed");
        }
    }

    public BarcodeDimension calcDimensions(String msg) {
        Code128LogicImpl impl = createLogicImpl();
        int msgLen = 0;

        msgLen = impl.createEncodedMessage(msg).length + 1;

        final double width = ((msgLen * 11) + 13) * getModuleWidth();
        final double qz = (hasQuietZone() ? quietZone : 0);
        final double vqz = (hasQuietZone() ? quietZoneVertical : 0);

        return new BarcodeDimension(width, getHeight(),
                width + (2 * qz), getHeight() + (2 * vqz),
                qz, vqz);
    }

    private Code128LogicImpl createLogicImpl() {
        return new Code128LogicImpl(getCodeset());
    }

    @Override
    public void configure(Settings settings) throws Exception {

    }

    /** {@inheritDoc} */
    public void generateBarcode(CanvasProvider canvas, String msg) {
        if ((msg == null) || (msg.length() == 0)) {
            throw new NullPointerException("Parameter msg must not be empty");
        }

        ClassicBarcodeLogicHandler handler =
                new DefaultCanvasLogicHandler(this, new Canvas(canvas));
        //handler = new LoggingLogicHandlerProxy(handler);

        Code128LogicImpl impl = createLogicImpl();
        impl.generateBarcodeLogic(handler, msg);
    }

}