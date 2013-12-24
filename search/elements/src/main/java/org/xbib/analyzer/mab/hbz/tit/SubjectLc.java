package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectLc extends MABElement {
    
    private final static MABElement element = new SubjectLc();
    
    private SubjectLc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
