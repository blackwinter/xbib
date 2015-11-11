package org.xbib.analyzer.mab.titel;

import org.xbib.etl.faceting.TermFacet;
import org.xbib.etl.marc.dialects.mab.MABEntity;
import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;

import java.io.IOException;
import java.util.Map;

public class TypeMicroform extends MABEntity {

    private final static TypeMicroform element = new TypeMicroform();

    public static TypeMicroform getInstance() {
        return element;
    }

    private String facet = "dc.format";

    @Override
    public MABEntity setSettings(Map params) {
        super.setSettings(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
        return this;
    }

    @Override
    public boolean fields(MABEntityQueue.MABWorker worker,
                          FieldList fields, String value) throws IOException {
        if (value == null || value.isEmpty()) {
            value = fields.getLast().data();
        }
        Map<String, Object> codes = (Map<String, Object>) getSettings().get("codes");
        if (codes == null) {
            throw new IllegalStateException("no codes section for " + fields);
        }
        String predicate = (String) codes.get("_predicate");
        if (predicate == null) {
            predicate = this.getClass().getSimpleName();
        }
        for (int i = 0; i < value.length(); i++) {
            Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
            if (q != null) {
                String code = (String) q.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) q.get(value.substring(i, i + 2));
                }
                worker.state().getResource().add(predicate, code);
            }
        }
        Map<String, Object> facetcodes = (Map<String, Object>) getSettings().get("facetcodes");
        if (facetcodes != null) {
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) facetcodes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && (i + 1 < value.length())) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    if (code != null) {
                        facetize(worker.state(), code);
                    }
                }
            }
        }
        return true; // done!
    }

    private MABEntity facetize(MABEntityBuilderState state, String value) {
        if (state.getFacets().get(facet) == null) {
           state.getFacets().put(facet, new TermFacet().setName(facet).setType(Literal.STRING));
        }
        state.getFacets().get(facet).addValue(value);
        return this;
    }
}
