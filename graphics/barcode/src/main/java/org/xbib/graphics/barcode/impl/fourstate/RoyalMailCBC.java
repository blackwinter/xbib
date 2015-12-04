
package org.xbib.graphics.barcode.impl.fourstate;

import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * Implements the Royal Mail Customer Barcode.
 */
public class RoyalMailCBC extends ConfigurableBarcodeGenerator {

    /** Create a new instance. */
    public RoyalMailCBC() {
        this.bean = new RoyalMailCBCGenerator();
    }
    
    public void configure(Settings cfg) throws Exception {
        Length mw = new Length(cfg.get("module-width", "0.53mm"), "mm");
        getRoyalMailCBCBean().setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);
    
        //Checksum mode    
        getRoyalMailCBCBean().setChecksumMode(ChecksumMode.byName(
            cfg.get("checksum", ChecksumMode.CP_AUTO.getName())));
    
        //Inter-character gap width    
        Length igw = new Length(cfg.get("interchar-gap-width", "1mw"), "mw");
        if (igw.getUnit().equalsIgnoreCase("mw")) {
            getRoyalMailCBCBean().setIntercharGapWidth(
                    igw.getValue() * getRoyalMailCBCBean().getModuleWidth());
        } else {
            getRoyalMailCBCBean().setIntercharGapWidth(igw.getValueAsMillimeter());
        }

        Length h = new Length(cfg.get("ascender-height", "1.8mm"), "mm");
        getRoyalMailCBCBean().setAscenderHeight(h.getValueAsMillimeter());
        
        Length hbh = new Length(cfg.get("track-height", "1.25mm"), "mm");
        getRoyalMailCBCBean().setTrackHeight(hbh.getValueAsMillimeter());

    }
   
    /**
     * Returns the underlying RoyalMailCBCBean.
     * @return the underlying RoyalMailCBCBean
     */
    public RoyalMailCBCGenerator getRoyalMailCBCBean() {
        return (RoyalMailCBCGenerator) getGenerator();
    }

}