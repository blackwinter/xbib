package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectDdcDnb extends MABElement {
    
    private final static MABElement element = new SubjectDdcDnb();
    
    private SubjectDdcDnb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
