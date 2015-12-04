/*
 * Copyright 2003,2004,2006 Jeremias Maerki.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xbib.graphics.barcode.impl.postnet;

import org.xbib.graphics.barcode.BaselineAlignment;
import org.xbib.graphics.barcode.HumanReadablePlacement;
import org.xbib.graphics.barcode.ClassicBarcodeLogicHandler;
import org.xbib.graphics.barcode.impl.AbstractVariableHeightLogicHandler;
import org.xbib.graphics.barcode.impl.HeightVariableBarcodeGenerator;
import org.xbib.graphics.barcode.output.Canvas;

/**
 * Logic Handler for POSTNET.
 * 
 * @author Chris Dolphy
 * @version $Id: POSTNETLogicHandler.java,v 1.4 2008/05/13 13:00:44 jmaerki Exp $
 */
public class POSTNETLogicHandler 
            extends AbstractVariableHeightLogicHandler {

    /**
     * Constructor 
     * @param bcBean the barcode implementation class
     * @param canvas the canvas to paint to
     */
    public POSTNETLogicHandler(HeightVariableBarcodeGenerator bcBean, Canvas canvas) {
        super(bcBean, canvas);
    }

    private double getStartY() {
        if (bcBean.hasQuietZone()) {
            return bcBean.getVerticalQuietZone();
        } else {
            return 0.0;
        }
    }            

    /** @see ClassicBarcodeLogicHandler */
    public void startBarcode(String msg, String formattedMsg) {
        super.startBarcode(msg, formattedMsg);
        y = getStartY();
    }

    /**
     * @see ClassicBarcodeLogicHandler#addBar(boolean, int)
     */
    public void addBar(boolean black, int height) {
        POSTNETGenerator pnBean = (POSTNETGenerator)bcBean;
        final double w = black ? bcBean.getBarWidth(1) : bcBean.getBarWidth(-1);
        final double h = bcBean.getBarHeight(height);
        final BaselineAlignment baselinePosition = pnBean.getBaselinePosition();
        
        if (black) {
            if (bcBean.getMsgPosition() == HumanReadablePlacement.HRP_TOP) {
                if (baselinePosition == BaselineAlignment.ALIGN_TOP) {
                    canvas.drawRectWH(x, y + bcBean.getHumanReadableHeight(), w, h);
                } else if (baselinePosition == BaselineAlignment.ALIGN_BOTTOM) {
                    canvas.drawRectWH(x, y + bcBean.getHeight() - h, w, h);
                }
            } else {
                if (baselinePosition == BaselineAlignment.ALIGN_TOP) {
                    canvas.drawRectWH(x, y, w, h);
                } else if (baselinePosition == BaselineAlignment.ALIGN_BOTTOM) {
                    canvas.drawRectWH(x, y + bcBean.getBarHeight() - h, w, h);
                } 
            }
        }
        x += w;
    }

}
