package org.xbib.graphics.barcode;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * This is a simple implementation of a BarcodeClassResolver.
 */
public class DefaultBarcodeClassResolver implements BarcodeClassResolver {

    private Map classes;
    private Set mainIDs;

    /**
     * Main constructor.
     * Already registers a default set of implementations.
     */
    public DefaultBarcodeClassResolver() {
        registerBarcodeClass("codabar", "org.krysalis.barcode4j.impl.codabar.Codabar", true);
        registerBarcodeClass("code39", "org.krysalis.barcode4j.impl.code39.Code39", true);
        registerBarcodeClass("code128", "org.krysalis.barcode4j.impl.code128.Code128", true);
        registerBarcodeClass("ean-128", "org.krysalis.barcode4j.impl.code128.EAN128", true);
        registerBarcodeClass("ean128", "org.krysalis.barcode4j.impl.code128.EAN128");
        registerBarcodeClass("2of5", "org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5");
        registerBarcodeClass("intl2of5",
                "org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5", true);
        registerBarcodeClass("interleaved2of5",
                "org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5");
        registerBarcodeClass("itf-14", "org.krysalis.barcode4j.impl.int2of5.ITF14", true);
        registerBarcodeClass("itf14", "org.krysalis.barcode4j.impl.int2of5.ITF14");
        registerBarcodeClass("ean-13", "org.krysalis.barcode4j.impl.upcean.EAN13", true);
        registerBarcodeClass("ean13", "org.krysalis.barcode4j.impl.upcean.EAN13");
        registerBarcodeClass("ean-8", "org.krysalis.barcode4j.impl.upcean.EAN8", true);
        registerBarcodeClass("ean8", "org.krysalis.barcode4j.impl.upcean.EAN8");
        registerBarcodeClass("upc-a", "org.krysalis.barcode4j.impl.upcean.UPCA", true);
        registerBarcodeClass("upca", "org.krysalis.barcode4j.impl.upcean.UPCA");
        registerBarcodeClass("upc-e", "org.krysalis.barcode4j.impl.upcean.UPCE", true);
        registerBarcodeClass("upce", "org.krysalis.barcode4j.impl.upcean.UPCE");
        registerBarcodeClass("postnet", "org.krysalis.barcode4j.impl.postnet.POSTNET", true);
        registerBarcodeClass("royal-mail-cbc",
                "org.krysalis.barcode4j.impl.fourstate.RoyalMailCBC", true);
        registerBarcodeClass("usps4cb",
                "org.krysalis.barcode4j.impl.fourstate.USPSIntelligentMail", true);
        registerBarcodeClass("pdf417", "org.krysalis.barcode4j.impl.pdf417.PDF417", true);
        registerBarcodeClass("datamatrix",
                "org.krysalis.barcode4j.impl.datamatrix.DataMatrix", true);
    }

    /**
     * Registers a barcode implementation.
     * @param id short name to use as a key
     * @param classname fully qualified classname
     */
    public void registerBarcodeClass(String id, String classname) {
        registerBarcodeClass(id, classname, false);
    }

    /**
     * Registers a barcode implementation.
     * @param id short name to use as a key
     * @param classname fully qualified classname
     * @param mainID indicates whether the name is the main name for the barcode
     */
    public void registerBarcodeClass(String id, String classname, boolean mainID) {
        if (this.classes == null) {
            this.classes = new java.util.HashMap();
            this.mainIDs = new java.util.HashSet();
        }
        this.classes.put(id.toLowerCase(), classname);
        if (mainID) {
            this.mainIDs.add(id);
        }
    }

    public Class resolve(String name) throws ClassNotFoundException {
        String clazz = null;
        if (this.classes != null) {
            clazz = (String)this.classes.get(name.toLowerCase());
        }
        if (clazz == null) {
            clazz = name;
        }
        Class cl = Class.forName(clazz);
        return cl;
    }

    public Class resolveBean(String name) throws ClassNotFoundException {
        String clazz = null;
        if (this.classes != null) {
            clazz = (String)this.classes.get(name.toLowerCase());
        }
        if (clazz == null) {
            clazz = name;
        }
        Class cl = Class.forName(clazz + "Bean");
        return cl;
    }

    public Collection getBarcodeNames() {
        return Collections.unmodifiableCollection(this.mainIDs);
    }
}
