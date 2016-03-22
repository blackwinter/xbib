package org.xbib.query.cql.elasticsearch;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.query.cql.SyntaxException;
import org.xbib.query.cql.elasticsearch.ast.Expression;
import org.xbib.query.cql.elasticsearch.ast.Modifier;
import org.xbib.query.cql.elasticsearch.ast.Name;
import org.xbib.query.cql.elasticsearch.ast.Node;
import org.xbib.query.cql.elasticsearch.ast.Operator;
import org.xbib.query.cql.elasticsearch.ast.Token;

import java.io.IOException;

import static org.xbib.common.xcontent.XContentService.jsonBuilder;

/**
 * Build Elasticsearch query from abstract syntax tree
 */
public class QueryGenerator implements Visitor {

    private final XContentBuilder builder;

    public QueryGenerator() throws IOException {
        this.builder = jsonBuilder();
    }

    public void start() throws IOException {
        builder.startObject();
    }

    public void end() throws IOException {
        builder.endObject();
    }

    public void startFiltered() throws IOException {
        builder.startObject("filtered").startObject("query");
    }

    public void endFiltered() throws IOException {
        builder.endObject();
    }

    public void startBoost(String boostField, String modifier, Float factor, String boostMode) throws IOException {
        builder.startObject("function_score")
                .startObject("field_value_factor")
                .field("field", boostField)
                .field("modifier", modifier != null ? modifier : "log1p")
                .field("factor", factor != null ? factor : 1.0f)
                .endObject()
                .field("boost_mode", boostMode != null ? boostMode : "multiply")
                .startObject("query");
    }

    public void endBoost() throws IOException {
        builder.endObject().endObject();
    }

    public XContentBuilder getResult() {
        return builder;
    }

