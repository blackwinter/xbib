package org.xbib.analyzer.mab.titel;

import org.xbib.etl.marc.dialects.mab.MABEntity;

public class Unassigned extends MABEntity {

    private final static Unassigned element = new Unassigned();

    public static Unassigned getInstance() {
        return element;
    }

}
