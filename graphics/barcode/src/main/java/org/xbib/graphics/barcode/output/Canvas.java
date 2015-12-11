package org.xbib.graphics.barcode.output;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.TextAlignment;

/**
 * This class is used by barcode rendering classes that paint a barcode using 
 * a coordinate system. The class delegates the call to a CanvasProvider and
 * provides some convenience methods.
 */
public class Canvas {

    private CanvasProvider canvasImp;

    /**
     * Main constructor
     * @param canvasImp the canvas provider to use
     */
    public Canvas(CanvasProvider canvasImp) {
        this.canvasImp = canvasImp;
    }

    /**
     * Returns the canvas provider in use.
     * @return the canvas provider
     */
    public CanvasProvider getCanvasImp() {
        return canvasImp;
    }

    /**
     * Sets the dimensions of the barcode.
     * @param dim the barcode dimensions
     */
    public void establishDimensions(BarcodeDimension dim) {
        getCanvasImp().establishDimensions(dim);
    }
    
    /**
     * @return the orientation of the barcode (0, 90, 180, 270, -90, -180, -270)
     */
    public int getOrientation() {
        return getCanvasImp().getOrientation();
    }

    /**
     * Draws a rectangle.
     * @param x1 x coordinate of the upper left corner
     * @param y1 y coordinate of the upper left corner
     * @param x2 x coordinate of the lower right corner
     * @param y2 y coordinate of the lower right corner
     */
    public void drawRect(double x1, double y1, double x2, double y2) {
        drawRectWH(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Draws a rectangle
     * @param x x coordinate of the upper left corner
     * @param y y coordinate of the upper left corner
     * @param w the width
     * @param h the height
     */
    public void drawRectWH(double x, double y, double w, double h) {
        getCanvasImp().deviceFillRect(x, y, w, h);
    }

    /**
     * Draws a centered character.
     * @param ch the character
     * @param x1 the left boundary
     * @param x2 the right boundary
     * @param y1 the y coordinate
     * @param fontName the name of the font
     * @param fontSize the size of the font
     */
    public void drawCenteredChar(char ch, double x1, double x2, double y1, 
                String fontName, double fontSize) {
        drawText(Character.toString(ch), x1, x2, y1, fontName, fontSize, TextAlignment.TA_CENTER);
    }

    /**
     * Draws text.
     * @param text the text to draw
     * @param x1 the left boundary
     * @param x2 the right boundary
     * @param y1 the y coordinate
     * @param fontName the name of the font
     * @param fontSize the size of the font
     * @param textAlign the text alignment
     */
    public void drawText(String text, double x1, double x2, double y1, 
                String fontName, double fontSize, TextAlignment textAlign) {
        getCanvasImp().deviceText(text, 
                x1, x2, y1, 
                fontName, fontSize, textAlign);
    }

}