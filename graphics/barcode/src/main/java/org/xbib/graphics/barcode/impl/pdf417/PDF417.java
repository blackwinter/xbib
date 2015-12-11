
package org.xbib.graphics.barcode.impl.pdf417;

import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * This class is an implementation of the PDF417 barcode.
 */
public class PDF417 extends ConfigurableBarcodeGenerator  {

    public PDF417() {
        this.bean = new PDF417Generator();
    }
    
    public void configure(Settings cfg) throws Exception {
        if (cfg.containsSetting("module-width")) {
            Length mw = new Length(cfg.get("module-width"), "mm");
            getPDF417Bean().setModuleWidth(mw.getValueAsMillimeter());
        }

        super.configure(cfg);

        if (cfg.containsSetting("min-columns")) {
            getPDF417Bean().setMinCols(cfg.getAsInt("min-columns", 0));
        }
        if (cfg.containsSetting("max-columns")) {
            getPDF417Bean().setMaxCols(cfg.getAsInt("max-columns", 0));
        }
        if (cfg.containsSetting("min-rows")) {
            getPDF417Bean().setMinRows(cfg.getAsInt("min-rows", 0));
        }
        if (cfg.containsSetting("max-rows")) {
            getPDF417Bean().setMaxRows(cfg.getAsInt("max-rows",0 ));
        }
        
        //Setting "columns" will override min/max-columns and min/max-rows!!!
        if (cfg.containsSetting("columns")) {
            getPDF417Bean().setColumns(cfg.getAsInt("columns", 0));
        }
        
        getPDF417Bean().setErrorCorrectionLevel(cfg.getAsInt("ec-level", PDF417Generator.DEFAULT_ERROR_CORRECTION_LEVEL));
        
        if (cfg.containsSetting("row-height")) {
            Length rh = new Length(cfg.get("row-height"), "mw");
            if (rh.getUnit().equalsIgnoreCase("mw")) {
                getPDF417Bean().setRowHeight(rh.getValue() * getGenerator().getModuleWidth());
            } else {
                getPDF417Bean().setRowHeight(rh.getValueAsMillimeter());
            }
        } else {
            getPDF417Bean().setRowHeight(
                    PDF417Generator.DEFAULT_X_TO_Y_FACTOR * getGenerator().getModuleWidth());
        }
        
        if (cfg.containsSetting("width-to-height-ratio")) {
            getPDF417Bean().setWidthToHeightRatio(cfg.getAsFloat("width-to-height-ratio", 0F));
        }
    }
   
    public PDF417Generator getPDF417Bean() {
        return (PDF417Generator) getGenerator();
    }

}