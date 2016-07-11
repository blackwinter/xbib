package org.xbib.analyzer.mab.titel;

import org.xbib.etl.faceting.Facet;
import org.xbib.etl.faceting.TermFacet;
import org.xbib.etl.marc.dialects.mab.MABEntity;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;

import java.io.IOException;
import java.util.Map;

public class ExtendedType extends MABEntity {

    public final static String FACET = "rda.type";

    private String predicate;

    private Map<String, Object> codes;

    private Map<String, Object> facetcodes;

    public ExtendedType(Map<String,Object> params) {
        super(params);
        this.predicate = this.getClass().getSimpleName();
        if (params.containsKey("_predicate")) {
            this.predicate = params.get("_predicate").toString();
        }
        this.codes = getCodes();
        this.facetcodes = getFacetCodes();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean fields(MABEntityQueue.MABWorker worker, FieldList fields) throws IOException {
        String value = fields.getLast().data();
        if (codes != null) {
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && i + 1 < value.length()) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    worker.getWorkerState().getResource().add(predicate, code);
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
                    facetize(worker, code);
                }
            }
        }
        return true; // done
    }

    private MABEntity facetize(MABEntityQueue.MABWorker worker, String value) {
        if (value != null && !value.isEmpty()) {
            worker.getWorkerState().getFacets().putIfAbsent(FACET, new TermFacet().setName(FACET).setType(Literal.STRING));
            worker.getWorkerState().getFacets().get(FACET).addValue(value);
        }
        return this;
    }

    public Facet getDefaultFacet() {
        return new TermFacet().setName(FACET).setType(Literal.STRING).addValue(getParams().get("_default"));
    }
}
