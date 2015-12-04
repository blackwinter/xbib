
package org.xbib.graphics.barcode.impl.upcean;

import org.xbib.graphics.barcode.ChecksumMode;
import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * This is an abstract base class for UPC and EAN barcodes.
 */
public abstract class UPCEAN extends ConfigurableBarcodeGenerator {

    public void configure(Settings cfg) throws Exception {
        //Module width (MUST ALWAYS BE FIRST BECAUSE QUIET ZONE MAY DEPEND ON IT)
        Length mw = new Length(cfg.get("module-width", "0.33mm"), "mm");
        getUPCEANBean().setModuleWidth(mw.getValueAsMillimeter());

        super.configure(cfg);
        
        //Checksum mode        
        getUPCEANBean().setChecksumMode(ChecksumMode.byName(
            cfg.get("checksum", ChecksumMode.CP_AUTO.getName())));
    }

    /**
     * @return the underlying UPCEANBean
     */
    public UPCEANGenerator getUPCEANBean() {
        return (UPCEANGenerator) getGenerator();
    }
    
}