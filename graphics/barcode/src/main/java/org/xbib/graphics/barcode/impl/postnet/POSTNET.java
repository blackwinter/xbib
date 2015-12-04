
package org.xbib.graphics.barcode.impl.postnet;

import org.xbib.graphics.barcode.BaselineAlignment;
import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * Implements the United States Postal Service POSTNET barcode.
 * 
 */
public class POSTNET extends ConfigurableBarcodeGenerator  {

    public POSTNET() {
        this.bean = new POSTNETGenerator();
    }
    
    public void configure(Settings cfg) throws Exception {
        Length mw = new Length(cfg.get("module-width", POSTNETGenerator.DEFAULT_MODULE_WIDTH + Length.INCH), Length.MM);
        getPOSTNETBean().setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);
    
        //Checksum mode    
        getPOSTNETBean().setChecksumMode(ChecksumMode.byName(
            cfg.get("checksum", ChecksumMode.CP_AUTO.getName())));
    
        //Inter-character gap width    
        Length igw = new Length(cfg.get("interchar-gap-width", POSTNETGenerator.DEFAULT_MODULE_WIDTH + Length.INCH), Length.MM);
        getPOSTNETBean().setIntercharGapWidth(igw.getValueAsMillimeter());

        Length h = new Length(cfg.get("tall-bar-height", POSTNETGenerator.DEFAULT_TALL_BAR_HEIGHT + Length.INCH), Length.MM);
        getPOSTNETBean().setBarHeight(h.getValueAsMillimeter());
        
        Length hbh = new Length(cfg.get("short-bar-height", POSTNETGenerator.DEFAULT_SHORT_BAR_HEIGHT + Length.INCH), Length.MM);
        getPOSTNETBean().setShortBarHeight(hbh.getValueAsMillimeter());

        getPOSTNETBean().setBaselinePosition(BaselineAlignment.byName(
            cfg.get("baseline-alignment", BaselineAlignment.ALIGN_BOTTOM.getName())));

        if (cfg.containsSetting("human-readable")) {
            //Display checksum in hr-message or not
            getPOSTNETBean().setDisplayChecksum(cfg.getAsBoolean("human-readable.display-checksum", false));
        }
    }
   
    /**
     * @return the underlying POSTNETBean
     */
    public POSTNETGenerator getPOSTNETBean() {
        return (POSTNETGenerator) getGenerator();
    }

}