
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

    /** Create a new instance. */
    public POSTNET() {
        this.bean = new POSTNETGenerator();
    }
    
    /** {@inheritDoc} */
    public void configure(Settings cfg) throws Exception {
        //Module width (MUST ALWAYS BE FIRST BECAUSE QUIET ZONE MAY DEPEND ON IT)
        Length mw = new Length(cfg.getChild("module-width").getValue(
                POSTNETGenerator.DEFAULT_MODULE_WIDTH + Length.INCH), Length.MM);
        getPOSTNETBean().setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);
    
        //Checksum mode    
        getPOSTNETBean().setChecksumMode(ChecksumMode.byName(
            cfg.getChild("checksum").getValue(ChecksumMode.CP_AUTO.getName())));
    
        //Inter-character gap width    
        Length igw = new Length(cfg.getChild("interchar-gap-width").getValue(
                POSTNETGenerator.DEFAULT_MODULE_WIDTH + Length.INCH), Length.MM);
        getPOSTNETBean().setIntercharGapWidth(igw.getValueAsMillimeter());

        Length h = new Length(cfg.getChild("tall-bar-height").getValue(
                POSTNETGenerator.DEFAULT_TALL_BAR_HEIGHT + Length.INCH), Length.MM);
        getPOSTNETBean().setBarHeight(h.getValueAsMillimeter());
        
        Length hbh = new Length(cfg.getChild("short-bar-height").getValue(
                POSTNETGenerator.DEFAULT_SHORT_BAR_HEIGHT + Length.INCH), Length.MM);
        getPOSTNETBean().setShortBarHeight(hbh.getValueAsMillimeter());

        getPOSTNETBean().setBaselinePosition(BaselineAlignment.byName(
            cfg.getChild("baseline-alignment").getValue(BaselineAlignment.ALIGN_BOTTOM.getName())));

        Settings hr = cfg.getChild("human-readable", false);
        if (hr != null) {
            //Display checksum in hr-message or not
            getPOSTNETBean().setDisplayChecksum(
                    hr.getChild("display-checksum").getValueAsBoolean(false));
        }
    }
   
    /**
     * @return the underlying POSTNETBean
     */
    public POSTNETGenerator getPOSTNETBean() {
        return (POSTNETGenerator) getGenerator();
    }

}