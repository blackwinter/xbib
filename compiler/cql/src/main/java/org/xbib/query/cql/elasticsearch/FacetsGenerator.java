package org.xbib.query.cql.elasticsearch;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.query.cql.SyntaxException;
import org.xbib.query.cql.elasticsearch.ast.Expression;
import org.xbib.query.cql.elasticsearch.ast.Modifier;
import org.xbib.query.cql.elasticsearch.ast.Name;
import org.xbib.query.cql.elasticsearch.ast.Operator;
import org.xbib.query.cql.elasticsearch.ast.Token;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Build facet from abstract syntax tree
 */
public class FacetsGenerator implements Visitor {

    private int facetlength = 10;

    private final XContentBuilder builder;

    public FacetsGenerator() throws IOException {
        this.builder = jsonBuilder();
    }

    public void start() throws IOException {
        builder.startObject();
    }

    public void end() throws IOException {
        builder.endObject();
    }

    public void startFacets() throws IOException {
        builder.startObject("aggregations");
    }

    public void endFacets() throws IOException {
        builder.endObject();
    }

    public XContentBuilder getResult() throws IOException {
        return builder;
    }

    @Override
    public void visit(Token node) {
        try {
            builder.value(node.toString().getBytes());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Name node) {
        try {
            builder.value(node.toString().getBytes());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Modifier node) {
        try {
            builder.value(node.toString().getBytes());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Operator node) {
        try {
            builder.value(node.toString().getBytes());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Expression node) {
        try {
            Operator op = node.getOperator();
            switch (op) {
                case TERMS_FACET: {
                    builder.startObject().field("myfacet", "myvalue")
                            .endObject();
                    break;
                }
                default:
                    throw new IllegalArgumentException(
                            "unable to translate operator while building elasticsearch facet: " + op);
            }
        } catch (IOException e) {
            throw new SyntaxException("internal error while building elasticsearch query", e);
        }
    }

    public FacetsGenerator facet(String facetLimit, String facetSort) throws IOException {
        if (facetLimit == null) {
            return this;
        }
        Map<String, Integer> facetMap = parseFacet(facetLimit);
        String[] sortSpec = facetSort != null ? facetSort.split(",") : new String[]{"recordCount", "descending"};
        String order = "_count";
        String dir = "desc";
        for (String s : sortSpec) {
            switch (s) {
                case "recordCount":
                    order = "_count";
                    break;
                case "alphanumeric":
                    order = "_term";
                    break;
                case "ascending":
                    dir = "asc";
                    break;
            }
        }
        builder.startObject();

        for (String index : facetMap.keySet()) {
            if ("*".equals(index)) {
                continue;
            }
            // TODO range aggregations etc.
            String facetType = "terms";
            Integer size = facetMap.get(index);
            builder.field(index)
                    .startObject()
                    .field(facetType).startObject()
                    .field("field", index)
                    .field("size", size > 0 ? size : 10)
                    .startObject("order")
                    .field(order, dir)
                    .endObject()
                    .endObject();
            builder.endObject();
        }
        builder.endObject();
        return this;
    }

    private Map<String, Integer> parseFacet(String spec) {
        Map<String, Integer> m = new HashMap<String, Integer>();
        m.put("*", facetlength);
        if (spec == null || spec.length() == 0) {
            return m;
        }
        String[] params = spec.split(",");
        for (String param : params) {
            int pos = param.indexOf(':');
            if (pos > 0) {
                int n = parseInt(param.substring(0, pos), facetlength);
                m.put(param.substring(pos + 1), n);
            } else if (param.length() > 0) {
                int n = parseInt(param, facetlength);
                m.put("*", n);
            }
        }
        return m;
    }

    private int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
