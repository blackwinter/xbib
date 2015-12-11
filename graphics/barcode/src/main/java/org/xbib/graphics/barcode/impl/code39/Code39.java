
package org.xbib.graphics.barcode.impl.code39;

import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * This class is an implementation of the Code39 barcode.
 */
public class Code39 extends ConfigurableBarcodeGenerator {

    /** Create a new instance. */
    public Code39() {
        this.bean = new Code39Generator();
    }
    
    public void configure(Settings cfg) throws Exception {
        Length mw = new Length(cfg.get("module-width", "0.19mm"), "mm");
        getCode39Bean().setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);
    
        //Checksum mode    
        getCode39Bean().setChecksumMode(ChecksumMode.byName(
            cfg.get("checksum", ChecksumMode.CP_AUTO.getName())));
    
        //Wide factor    
        getCode39Bean().setWideFactor(cfg.getAsFloat("wide-factor", (float) Code39Generator.DEFAULT_WIDE_FACTOR));
    
        //Inter-character gap width    
        Length igw = new Length(cfg.get("interchar-gap-width", "1mw"), "mw");
        if (igw.getUnit().equalsIgnoreCase("mw")) {
            getCode39Bean().setIntercharGapWidth(
                    igw.getValue() * getCode39Bean().getModuleWidth());
        } else {
            getCode39Bean().setIntercharGapWidth(igw.getValueAsMillimeter());
        }
        
        if (cfg.containsSetting("extended-charset")) {
            getCode39Bean().setExtendedCharSetEnabled(cfg.getAsBoolean("extended-charset", false));
        }
        
        if (cfg.containsSetting("human-readable")) {
            //Display start/stop character and checksum in hr-message or not
            getCode39Bean().setDisplayStartStop(
                    cfg.getAsBoolean("human-readable.display-start-stop", false));
            getCode39Bean().setDisplayChecksum(
                    cfg.getAsBoolean("human-readable.display-checksum", false));
        }
    }

    public Code39Generator getCode39Bean() {
        return (Code39Generator) getGenerator();
    }

}