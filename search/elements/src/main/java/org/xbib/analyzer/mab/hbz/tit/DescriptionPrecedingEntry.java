package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class DescriptionPrecedingEntry extends MABElement {
    
    private final static MABElement element = new DescriptionPrecedingEntry();
    
    private DescriptionPrecedingEntry() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
