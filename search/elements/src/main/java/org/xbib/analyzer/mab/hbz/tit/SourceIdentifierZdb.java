package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceIdentifierZdb extends MABElement {
    
    private final static MABElement element = new SourceIdentifierZdb();
    
    private SourceIdentifierZdb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
