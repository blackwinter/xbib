
package org.xbib.graphics.barcode;

import org.xbib.common.settings.Settings;

/**
 * This is a convenience class to generate barcodes. It is implemented as
 * Singleton to cache the BarcodeClassResolver. However, the class also
 * contains a set of static methods which you can use of you manage your own
 * BarcodeClassResolver.
 */
public class BarcodeUtil {
    
    private static BarcodeUtil instance = null;
    
    private BarcodeClassResolver classResolver = new DefaultBarcodeClassResolver();
    
    /**
     * Creates a new BarcodeUtil object. This constructor is protected because
     * this class is designed as a singleton.
     */
    protected BarcodeUtil() {
        //nop
    }
    
    /**
     * Returns the default instance of this class.
     * @return the singleton
     */
    public static BarcodeUtil getInstance() {
        if (instance == null) {
            instance = new BarcodeUtil();
        }
        return instance;
    }
    
    /**
     * Returns the class resolver used by this class.
     * @return a BarcodeClassResolver instance
     */
    public BarcodeClassResolver getClassResolver() {
        return this.classResolver;
    }
    
    /**
     * Creates a BarcoderGenerator.
     * @param settings Configuration object that specifies the barcode to produce.
     * @param classResolver The BarcodeClassResolver to use for lookup of
     * barcode implementations.
     * @return the newly instantiated BarcodeGenerator
     * @throws BarcodeException if setting up a BarcodeGenerator fails
     */
    public static BarcodeGenerator createBarcodeGenerator(Settings settings,
                                    BarcodeClassResolver classResolver) 
            throws BarcodeException {
        Class cl = null;
        try {
            String type = settings.get("type");
            try {
                cl = classResolver.resolve(type);
            } catch (ClassNotFoundException cnfe) {
                cl = null;
            }
            if (cl == null) {
                throw new BarcodeException(
                    "No barcode configuration element not found");
            }
            BarcodeGenerator gen = (BarcodeGenerator)cl.newInstance();

            try {
                gen.configure(settings);
            } catch (Exception iae) {
                throw new BarcodeException("Cannot configure barcode generator", iae);
            }
            /*try {
                ContainerUtil.initialize(gen);
            } catch (Exception e) {
                throw new RuntimeException("Cannot initialize barcode generator. " 
                        + e.getMessage());
            }*/
            return gen;
        } catch (IllegalAccessException ia) {
            throw new RuntimeException("Problem while instantiating a barcode"
                    + " generator: " + ia.getMessage());
        } catch (InstantiationException ie) {
            throw new BarcodeException("Error instantiating a barcode generator: "
                    + cl.getName());
        }
    }
            
    /**
     * Creates a BarcoderGenerator.
     * @param settings Configuration object that specifies the barcode to produce.
     * @return the newly instantiated BarcodeGenerator
     * @throws BarcodeException if setting up a BarcodeGenerator fails
     */
    public BarcodeGenerator createBarcodeGenerator(Settings settings)
            throws BarcodeException {
        return createBarcodeGenerator(settings, this.classResolver);
    }

}
