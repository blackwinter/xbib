package org.xbib.graphics.barcode.impl.codabar;

import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * This class is an implementation of the Codabar barcode.
 */
public class Codabar extends ConfigurableBarcodeGenerator {

    public Codabar() {
        this.bean = new CodabarGenerator();
    }

    public void configure(Settings cfg) throws Exception {
        Length mw = new Length(cfg.get("module-width","0.21mm"), "mm");
        getGenerator().setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);

        getCodabarBean().setChecksumMode(ChecksumMode.byName(cfg.get("checksum", ChecksumMode.CP_AUTO.getName())));
        getCodabarBean().setWideFactor(cfg.getAsFloat("wide-factor", (float) CodabarGenerator.DEFAULT_WIDE_FACTOR));
        Settings hr = cfg.getAsSettings("human-readable");
        if (hr != null) {
            //Display start/stop character and checksum in hr-message or not
            getCodabarBean().setDisplayStartStop(
                    hr.getAsBoolean("display-start-stop", CodabarGenerator.DEFAULT_DISPLAY_START_STOP));
        }
    }

    /**
     * Returns the underlying Codabar Java Bean.
     * @return the underlying CodabarBean
     */
    public CodabarGenerator getCodabarBean() {
        return (CodabarGenerator) getGenerator();
    }

}