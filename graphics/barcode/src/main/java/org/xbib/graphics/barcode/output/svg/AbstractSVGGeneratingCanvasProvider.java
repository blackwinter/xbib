package org.xbib.graphics.barcode.output.svg;

import org.xbib.graphics.barcode.output.AbstractXMLGeneratingCanvasProvider;
import org.xbib.graphics.barcode.output.BarcodeCanvasSetupException;

/**
 * Abstract base class for implementations that generate SVG.
 * 
 */
public abstract class AbstractSVGGeneratingCanvasProvider
    extends AbstractXMLGeneratingCanvasProvider {

    /** the SVG namespace: http://www.w3.org/2000/svg */
    public static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

    private boolean useNamespace = true;
    private String prefix = "";
    
    /**
     * Creates a new AbstractSVGCanvasProvider.
     * @param useNamespace Controls whether namespaces should be used
     * @param namespacePrefix the namespace prefix to use, null for no prefix
     * @throws BarcodeCanvasSetupException if setting up the provider fails
     */
    public AbstractSVGGeneratingCanvasProvider(boolean useNamespace, String namespacePrefix, 
                    int orientation) 
                throws BarcodeCanvasSetupException {
        super(orientation);
        if (!useNamespace && namespacePrefix != null) 
            throw new IllegalArgumentException("No prefix allow when namespaces are enabled");
        this.useNamespace = true;
        this.prefix = namespacePrefix;
    }
    
    /**
     * Creates a new AbstractSVGCanvasProvider with namespaces enabled.
     * @param namespacePrefix the namespace prefix to use, null for no prefix
     * @throws BarcodeCanvasSetupException if setting up the provider fails
     */
    public AbstractSVGGeneratingCanvasProvider(String namespacePrefix, int orientation) 
                throws BarcodeCanvasSetupException {
        this(true, namespacePrefix, orientation);
    }

    /**
     * Creates a new AbstractSVGCanvasProvider.
     * @param useNamespace Controls whether namespaces should be used
     * @throws BarcodeCanvasSetupException if setting up the provider fails
     */
    public AbstractSVGGeneratingCanvasProvider(boolean useNamespace, int orientation) 
                throws BarcodeCanvasSetupException {
        this(useNamespace, null, orientation);
    }

    /**
     * Creates a new AbstractSVGCanvasProvider with default settings (with 
     * namespaces, but without namespace prefix).
     * @throws BarcodeCanvasSetupException if setting up the provider fails
     */
    public AbstractSVGGeneratingCanvasProvider(int orientation) 
                throws BarcodeCanvasSetupException {
        this(true, null, orientation);
    }

    /**
     * Indicates whether namespaces are enabled.
     * @return true if namespaces are enabled
     */
    public boolean isNamespaceEnabled() {
        return this.useNamespace;
    }
    
    /**
     * Returns the namespace prefix
     * @return the namespace prefix (may be null)
     */
    public String getNamespacePrefix() {
        return this.prefix;
    }
    
    /**
     * Constructs a fully qualified element name based on the namespace 
     * settings.
     * @param localName the local name
     * @return the fully qualified name
     */
    protected String getQualifiedName(String localName) {
        if (prefix == null || "".equals(prefix)) {
            return localName;
        } else {
            return prefix + ':' + localName;
        }
    }

}
