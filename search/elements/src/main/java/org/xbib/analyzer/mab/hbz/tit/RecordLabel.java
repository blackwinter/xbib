package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.FieldCollection;

public class RecordLabel extends MABElement {

    private final static MABElement element = new RecordLabel();

    public static MABElement getInstance() {
        return element;
    }

    @Override
    public boolean fields(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder,
                       FieldCollection fields, String value) {
        builder.context().label(value.trim());
        if (value.length() == 24) {
            char satztyp = value.charAt(23);
            builder.context().resource().add("_type", String.valueOf(satztyp));
            if (satztyp == 'u') {
                builder.context().resource().add("_boost", "0.1");
            }
        } else {
            logger.warn("record label length is {} characters: {}", value.length(), value);
        }
        return true;
    }
}
