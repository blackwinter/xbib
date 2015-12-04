
package org.xbib.graphics.barcode.impl.fourstate;

import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * Implements the USPS Intelligent Mail Barcode (Four State Customer Barcode).
 */
public class USPSIntelligentMail extends ConfigurableBarcodeGenerator {

    /** Create a new instance. */
    public USPSIntelligentMail() {
        this.bean = new USPSIntelligentMailGenerator();
    }
    
    /** {@inheritDoc} */
    public void configure(Settings cfg) throws Exception {
        //Module width (MUST ALWAYS BE FIRST BECAUSE QUIET ZONE MAY DEPEND ON IT)
        Length mw = new Length(cfg.get("module-width", USPSIntelligentMailGenerator.DEFAULT_MODULE_WIDTH_INCH + Length.INCH), Length.INCH);
        getUSPSIntelligentMailBean().setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);
    
        //Checksum mode    
        getUSPSIntelligentMailBean().setChecksumMode(ChecksumMode.byName(
            cfg.get("checksum", ChecksumMode.CP_AUTO.getName())));
    
        //Inter-character gap width    
        Length igw = new Length(cfg.get("interchar-gap-width", USPSIntelligentMailGenerator.DEFAULT_INTERCHAR_GAP_WIDTH_INCH + Length.INCH), Length.INCH);
        if (igw.getUnit().equalsIgnoreCase("mw")) {
            getUSPSIntelligentMailBean().setIntercharGapWidth(
                    igw.getValue() * getUSPSIntelligentMailBean().getModuleWidth());
        } else {
            getUSPSIntelligentMailBean().setIntercharGapWidth(igw.getValueAsMillimeter());
        }

        Length ah = new Length(cfg.get("ascender-height", USPSIntelligentMailGenerator.DEFAULT_ASCENDER_HEIGHT_INCH + Length.INCH), Length.INCH);
        getUSPSIntelligentMailBean().setAscenderHeight(ah.getValueAsMillimeter());
        
        Length th = new Length(cfg.get("track-height", USPSIntelligentMailGenerator.DEFAULT_TRACK_HEIGHT_INCH + Length.INCH), Length.INCH);
        getUSPSIntelligentMailBean().setTrackHeight(th.getValueAsMillimeter());
    }
   
    /**
     * Returns the underlying USPSIntelligentMailBean.
     * @return the underlying USPSIntelligentMailBean
     */
    public USPSIntelligentMailGenerator getUSPSIntelligentMailBean() {
        return (USPSIntelligentMailGenerator) getGenerator();
    }

}