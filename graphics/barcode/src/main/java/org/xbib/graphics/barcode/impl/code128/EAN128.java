package org.xbib.graphics.barcode.impl.code128;

import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * This class is an implementation of the Code 128 barcode.
 */
public class EAN128 extends Code128 {

    public EAN128() {
        this.bean = new EAN128Generator();
    }
    
    public EAN128Generator getEAN128Bean() {
        return (EAN128Generator) getGenerator();
    }

    public void configure(Settings cfg) throws Exception {
        Length mw = new Length(cfg.get("module-width", "0.21mm"), "mm");
        getEAN128Bean().setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);
        
        //Checksum mode        
        getEAN128Bean().setChecksumMode(ChecksumMode.byName(
            cfg.get("checksum", ChecksumMode.CP_AUTO.getName())));
        //Checkdigit place holder
        getEAN128Bean().setCheckDigitMarker(getFirstChar(
                cfg.get("check-digit-marker", "\u00f0")));
        //Template
        getEAN128Bean().setTemplate(cfg.get("template", ""));
        //group seperator aka FNC_1 
        getEAN128Bean().setGroupSeparator(getFirstChar(
                cfg.get("group-separator", "\u00f1")));

        if (cfg.containsSetting("human-readable")) {
            //omit Brackets for AI
            getEAN128Bean().setOmitBrackets(
                    cfg.getAsBoolean("human-readable.omit-brackets", false));
        }
    }
    
    private char getFirstChar(String s) {
        if (s != null && s.length() > 0) {
            return s.charAt(0);
        } else {
            throw new IllegalArgumentException("Value must have at least one character");
        }
    }
}