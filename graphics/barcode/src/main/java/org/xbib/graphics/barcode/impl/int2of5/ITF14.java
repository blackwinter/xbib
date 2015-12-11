
package org.xbib.graphics.barcode.impl.int2of5;

import org.xbib.graphics.barcode.impl.AbstractBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;


/**
 * This class is an implementation of ITF-14 (as defined by the
 * <a href="http://www.gs1.org">GS1 standards organization</a>).
 * ITF-14 is basically an Interleaved 2 of 5 barcode with an added, so-called bearer bar.
 */
public class ITF14 extends Interleaved2Of5 {

    protected AbstractBarcodeGenerator createBean() {
        return new ITF14Generator();
    }

    public void configure(Settings cfg) throws Exception {
        super.configure(cfg);

        //Bearer bar width
        if (cfg.containsSetting("bearer-bar-width")) {
            Length w = new Length(cfg.get("bearer-bar-width"), "mw");
            if (w.getUnit().equalsIgnoreCase("mw")) {
                getITFBean().setBearerBarWidth(w.getValue() * getGenerator().getModuleWidth());
            } else {
                getITFBean().setBearerBarWidth(w.getValueAsMillimeter());
            }
        }

        //Bearer box
        if (cfg.containsSetting("bearer-box")) {
            getITFBean().setBearerBox(cfg.getAsBoolean("bearer-box", false));
        }
    }

    /**
     * Returns the underlying {@code ITF14Bean}.
     * @return the underlying {@code ITF14Bean}
     */
    public ITF14Generator getITFBean() {
        return (ITF14Generator) getGenerator();
    }

}