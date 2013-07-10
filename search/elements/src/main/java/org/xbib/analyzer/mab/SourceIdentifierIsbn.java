package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceIdentifierIsbn extends MABElement {
    
    private final static MABElement element = new SourceIdentifierIsbn();
    
    private SourceIdentifierIsbn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
