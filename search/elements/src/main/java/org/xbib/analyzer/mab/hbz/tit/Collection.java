package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Collection extends MABElement {
    
    private final static MABElement element = new Collection();
    
    private Collection() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
