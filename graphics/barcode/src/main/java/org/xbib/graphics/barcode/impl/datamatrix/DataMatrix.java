package org.xbib.graphics.barcode.impl.datamatrix;

import java.awt.Dimension;

import org.xbib.graphics.barcode.impl.ConfigurableBarcodeGenerator;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * This class is an implementation of the DataMatrix barcode.
 *
 */
public class DataMatrix extends ConfigurableBarcodeGenerator {

    /** Create a new instance. */
    public DataMatrix() {
        this.bean = new DataMatrixGenerator();
    }

    public void configure(Settings cfg) throws Exception {
        //Module width (MUST ALWAYS BE FIRST BECAUSE QUIET ZONE MAY DEPEND ON IT)
        String mws = cfg.get("module-width", null);
        if (mws != null) {
            Length mw = new Length(mws, "mm");
            getDataMatrixBean().setModuleWidth(mw.getValueAsMillimeter());
        }

        super.configure(cfg);

        if (cfg.containsSetting("shape")) {
            getDataMatrixBean().setShape(SymbolShapeHint.byName(cfg.get("shape")));
        }

        if (cfg.containsSetting("min-symbol-size")) {
            getDataMatrixBean().setMinSize(parseSymbolSize(cfg.get("min-symbol-size")));
        }
        if (cfg.containsSetting("max-symbol-size")) {
            getDataMatrixBean().setMaxSize(parseSymbolSize(cfg.get("max-symbol-size")));
        }
    }

    private Dimension parseSymbolSize(String size) {
        int idx = size.indexOf('x');
        Dimension dim;
        if (idx > 0) {
            dim = new Dimension(Integer.parseInt(size.substring(0, idx)),
                    Integer.parseInt(size.substring(idx + 1)));
        } else {
            int extent = Integer.parseInt(size);
            dim = new Dimension(extent, extent);
        }
        return dim;
    }

    /**
     * @return the underlying DataMatrix bean
     */
    public DataMatrixGenerator getDataMatrixBean() {
        return (DataMatrixGenerator) getGenerator();
    }

}