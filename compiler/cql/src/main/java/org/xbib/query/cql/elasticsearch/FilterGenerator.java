package org.xbib.query.cql.elasticsearch;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.query.QuotedStringTokenizer;
import org.xbib.query.cql.SyntaxException;
import org.xbib.query.cql.elasticsearch.ast.Expression;
import org.xbib.query.cql.elasticsearch.ast.Modifier;
import org.xbib.query.cql.elasticsearch.ast.Name;
import org.xbib.query.cql.elasticsearch.ast.Node;
import org.xbib.query.cql.elasticsearch.ast.Operator;
import org.xbib.query.cql.elasticsearch.ast.Token;

import java.io.IOException;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Build query filter in Elasticsearch JSON syntax from abstract syntax tree
 */
public class FilterGenerator implements Visitor {

    private XContentBuilder builder;

    public FilterGenerator() throws IOException {
        this.builder = jsonBuilder();
    }

    public FilterGenerator(QueryGenerator queryGenerator) throws IOException {
        this.builder = queryGenerator.getResult();
    }

    public FilterGenerator start() throws IOException {
        builder.startObject();
        return this;
    }

    public FilterGenerator end() throws IOException {
        builder.endObject();
        return this;
    }

    public FilterGenerator startFilter() throws IOException {
        builder.startObject("filter");
        return this;
    }

    public FilterGenerator endFilter() throws IOException {
        builder.endObject();
        return this;
    }

    public XContentBuilder getResult() throws IOException {
        return builder;
    }

