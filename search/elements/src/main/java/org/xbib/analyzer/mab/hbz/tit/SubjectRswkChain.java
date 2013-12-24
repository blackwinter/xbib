package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectRswkChain extends MABElement {

    private final static MABElement element = new SubjectRswkChain();

    private SubjectRswkChain() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
