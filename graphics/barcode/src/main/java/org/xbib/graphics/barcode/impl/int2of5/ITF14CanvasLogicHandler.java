package org.xbib.graphics.barcode.impl.int2of5;

import org.xbib.graphics.barcode.HumanReadablePlacement;
import org.xbib.graphics.barcode.impl.DefaultCanvasLogicHandler;
import org.xbib.graphics.barcode.output.Canvas;

/**
 * Specialized logic handler for ITF-14 (to paint the bearer bar).
 *
 */
public class ITF14CanvasLogicHandler extends DefaultCanvasLogicHandler {

    /**
     * Main constructor.
     * @param bcBean the barcode bean
     * @param canvas the canvas to paint on
     */
    public ITF14CanvasLogicHandler(ITF14Generator bcBean, Canvas canvas) {
        super(bcBean, canvas);
    }

    private ITF14Generator getITF14Bean() {
        return (ITF14Generator)this.bcBean;
    }

    /** {@inheritDoc} */
    public void startBarcode(String msg, String formattedMsg) {
        super.startBarcode(msg, formattedMsg);
        ITF14Generator bean = getITF14Bean();
        double bbw = bean.getBearerBarWidth();
        double w = dimensions.getWidthPlusQuiet();
        double h = bean.getBarHeight();
        double top = 0;
        if (bcBean.getMsgPosition() == HumanReadablePlacement.HRP_TOP) {
            top += bcBean.getHumanReadableHeight();
        }
        canvas.drawRect(0, top, w, top + bbw);
        canvas.drawRect(0, top + bbw + h, w, top + bbw + h + bbw);
        if (bean.isBearerBox()) {
            canvas.drawRect(0, top + bbw, bbw, top + bbw + h);
            canvas.drawRect(w - bbw, top + bbw, w, top + bbw + h);
        }
        //canvas.drawRect(getStartX(), 2 * bbw, getStartX() + dimensions.getWidth(), 3 * bbw);
    }

    /** {@inheritDoc} */
    protected double getStartX() {
        ITF14Generator bean = getITF14Bean();
        return super.getStartX() + (bean.isBearerBox() ? bean.getBearerBarWidth() : 0);
    }

    /** {@inheritDoc} */
    protected double getStartY() {
        double y = super.getStartY() + getITF14Bean().getBearerBarWidth();
        return y;
    }

    /** {@inheritDoc} */
    protected double getTextBaselinePosition() {
        if (bcBean.getMsgPosition() == HumanReadablePlacement.HRP_BOTTOM) {
            double ty = super.getTextBaselinePosition();
            ty += 2 * getITF14Bean().getBearerBarWidth();
            return ty;
        } else {
            return super.getTextBaselinePosition();
        }
    }

}
