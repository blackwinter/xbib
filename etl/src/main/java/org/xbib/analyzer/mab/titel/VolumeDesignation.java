package org.xbib.analyzer.mab.titel;

import org.xbib.etl.marc.dialects.mab.MABEntity;

public class VolumeDesignation extends MABEntity {

    private final static VolumeDesignation element = new VolumeDesignation();

    public static VolumeDesignation getInstance() {
        return element;
    }

}
