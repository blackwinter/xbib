
package org.xbib.graphics.barcode.impl;

import org.xbib.graphics.barcode.BarGroup;
import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.TwoDimBarcodeLogicHandler;
import org.xbib.graphics.barcode.output.Canvas;

/**
 * Default 2D Logic Handler implementation for painting on a Canvas.
 * 
 */
public class DefaultTwoDimCanvasLogicHandler implements TwoDimBarcodeLogicHandler {
    
    private AbstractBarcodeGenerator bcBean;
    private Canvas canvas;
    private double x = 0.0;
    private double y = 0.0;
    
    /**
     * Main constructor.
     * @param bcBean the barcode implementation class
     * @param canvas the canvas to paint to
     */
    public DefaultTwoDimCanvasLogicHandler(AbstractBarcodeGenerator bcBean, Canvas canvas) {
        this.bcBean = bcBean;
        this.canvas = canvas;
    }
    
    private double getStartX() {
        if (bcBean.hasQuietZone()) {
            return bcBean.getQuietZone();
        } else {
            return 0.0;
        }
    }            

    private double getStartY() {
        if (bcBean.hasQuietZone()) {
            return bcBean.getVerticalQuietZone();
        } else {
            return 0.0;
        }
    }            

    public void startBarcode(String msg, String formattedMsg) {
        //Calculate extents
        BarcodeDimension dim = bcBean.calcDimensions(msg);
        
        canvas.establishDimensions(dim);
        y = getStartY();
    }

    public void startRow() {
        x = getStartX();
    }

    public void startBarGroup(BarGroup type, String submsg) {
        //nop
    }

    public void addBar(boolean black, int width) {
        final double w = bcBean.getBarWidth(width);
        if (black) {
            canvas.drawRectWH(x, y, w, bcBean.getBarHeight());
        }
        x += w;
    }

    public void endBarGroup() {
        //nop
    }

    public void endRow() {
        y += bcBean.getBarHeight(); //=row height
    }

    public void endBarcode() {
        //nop
    }

}

