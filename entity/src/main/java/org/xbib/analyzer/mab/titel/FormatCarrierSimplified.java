package org.xbib.analyzer.mab.titel;

import org.xbib.entities.faceting.StringFacet;
import org.xbib.entities.marc.dialects.mab.MABEntity;
import org.xbib.entities.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.entities.marc.dialects.mab.MABEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;

import java.io.IOException;
import java.util.Map;

public class FormatCarrierSimplified extends MABEntity {

    private final static FormatCarrierSimplified element = new FormatCarrierSimplified();

    public static FormatCarrierSimplified getInstance() {
        return element;
    }

    public final static String FACET = "dc.format";

    private String predicate;

    private Map<String, Object> codes;

    private Map<String, Object> facetcodes;

    @Override
    public MABEntity setSettings(Map params) {
        super.setSettings(params);
        this.predicate = this.getClass().getSimpleName();
        if (params.containsKey("_predicate")) {
            this.predicate = params.get("_predicate").toString();
        }
        this.codes = (Map<String, Object>) getSettings().get("codes");
        this.facetcodes = (Map<String, Object>) getSettings().get("facetcodes");
        return this;
    }

    @Override
    public boolean fields(MABEntityQueue.MABWorker worker,
                          FieldList fields, String value) throws IOException {
        if (value == null || value.isEmpty()) {
            value = fields.getLast().data();
        }
        if (codes != null) {
            for (int i = 0; i < value.length(); i++) {
                String code = (String) codes.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) codes.get(value.substring(i, i + 2));
                }
                worker.state().getResource().add(predicate, code);
            }
        }
        if (facetcodes != null) {
            for (int i = 0; i < value.length(); i++) {
                String code = (String) facetcodes.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) facetcodes.get(value.substring(i, i + 2));
                }
                facetize(worker.state(), code);
            }
        }
        return true; // done!
    }

    private MABEntity facetize(MABEntityBuilderState state, String value) {
        if (value != null && !value.isEmpty()) {
            if (state.getFacets().get(FACET) == null) {
                state.getFacets().put(FACET, new StringFacet().setName(FACET).setType(Literal.STRING));
            }
            state.getFacets().get(FACET).addValue(value);
        }
        return this;
    }

}
