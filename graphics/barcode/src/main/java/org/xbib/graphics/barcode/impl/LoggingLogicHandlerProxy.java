package org.xbib.graphics.barcode.impl;

import org.xbib.graphics.barcode.BarGroup;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;

/**
 * Proxy class for logging.
 */
public class LoggingLogicHandlerProxy implements ClassicBarcodeLogicHandler {

    private ClassicBarcodeLogicHandler delegate;
    
    /**
     * Main constructor.
     * @param delegate the logic handler that the method calls are passed to.
     */
    public LoggingLogicHandlerProxy(ClassicBarcodeLogicHandler delegate) {
        this.delegate = delegate;
    }

    /** @see ClassicBarcodeLogicHandler */
    public void startBarGroup(BarGroup type, String submsg) {
        System.out.println("startBarGroup(" + type + ", " + submsg + ")");
        delegate.startBarGroup(type, submsg);
    }

    /** @see ClassicBarcodeLogicHandler */
    public void endBarGroup() {
        System.out.println("endBarGroup()");
        delegate.endBarGroup();
    }

    /** @see ClassicBarcodeLogicHandler */
    public void addBar(boolean black, int weight) {
        System.out.println("addBar(" + black + ", " + weight + ")");
        delegate.addBar(black, weight);
    }

    /** @see ClassicBarcodeLogicHandler */
    public void startBarcode(String msg, String formattedMsg) {
        System.out.println("startBarcode(" + msg + ", " + formattedMsg + ")");
        delegate.startBarcode(msg, formattedMsg);
    }

    /** @see ClassicBarcodeLogicHandler */
    public void endBarcode() {
        System.out.println("endBarcode()");
        delegate.endBarcode();
    }

}
