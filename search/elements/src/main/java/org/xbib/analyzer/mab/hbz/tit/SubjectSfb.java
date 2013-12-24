package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectSfb extends MABElement {
    
    private final static MABElement element = new SubjectSfb();
    
    private SubjectSfb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