    @Override
    public void visit(Token node) {
        try {
            builder.value(node.getString());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Name node) {
        try {
            builder.field(node.toString());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Modifier node) {
        try {
            builder.value(node.toString());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Operator node) {
        try {
            builder.value(node.toString());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Expression node) {
        if (!node.isVisible()) {
            return;
        }
        try {
            Operator op = node.getOperator();
            switch (op.getArity()) {
                case 2: {
                    Node arg1 = node.getArg1();
                    Node arg2 = node.getArgs().length > 1 ? node.getArg2() : null;
                    boolean visible = false;
                    for (Node arg : node.getArgs()) {
                        visible = visible || arg.isVisible();
                    }
                    if (!visible) {
                        return;
                    }
                    Token tok2 = arg2 instanceof Token ? (Token) arg2 : null;
                    switch (op) {
                        case EQUALS: {
                            String field = arg1.toString();
                            String value = tok2 != null ? tok2.getString() : "";
                            builder.startObject(tok2 != null && tok2.isBoundary() ? "prefix" : "term");
                            builder.field(field, value).endObject();
                            break;
                        }
                        case NOT_EQUALS: {
                            String field = arg1.toString();
                            String value = tok2 != null ? tok2.getString() : "";
                            builder.startObject("not")
                                    .startObject(tok2 != null && tok2.isBoundary() ? "prefix" : "term")
                                    .field(field, value)
                                    .endObject().endObject();
                            break;
                        }
                        case ALL: {
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            boolean phrase = arg2 instanceof Token && ((Token) arg2).isProtected();
                            if (phrase) {
                                builder.startArray("and");
                                QuotedStringTokenizer qst = new QuotedStringTokenizer(value);
                                while (qst.hasMoreTokens()) {
                                    builder.startObject().startObject("term").field(field, qst.nextToken()).endObject().endObject();
                                }
                                builder.endArray();
                            } else {
                                builder.startObject(tok2 != null && tok2.isBoundary() ? "prefix" : "term")
                                        .field(field, value)
                                        .endObject();
                            }
                            break;
                        }
                        case ANY: {
                            boolean phrase = arg2 instanceof Token && ((Token) arg2).isProtected();
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            if (phrase) {
                                builder.startArray("or");
                                QuotedStringTokenizer qst = new QuotedStringTokenizer(value);
                                while (qst.hasMoreTokens()) {
                                    builder.startObject().startObject("term")
                                            .field(field, qst.nextToken()).endObject().endObject();
                                }
                                builder.endArray();
                            } else {
                                builder.startObject(tok2 != null && tok2.isBoundary() ? "prefix" : "term")
                                        .field(field, value)
                                        .endObject();
                            }
                            break;
                        }
                        case RANGE_GREATER_THAN: {
                            String field = arg1.toString();
                            String value = tok2 != null ? tok2.getString() : "";
                            builder.startObject("range").startObject(field)
                                    .field("from", value)
                                    .field("include_lower", false)
                                    .endObject().endObject();
                            break;
                        }
                        case RANGE_GREATER_OR_EQUAL: {
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            builder.startObject("range").startObject(field)
                                    .field("from", value)
                                    .field("include_lower", true)
                                    .endObject().endObject();
                            break;
                        }
                        case RANGE_LESS_THAN: {
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            builder.startObject("range").startObject(field)
                                    .field("to", value)
                                    .field("include_upper", false)
                                    .endObject().endObject();
                            break;
                        }
                        case RANGE_LESS_OR_EQUALS: {
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            builder.startObject("range").startObject(field)
                                    .field("to", value)
                                    .field("include_upper", true)
                                    .endObject().endObject();
                            break;
                        }
                        case RANGE_WITHIN: {
                            String field = arg1.toString();
                            String value = tok2 != null ? tok2.getString() : "";
                            String[] s = value.split(" ");
                            builder.startObject("range").startObject(field).
                                    field("from", s[0])
                                    .field("to", s[1])
                                    .field("include_lower", true)
                                    .field("include_upper", true)
                                    .endObject().endObject();
                            break;
                        }
                        case AND: {
                            if (arg2 == null) {
                                if (arg1.isVisible()) {
                                    arg1.accept(this);
                                }
                            } else {
                                builder.startObject("bool");
                                builder.startArray("must");
                                Node[] args = node.getArgs();
                                for (int i = 0; i < node.getArgs().length; i++) {
                                    if (args[i].isVisible()) {
                                        builder.startObject();
                                        args[i].accept(this);
                                        builder.endObject();
                                    }
                                }
                                builder.endArray();
                                builder.endObject();
                            }
                            break;
                        }
                        case OR: {
                            if (arg2 == null) {
                                if (arg1.isVisible()) {
                                    arg1.accept(this);
                                }
                            } else {
                                builder.startObject("bool");
                                builder.startArray("should");
                                Node[] args = node.getArgs();
                                for (int i = 0; i < node.getArgs().length; i++) {
                                    if (args[i].isVisible()) {
                                        builder.startObject();
                                        args[i].accept(this);
                                        builder.endObject();
                                    }
                                }
                                builder.endArray();
                                builder.endObject();
                            }
                            break;
                        }
                        case OR_FILTER: {
                            builder.startObject("bool");
                            builder.startArray("should");
                            Node[] args = node.getArgs();
                            for (int i = 0; i < args.length; i += 2) {
                                if (args[i].isVisible()) {
                                    builder.startObject().startObject("term");
                                    args[i].accept(this);
                                    args[i + 1].accept(this);
                                    builder.endObject().endObject();
                                }
                            }
                            builder.endArray();
                            builder.endObject();
                            break;
                        }
                        case AND_FILTER: {
                            builder.startObject("bool");
                            builder.startArray("must");
                            Node[] args = node.getArgs();
                            for (int i = 0; i < args.length; i += 2) {
                                if (args[i].isVisible()) {
                                    builder.startObject().startObject("term");
                                    args[i].accept(this);
                                    args[i + 1].accept(this);
                                    builder.endObject().endObject();
                                }
                            }
                            builder.endArray();
                            builder.endObject();
                            break;
                        }
                        case ANDNOT: {
                            if (arg2 == null) {
                                if (arg1.isVisible()) {
                                    arg1.accept(this);
                                }
                            } else {
                                builder.startObject("bool");
                                builder.startArray("must_not");
                                Node[] args = node.getArgs();
                                for (int i = 0; i < node.getArgs().length; i++) {
                                    if (args[i].isVisible()) {
                                        builder.startObject();
                                        args[i].accept(this);
                                        builder.endObject();
                                    }
                                }
                                builder.endArray();
                                builder.endObject();
                            }
                            break;
                        }
                        case PROX: {
                            String field = arg1.toString();
                            // we assume a  default of 10 words is enough for proximity
                            String value = arg2 != null ? arg2.toString() + "~10" : "";
                            builder.startObject("field").field(field, value).endObject();
                            break;
                        }
                        case TERM_FILTER: {
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            builder.startObject("term").field(field, value).endObject();
                            break;
                        }
                        case QUERY_FILTER: {
                            builder.startObject("query");
                            arg1.accept(this);
                            builder.endObject();
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("unable to translate operator while building elasticsearch query filter: " + op);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            throw new SyntaxException("internal error while building elasticsearch query filter", e);
        }
    }

}
