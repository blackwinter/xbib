package org.xbib.analyzer.marc.bib;

import org.xbib.etl.marc.MARCEntity;


public class ItemLibraryIdentifier extends MARCEntity {

    private final static MARCEntity element = new ItemLibraryIdentifier();

    private ItemLibraryIdentifier() {
    }

    public static MARCEntity getInstance() {
        return element;
    }

}