    @Override
    public void visit(Token token) {
        try {
            switch (token.getType()) {
                case BOOL:
                    builder.value(token.getBoolean());
                    break;
                case INT:
                    builder.value(token.getInteger());
                    break;
                case FLOAT:
                    builder.value(token.getFloat());
                    break;
                case DATETIME:
                    builder.value(token.getDate());
                    break;
                case STRING:
                    builder.value(token.getString());
                    break;
                default:
                    throw new IOException("unknown token type: " + token);
            }
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
                case 0: {
                    switch (op) {
                        case MATCH_ALL: {
                            builder.startObject("match_all").endObject();
                            break;
                        }
                    }
                    break;
                }
                case 1: {
                    // unary operators, anyone?
                    break;
                }
                case 2: {
                    // binary operators
                    Node arg1 = node.getArg1();
                    Node arg2 = node.getArgs().length > 1 ? node.getArg2() : null;
                    Token tok2 = arg2 instanceof Token ? (Token) arg2 : null;
                    boolean visible = false;
                    for (Node arg : node.getArgs()) {
                        visible = visible || arg.isVisible();
                    }
                    if (!visible) {
                        return;
                    }
                    switch (op) {
                        case EQUALS: {
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            builder.startObject("simple_query_string")
                                    .field("query", value)
                                    .field("fields", new String[]{field})
                                    .field("analyze_wildcard", true)
                                    .field("default_operator", "and")
                                    .endObject();
                            break;
                        }
                        case NOT_EQUALS: {
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            builder.startObject("bool").startObject("must_not");
                            builder.startObject("simple_query_string")
                                    .field("query", value)
                                    .field("fields", new String[]{field})
                                    .field("analyze_wildcard", true)
                                    .field("default_operator", "and")
                                    .endObject();
                            builder.endObject().endObject();
                            break;
                        }
                        case ALL: {
                            String field = arg1.toString();
                            String value = tok2 != null ? tok2.getString() : "";
                            builder.startObject("simple_query_string")
                                    .field("query", value)
                                    .field("fields", new String[]{field})
                                    .field("analyze_wildcard", true)
                                    .field("default_operator", "and")
                                    .endObject();
                            break;
                        }
                        case ANY: {
                            String field = arg1.toString();
                            String value = tok2 != null ? tok2.getString() : "";
                            builder.startObject("simple_query_string")
                                    .field("query", value)
                                    .field("fields", new String[]{field})
                                    .field("analyze_wildcard", true)
                                    .field("default_operator", "or")
                                    .endObject();
                            break;
                        }
                        case PHRASE: {
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            if (tok2 != null) {
                                if (tok2.isProtected()) {
                                    builder.startObject("match_phrase")
                                            .startObject(field)
                                            .field("query", tok2.getString())
                                            .field("slop", 0)
                                            .endObject()
                                            .endObject();
                                } else if (tok2.isAll()) {
                                    builder.startObject("match_all").endObject();
                                } else if (tok2.isWildcard()) {
                                    builder.startObject("wildcard").field(field, value).endObject();
                                } else if (tok2.isBoundary()) {
                                    builder.startObject("prefix").field(field, value).endObject();
                                } else {
                                    builder.startObject("match_phrase")
                                            .startObject(field)
                                            .field("query", value)
                                            .field("slop", 0)
                                            .endObject()
                                            .endObject();
                                }
                            }
                            break;
                        }
                        case RANGE_GREATER_THAN: {
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
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
                            // borders are inclusive
                            String field = arg1.toString();
                            String value = arg2 != null ? arg2.toString() : "";
                            String from = null;
                            String to = null;
                            if (tok2 != null) {
                                if (!tok2.isProtected()) {
                                    throw new IllegalArgumentException("range within: unable to derive range from a non-phrase: " + value);
                                }
                                if (tok2.getStringList().size() != 2) {
                                    throw new IllegalArgumentException("range within: unable to derive range from a phrase of length not equals to 2: " + tok2.getStringList());
                                }
                                from = tok2.getStringList().get(0);
                                to = tok2.getStringList().get(1);
                            }
                            builder.startObject("range").startObject(field)
                                    .field("from", from)
                                    .field("to", to)
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
                                if (arg1.isVisible() && arg2.isVisible()) {
                                    builder.startArray("must").startObject();
                                    arg1.accept(this);
                                    builder.endObject().startObject();
                                    arg2.accept(this);
                                    builder.endObject().endArray();
                                } else if (arg1.isVisible()) {
                                    builder.startObject("must");
                                    arg1.accept(this);
                                    builder.endObject();
                                } else if (arg2.isVisible()) {
                                    builder.startObject("must");
                                    arg2.accept(this);
                                    builder.endObject();
                                }
                                builder.endObject();
                            }
                            break;
                        }
                        case OR: {
                            // short expression
                            if (arg2 == null) {
                                if (arg1.isVisible()) {
                                    arg1.accept(this);
                                }
                            } else {
                                builder.startObject("bool");
                                if (arg1.isVisible() && arg2.isVisible()) {
                                    builder.startArray("should").startObject();
                                    arg1.accept(this);
                                    builder.endObject().startObject();
                                    arg2.accept(this);
                                    builder.endObject().endArray();
                                } else if (arg1.isVisible()) {
                                    builder.startObject("should");
                                    arg1.accept(this);
                                    builder.endObject();
                                } else if (arg2.isVisible()) {
                                    builder.startObject("should");
                                    arg2.accept(this);
                                    builder.endObject();
                                }
                                builder.endObject();
                            }
                            break;
                        }
                        case ANDNOT: {
                            if (arg2 == null) {
                                if (arg1.isVisible()) {
                                    arg1.accept(this);
                                }
                            } else {
                                builder.startObject("bool");
                                if (arg1.isVisible() && arg2.isVisible()) {
                                    builder.startArray("must_not").startObject();
                                    arg1.accept(this);
                                    builder.endObject().startObject();
                                    arg2.accept(this);
                                    builder.endObject().endArray();
                                } else if (arg1.isVisible()) {
                                    builder.startObject("must_not");
                                    arg1.accept(this);
                                    builder.endObject();
                                } else if (arg2.isVisible()) {
                                    builder.startObject("must_not");
                                    arg2.accept(this);
                                    builder.endObject();
                                }
                                builder.endObject();
                            }
                            break;
                        }
                        case PROX: {
                            String field = arg1.toString();
                            // we assume a default of 10 words is enough for proximity
                            String value = arg2 != null ? arg2.toString() + "~10" : "";
                            builder.startObject("field").field(field, value).endObject();
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("unable to translate operator while building elasticsearch query: " + op);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            throw new SyntaxException("internal error while building elasticsearch query", e);
        }
    }

}
