
package org.xbib.graphics.barcode.impl;

import java.util.ArrayList;
import java.util.List;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.BarcodeGenerator;
import org.xbib.graphics.barcode.BarcodeUtil;
import org.xbib.graphics.barcode.HumanReadablePlacement;
import org.xbib.graphics.barcode.output.CanvasProvider;
import org.xbib.graphics.barcode.tools.Length;
import org.xbib.common.settings.Settings;

/**
 * Base class for barcode implementations.
 */
public abstract class ConfigurableBarcodeGenerator implements BarcodeGenerator {

    /**
     * Contains all possible element names that may appear in barcode.
     */
    public static final String[] BARCODE_ELEMENTS;

    static {
        List<String> elements = new ArrayList<>();
        //All barcode names
        elements.addAll(BarcodeUtil.getInstance().getClassResolver().getBarcodeNames());
        //All configuration element names
        elements.add("height");
        elements.add("module-width");
        elements.add("wide-factor");
        elements.add("quiet-zone");
        elements.add("vertical-quiet-zone");
        elements.add("checksum");
        elements.add("human-readable");
        elements.add("human-readable-font");
        elements.add("human-readable-size");
        elements.add("font-name");
        elements.add("font-size");
        elements.add("placement");
        elements.add("pattern");
        elements.add("display-start-stop");
        elements.add("display-checksum");
        elements.add("interchar-gap-width");
        elements.add("tall-bar-height");
        elements.add("short-bar-height");
        elements.add("track-height");
        elements.add("ascender-height");
        elements.add("baseline-alignment");
        elements.add("template");
        elements.add("group-separator");
        elements.add("check-digit-marker");
        elements.add("omit-brackets");
        elements.add("shape"); //DataMatrix
        elements.add("row-height"); //PDF417
        elements.add("columns"); //PDF417
        elements.add("min-columns"); //PDF417
        elements.add("max-columns"); //PDF417
        elements.add("min-rows"); //PDF417
        elements.add("max-rows"); //PDF417
        elements.add("ec-level"); //PDF417
        elements.add("width-to-height-ratio");
        elements.add("min-symbol-size"); //DataMatrix
        elements.add("max-symbol-size"); //DataMatrix
        elements.add("codesets"); //Code128
        elements.add("bearer-bar-width"); //ITF-14
        elements.add("bearer-box"); //ITF-14
        BARCODE_ELEMENTS = (String[])elements.toArray(new String[elements.size()]);
    }

    protected AbstractBarcodeGenerator bean;

    public void configure(Settings cfg) throws Exception {
        //Height (must be evaluated after the font size because of setHeight())
        if (cfg.containsSetting("height")) {
            Length h = new Length(cfg.getAsDouble("height", 0d), "mm");
            getGenerator().setHeight(h.getValueAsMillimeter());
        }

        //Quiet zone
        //getGenerator().doQuietZone(cfg.getChild("quiet-zone").getAttributeAsBoolean("enabled", true));
        getGenerator().doQuietZone(cfg.getAsBoolean("quiet-zone.enabled", true));

        String qzs = cfg.get("quiet-zone");
        if (qzs != null) {
            Length qz = new Length(qzs, "mw");
            if (qz.getUnit().equalsIgnoreCase("mw")) {
                getGenerator().setQuietZone(qz.getValue() * getGenerator().getModuleWidth());
            } else {
                getGenerator().setQuietZone(qz.getValueAsMillimeter());
            }
        }

        //Vertical quiet zone
        String qzvs = cfg.get("vertical-quiet-zone");
        if (qzvs != null) {
            Length qz = new Length(qzvs, Length.INCH);
            if (qz.getUnit().equalsIgnoreCase("mw")) {
                getGenerator().setVerticalQuietZone(
                        qz.getValue() * getGenerator().getModuleWidth());
            } else {
                getGenerator().setVerticalQuietZone(
                        qz.getValueAsMillimeter());
            }
        }

        Settings hr = cfg.getAsSettings("human-readable");
        if (hr != null) {
            //Human-readable placement
            String v = hr.get("placement");
            if (v != null) {
                getGenerator().setMsgPosition(HumanReadablePlacement.byName(v));
            }
            String c = hr.get("font-size");
            if (c != null) {
                Length fs = new Length(c);
                getGenerator().setFontSize(fs.getValueAsMillimeter());
            }
            getGenerator().setFontName(hr.get("font-name", "Helvetica"));
            getGenerator().setPattern(hr.get("pattern", ""));
        } else {
            //Legacy code for compatibility
            //Human-readable placement
            String v = cfg.get("human-readable");
            if (v != null) {
                getGenerator().setMsgPosition(HumanReadablePlacement.byName(v));
            }
            String c = cfg.get("human-readable-size");
            if (c != null) {
                Length fs = new Length(c);
                getGenerator().setFontSize(fs.getValueAsMillimeter());
            }
            getGenerator().setFontName(cfg.get("human-readable-font", "Helvetica"));
        }
    }

    public AbstractBarcodeGenerator getGenerator() {
        return this.bean;
    }

    public void generateBarcode(CanvasProvider canvas, String msg) {
        getGenerator().generateBarcode(canvas, msg);
    }

    public BarcodeDimension calcDimensions(String msg) {
        return getGenerator().calcDimensions(msg);
    }

}
