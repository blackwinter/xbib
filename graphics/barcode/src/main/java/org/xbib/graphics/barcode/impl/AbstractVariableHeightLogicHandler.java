
package org.xbib.graphics.barcode.impl;

import org.xbib.graphics.barcode.BarGroup;
import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;
import org.xbib.graphics.barcode.HumanReadablePlacement;
import org.xbib.graphics.barcode.TextAlignment;
import org.xbib.graphics.barcode.output.Canvas;
import org.xbib.graphics.barcode.tools.MessagePatternUtil;

/**
 * Logic Handler to be used by subclasses of HeightVariableBarcodeBean 
 * for painting on a Canvas.
 */
public abstract class AbstractVariableHeightLogicHandler 
            implements ClassicBarcodeLogicHandler {

    /** the barcode bean */
    protected HeightVariableBarcodeGenerator bcBean;
    /** the canvas to paint on */
    protected Canvas canvas;
    /** the cursor in x-direction */
    protected double x = 0.0;
    /** the cursor in y-direction */
    protected double y = 0.0;
    private String formattedMsg;
    private TextAlignment textAlignment = TextAlignment.TA_CENTER;

    /**
     * Constructor 
     * @param bcBean the barcode implementation class
     * @param canvas the canvas to paint to
     */
    public AbstractVariableHeightLogicHandler(HeightVariableBarcodeGenerator bcBean, Canvas canvas) {
        this.bcBean = bcBean;
        this.canvas = canvas;
    }

    /**
     * Sets the alignment of the human-readable part.
     * @param align the new alignment
     */
    public void setTextAlignment(TextAlignment align) {
        if (align == null) {
            throw new NullPointerException("align must not be null");
        }
        this.textAlignment = align;
    }
    
    private double getStartX() {
        if (bcBean.hasQuietZone()) {
            return bcBean.getQuietZone();
        } else {
            return 0.0;
        }
    }            

    /** {@inheritDoc} */
    public void startBarcode(String msg, String formattedMsg) {
        this.formattedMsg = MessagePatternUtil.applyCustomMessagePattern(
                formattedMsg, bcBean.getPattern());
        //Calculate extents
        BarcodeDimension dim = bcBean.calcDimensions(msg);       
        canvas.establishDimensions(dim);        
        x = getStartX();
    }

    /**
     * Determines the Y coordinate for the baseline of the human-readable part.
     * @return the adjusted Y coordinate
     */
    protected double getTextY() {
        double texty = 0.0;
        if (bcBean.getMsgPosition() == HumanReadablePlacement.HRP_NONE) {
            //nop
        } else if (bcBean.getMsgPosition() == HumanReadablePlacement.HRP_TOP) {
            texty += bcBean.getHumanReadableHeight();
        } else if (bcBean.getMsgPosition() == HumanReadablePlacement.HRP_BOTTOM) {
            texty += bcBean.getHeight();
            if (bcBean.hasQuietZone()) {
                texty += bcBean.getVerticalQuietZone();
            }
        }
        return texty;
    }
    
    /** {@inheritDoc} */
    public void endBarcode() {
        if (bcBean.getMsgPosition() != HumanReadablePlacement.HRP_NONE) {
            double texty = getTextY();
            DrawingUtil.drawText(canvas, bcBean, formattedMsg, 
                    getStartX(), x, texty, this.textAlignment);
        }
    }

    /** {@inheritDoc} */
    public void startBarGroup(BarGroup barGroup, String string) {
    }

    /** {@inheritDoc} */
    public void endBarGroup() {
    }

}
