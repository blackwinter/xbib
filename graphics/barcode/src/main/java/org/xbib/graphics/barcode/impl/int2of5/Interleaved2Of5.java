
package org.xbib.graphics.barcode.impl.int2of5;

import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.impl.AbstractBarcodeGenerator;
import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * This class is an implementation of the Interleaved 2 of 5 barcode.
 *
 */
public class Interleaved2Of5 extends ConfigurableBarcodeGenerator {

    /** Create a new instance. */
    public Interleaved2Of5() {
        this.bean = createBean();
    }

    /**
     * Creates the Bean instance.
     * @return the Bean instance
     */
    protected AbstractBarcodeGenerator createBean() {
        return new Interleaved2Of5Generator();
    }

    public void configure(Settings cfg) throws Exception {
        Interleaved2Of5Generator bean = getInterleaved2Of5Bean();
        //Module width (MUST ALWAYS BE FIRST BECAUSE QUIET ZONE MAY DEPEND ON IT)
        Length mw = new Length(cfg.getChild("module-width")
                        .getValue(bean.getModuleWidth() + "mm"), "mm");
        bean.setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);

        //Checksum mode
        bean.setChecksumMode(ChecksumMode.byName(
            cfg.getChild("checksum").getValue(ChecksumMode.CP_AUTO.getName())));

        //Wide factor
        bean.setWideFactor(
            cfg.getChild("wide-factor").getValueAsFloat((float)bean.getWideFactor()));

        Settings hr = cfg.getChild("human-readable", false);
        if (hr != null) {
            //Display checksum in hr-message or not
            bean.setDisplayChecksum(
                    hr.getChild("display-checksum").getValueAsBoolean(false));
        }
    }

    /**
     * Returns the underlying {@code Interleaved2Of5Bean}.
     * @return the underlying Interleaved2Of5Bean
     */
    public Interleaved2Of5Generator getInterleaved2Of5Bean() {
        return (Interleaved2Of5Generator) getGenerator();
    }

}