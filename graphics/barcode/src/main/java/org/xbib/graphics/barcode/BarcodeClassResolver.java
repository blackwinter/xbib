
 package org.xbib.graphics.barcode;

import java.util.Collection;

/**
 * This interface is used to resolve arbitrary string to classnames of Barcode
 * implementations.
 */
public interface BarcodeClassResolver {

    /**
     * Returns the Class object of a Barcode implementation.
     * 
     * @param name Name or Classname of a Barcode implementation class
     * @return Class The class requested
     * @throws ClassNotFoundException If the class could not be resolved
     */
    Class resolve(String name) throws ClassNotFoundException;

    /**
     * Returns the Class object of a Barcode bean implementation.
     * 
     * @param name Name or Classname of a Barcode bean implementation class
     * @return Class The class requested
     * @throws ClassNotFoundException If the class could not be resolved
     */
    Class resolveBean(String name) throws ClassNotFoundException;
    
    /**
     * Return the names of all registered barcode types.
     * @return the names as a Collection of java.lang.String instances.
     */
    Collection getBarcodeNames();
    
}
