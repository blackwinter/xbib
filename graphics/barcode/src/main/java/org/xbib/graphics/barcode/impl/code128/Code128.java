
package org.xbib.graphics.barcode.impl.code128;


import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * This class is an implementation of the Code 128 barcode.
 */
public class Code128 extends ConfigurableBarcodeGenerator {

    public Code128() {
        this.bean = new Code128Generator();
    }

    public void configure(Settings cfg) throws Exception {
        Length mw = new Length(cfg.get("module-width", "0.21mm"), "mm");
        getCode128Bean().setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);

        String codesets = cfg.get("codesets");
        if (codesets != null) {
            codesets = codesets.toUpperCase();
            int bits = 0;
            if (codesets.indexOf('A') >= 0) {
                bits |= Code128Constants.CODESET_A;
            }
            if (codesets.indexOf('B') >= 0) {
                bits |= Code128Constants.CODESET_B;
            }
            if (codesets.indexOf('C') >= 0) {
                bits |= Code128Constants.CODESET_C;
            }
            getCode128Bean().setCodeset(bits);
        }
    }

    /**
     * @return the underlying Code128Bean
     */
    public Code128Generator getCode128Bean() {
        return (Code128Generator) getGenerator();
    }


}