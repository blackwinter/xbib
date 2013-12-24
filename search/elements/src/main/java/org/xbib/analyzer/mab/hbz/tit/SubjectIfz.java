package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectIfz extends MABElement {
    
    private final static MABElement element = new SubjectIfz();
    
    private SubjectIfz() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
