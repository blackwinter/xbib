package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DateIssued extends MABElement {
    
    private final static MABElement element = new DateIssued();
    
    private DateIssued() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
