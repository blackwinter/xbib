package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceTitle extends MABElement {
    
    private final static MABElement element = new SourceTitle();
    
    private SourceTitle() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
