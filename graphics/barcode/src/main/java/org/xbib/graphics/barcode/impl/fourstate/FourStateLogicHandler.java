package org.xbib.graphics.barcode.impl.fourstate;

import org.xbib.graphics.barcode.HumanReadablePlacement;
import org.xbib.graphics.barcode.impl.AbstractVariableHeightLogicHandler;
import org.xbib.graphics.barcode.impl.HeightVariableBarcodeGenerator;
import org.xbib.graphics.barcode.output.Canvas;

/**
 * Logic Handler to be used by "four-state" barcodes 
 * for painting on a Canvas.
 */
public class FourStateLogicHandler 
            extends AbstractVariableHeightLogicHandler {

    /**
     * Constructor 
     * @param bcBean the barcode implementation class
     * @param canvas the canvas to paint to
     */
    public FourStateLogicHandler(HeightVariableBarcodeGenerator bcBean, Canvas canvas) {
        super(bcBean, canvas);
    }

    private double getStartY() {
        double y = 0.0;
        if (bcBean.hasQuietZone()) {
            y += bcBean.getVerticalQuietZone();
        }
        if (bcBean.getMsgPosition() == HumanReadablePlacement.HRP_TOP) {
            y += bcBean.getHumanReadableHeight();
        }
        return y;
    }            

    /** {@inheritDoc} */
    public void addBar(boolean black, int height) {
        final double w = bcBean.getBarWidth(1);
        final double h = bcBean.getBarHeight(height);
        
        final double middle = bcBean.getBarHeight() / 2;
        double y1;
        switch (height) {
        case 0:
        case 2:
            y1 = middle - (bcBean.getBarHeight(0) / 2);
            break;
        case 1:
        case 3:
            y1 = middle - (bcBean.getBarHeight(3) / 2);
            break;
        default:
            throw new RuntimeException("Bug!");
        }
        
        canvas.drawRectWH(x, getStartY() + y1, w, h);
        x += w + bcBean.getBarWidth(-1);
    }

}
