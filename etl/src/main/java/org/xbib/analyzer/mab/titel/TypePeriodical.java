package org.xbib.analyzer.mab.titel;

import org.xbib.etl.faceting.Facet;
import org.xbib.etl.faceting.TermFacet;
import org.xbib.etl.marc.dialects.mab.MABEntity;
import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;

import java.io.IOException;
import java.util.Map;

public class TypePeriodical extends MABEntity {

    private final static TypePeriodical element = new TypePeriodical();

    public static TypePeriodical getInstance() {
        return element;
    }

    private String facet = "dc.type";

    private String predicate;

    private Map<String, Object> codes;

    private Map<String, Object> facetcodes;

    @Override
    public MABEntity setSettings(Map params) {
        super.setSettings(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
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
                Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && i + 1 < value.length()) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    worker.state().getResource().add(predicate, code);
                }
            }
        }
        if (facetcodes != null) {
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) facetcodes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && i + 1 < value.length()) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    if (code != null) {
                        facetize(worker.state(), code);
                    }
                }
            }
        }
        return true; // done
    }

    private MABEntity facetize(MABEntityBuilderState state, String value) {
        if (state.getFacets().get(facet) == null) {
            state.getFacets().put(facet, new TermFacet().setName(facet).setType(Literal.STRING));
        }
        Facet typeFacet = state.getFacets().get(facet);
        typeFacet.addValue(value);
        return this;
    }
}

