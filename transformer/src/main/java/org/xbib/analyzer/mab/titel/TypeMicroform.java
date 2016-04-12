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

    private String facet = "dc.format";

    private final Map<String, Object> codes;

    private final Map<String, Object> facetcodes;

    public TypeMicroform(Map<String,Object> params) {
        super(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
        codes = getCodes();
        facetcodes =getFacetCodes();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean fields(MABEntityQueue.MABWorker worker, FieldList fields) throws IOException {
        String value = fields.getLast().data();
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
        state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
        state.getFacets().get(facet).addValue(value);
        return this;
    }
}
