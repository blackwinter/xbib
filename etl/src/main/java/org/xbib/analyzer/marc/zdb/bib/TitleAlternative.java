package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;

public class TitleAlternative extends MARCEntity {
    
    private final static TitleAlternative element = new TitleAlternative();

    public static TitleAlternative getInstance() {
        return element;
    }
}
