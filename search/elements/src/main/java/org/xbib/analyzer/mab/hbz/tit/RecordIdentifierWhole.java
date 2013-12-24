package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class RecordIdentifierWhole extends MABElement {
    
    private final static MABElement element = new RecordIdentifierWhole();
    
    private RecordIdentifierWhole() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
