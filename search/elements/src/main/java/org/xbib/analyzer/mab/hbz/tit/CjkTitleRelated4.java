package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class CjkTitleRelated4 extends MABElement {
    
    private final static MABElement element = new CjkTitleRelated4();
    
    private CjkTitleRelated4() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
