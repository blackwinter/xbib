package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceTitle extends MABElement {
    
    private final static MABElement element = new SourceTitle();
    
    private SourceTitle() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
