package org.xbib.graphics.barcode.impl;

import org.xbib.graphics.barcode.TextAlignment;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.tools.UnitConv;

/**
 * Drawing utilities.
 */
public class DrawingUtil {


    /**
     * Draws text on a canvas.
     * @param canvas the canvas to paint on
     * @param bean the barcode bean to get the font settings from
     * @param text the text to paint
     * @param x1 the left boundary
     * @param x2 the right boundary
     * @param y1 the y coordinate of the font's baseline
     * @param textAlign the text alignment
     */
    public static void drawText(Canvas canvas, AbstractBarcodeGenerator bean,
                                    String text, 
                                    double x1, double x2, double y1,
                                    TextAlignment textAlign) {
        canvas.drawText(text, x1, x2, 
                y1 - UnitConv.pt2mm(bean.getFontSize()) * 0.2, 
                bean.getFontName(), bean.getFontSize(),
                textAlign);
    }
    
}
