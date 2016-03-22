package org.xbib.query.cql.elasticsearch;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.query.DateUtil;
import org.xbib.query.cql.BooleanGroup;
import org.xbib.query.cql.BooleanOperator;
import org.xbib.query.cql.CQLParser;
import org.xbib.query.cql.Comparitor;
import org.xbib.query.cql.Identifier;
import org.xbib.query.cql.Index;
import org.xbib.query.cql.ModifierList;
import org.xbib.query.cql.PrefixAssignment;
import org.xbib.query.cql.Query;
import org.xbib.query.cql.Relation;
import org.xbib.query.cql.ScopedClause;
import org.xbib.query.cql.SearchClause;
import org.xbib.query.cql.SimpleName;
import org.xbib.query.cql.SingleSpec;
import org.xbib.query.cql.SortSpec;
import org.xbib.query.cql.SortedQuery;
import org.xbib.query.cql.SyntaxException;
import org.xbib.query.cql.Term;
import org.xbib.query.cql.Visitor;
import org.xbib.query.cql.elasticsearch.ast.Expression;
import org.xbib.query.cql.elasticsearch.ast.Modifier;
import org.xbib.query.cql.elasticsearch.ast.Name;
import org.xbib.query.cql.elasticsearch.ast.Node;
import org.xbib.query.cql.elasticsearch.ast.Operator;
import org.xbib.query.cql.elasticsearch.ast.Token;
import org.xbib.query.cql.elasticsearch.ast.TokenType;
import org.xbib.query.cql.elasticsearch.model.ElasticsearchQueryModel;

import java.io.IOException;
import java.util.Collection;
import java.util.Stack;

/**
 * Generate Elasticsearch QueryModel DSL from CQL abstract syntax tree
 */
public class ElasticsearchQueryGenerator implements Visitor {

    private ElasticsearchQueryModel model;

    private ElasticsearchFilterGenerator filterGenerator;

    private Stack<Node> stack;

    private int from;

    private int size;

    private String boostField;

    private String modifier;

    private Float factor;

    private String boostMode;

    private SourceGenerator sourceGen;

    private QueryGenerator queryGen;

    private FilterGenerator filterGen;

    private FacetsGenerator facetGen;

    private XContentBuilder sort;

    public ElasticsearchQueryGenerator() {
        this.from = 0;
        this.size = 10;
        this.model = new ElasticsearchQueryModel();
        this.filterGenerator = new ElasticsearchFilterGenerator(model);
        this.stack = new Stack<>();
        try {
            this.sourceGen = new SourceGenerator();
            this.queryGen = new QueryGenerator();
            this.filterGen = new FilterGenerator();
            this.facetGen = new FacetsGenerator();
        } catch (IOException e) {
            // ignore
        }
    }

    public ElasticsearchQueryModel getModel() {
        return model;
    }

    public ElasticsearchQueryGenerator setFrom(int from) {
        this.from = from;
        return this;
    }

    public ElasticsearchQueryGenerator setSize(int size) {
        this.size = size;
        return this;
    }

    public ElasticsearchQueryGenerator setSort(XContentBuilder sort) {
        this.sort = sort;
        return this;
    }

    public ElasticsearchQueryGenerator setBoostParams(String boostField, String modifier, Float factor, String boostMode) {
        this.boostField = boostField;
        this.modifier = modifier;
        this.factor = factor;
        this.boostMode = boostMode;
        return this;
    }

    public ElasticsearchQueryGenerator filter(String filter) {
        CQLParser parser = new CQLParser(filter);
        parser.parse();
        parser.getCQLQuery().accept(filterGenerator);
        return this;
    }

    public ElasticsearchQueryGenerator andfilter(String filterKey, Collection<String> filterValues) {
        filterGenerator.addAndFilter(filterKey, filterValues);
        return this;
    }

    public ElasticsearchQueryGenerator orfilter(String filterKey, Collection<String> filterValues) {
        filterGenerator.addOrFilter(filterKey, filterValues);
        return this;
    }

    public ElasticsearchQueryGenerator facet(String facetLimit, String facetSort) {
        try {
            facetGen.facet(facetLimit, facetSort);
        } catch (IOException e) {
            // ignore
        }
        return this;
    }

    public String getQueryResult() {
        try {
            return queryGen.getResult().string();
        } catch (IOException e) {
            return e.getMessage();
        }
    }


    public String getFacetResult() {
        try {
            return facetGen.getResult().string();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public String getSourceResult() {
        try {
            return sourceGen.getResult().string();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    public void visit(SortedQuery node) {
        try {
            if (node.getSortSpec() != null) {
                node.getSortSpec().accept(this);
            }
            queryGen.start();
            node.getQuery().accept(this);
            if (boostField != null) {
                queryGen.startBoost(boostField, modifier, factor, boostMode);
            }
            if (model.hasFilter()) {
                queryGen.startFiltered();
            } else if (filterGenerator.getResult().bytes().length() > 0) {
                queryGen.startFiltered();
            }
            Node querynode = stack.pop();
            if (querynode instanceof Token) {
                Token token = (Token) querynode;
                querynode = ".".equals(token.getString()) ?
                        new Expression(Operator.MATCH_ALL) :
                        new Expression(Operator.EQUALS, new Name("cql.allIndexes"), querynode);
            }
            queryGen.visit((Expression) querynode);
            if (model.hasFilter()) {
                queryGen.end();
                filterGen = new FilterGenerator(queryGen);
                filterGen.startFilter();
                filterGen.visit(model.getFilterExpression());
                filterGen.endFilter();
                queryGen.end();
            } else if (filterGenerator.getResult().bytes().length() > 0) {
                queryGen.end();
                queryGen.getResult().rawField("filter", filterGenerator.getResult().bytes());
                queryGen.endFiltered();
            }
            if (boostField != null) {
                queryGen.endBoost();
            }
            if (model.hasFacets()) {
                facetGen = new FacetsGenerator();
                facetGen.visit(model.getFacetExpression());
            }
            queryGen.end();
            Expression sortnode = model.getSort();
            SortGenerator sortGen = new SortGenerator();
            if (sortnode != null) {
                sortGen.start();
                sortGen.visit(sortnode);
                sortGen.end();
                sort = sortGen.getResult();
            }
            sourceGen.build(queryGen, from, size, sort, facetGen.getResult());
        } catch (IOException e) {
            throw new SyntaxException("unable to build a valid query from " + node + " , reason: " + e.getMessage(), e);
        }
    }

    @Override
    public void visit(SortSpec node) {
        if (node.getSingleSpec() != null) {
            node.getSingleSpec().accept(this);
        }
        if (node.getSortSpec() != null) {
            node.getSortSpec().accept(this);
        }
    }

    @Override
    public void visit(SingleSpec node) {
        if (node.getIndex() != null) {
            node.getIndex().accept(this);
        }
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
        if (!stack.isEmpty()) {
            model.setSort(stack);
        }
    }

    @Override
    public void visit(Query node) {
        for (PrefixAssignment assignment : node.getPrefixAssignments()) {
            assignment.accept(this);
        }
        if (node.getScopedClause() != null) {
            node.getScopedClause().accept(this);
        }
    }

    @Override
    public void visit(PrefixAssignment node) {
        node.getPrefix().accept(this);
        node.getURI().accept(this);
    }

    @Override
    public void visit(ScopedClause node) {
        if (node.getScopedClause() != null) {
            node.getScopedClause().accept(this);
        }
        node.getSearchClause().accept(this);
        if (node.getBooleanGroup() != null) {
            node.getBooleanGroup().accept(this);
        }
        // format disjunctive or conjunctive filters
        if (node.getSearchClause().getIndex() != null
                && model.isFilterContext(node.getSearchClause().getIndex().getContext())) {
            // assume that each operator-less filter is a conjunctive filter
            BooleanOperator op = node.getBooleanGroup() != null
                    ? node.getBooleanGroup().getOperator() : BooleanOperator.AND;
            String filtername = node.getSearchClause().getIndex().getName();
            Operator filterop = comparitorToES(node.getSearchClause().getRelation().getComparitor());
            Node filterterm = termToESwithoutWildCard(node.getSearchClause().getTerm());
            if (op == BooleanOperator.AND) {
                model.addConjunctiveFilter(filtername, filterterm, filterop);
            } else if (op == BooleanOperator.OR) {
                model.addDisjunctiveFilter(filtername, filterterm, filterop);
            }
        }
        // evaluate expression
        if (!stack.isEmpty() && stack.peek() instanceof Operator) {
            Operator op = (Operator) stack.pop();
            if (!stack.isEmpty()) {
                Node esnode = stack.pop();
                // add default context if node is a literal without a context
                if (esnode instanceof Token && TokenType.STRING.equals(esnode.getType())) {
                    esnode = new Expression(Operator.EQUALS, new Name("cql.allIndexes"), esnode);
                }
                if (stack.isEmpty()) {
                    // unary expression
                    throw new IllegalArgumentException("unary expression not allowed, op=" + op + " node=" + esnode);
                } else {
                    // binary expression
                    Node esnode2 = stack.pop();
                    // add default context if node is a literal without context
                    if (esnode2 instanceof Token && TokenType.STRING.equals(esnode2.getType())) {
                        esnode2 = new Expression(Operator.EQUALS, new Name("cql.allIndexes"), esnode2);
                    }
                    esnode = new Expression(op, esnode2, esnode);
                }
                stack.push(esnode);
            }
        }
    }

    @Override
    public void visit(SearchClause node) {
        if (node.getQuery() != null) {
            // CQL query in parenthesis
            node.getQuery().accept(this);
        }
        if (node.getTerm() != null) {
            node.getTerm().accept(this);
        }
        if (node.getIndex() != null) {
            node.getIndex().accept(this);
            String context = node.getIndex().getContext();
            // format facets
            if (model.isFacetContext(context)) {
                model.addFacet(node.getIndex().getName(), node.getTerm().getValue());
            }
        }
        if (node.getRelation() != null) {
            node.getRelation().accept(this);
            if (node.getRelation().getModifierList() != null && node.getIndex() != null) {
                // stack layout: op, list of modifiers, modifiable index
                Node op = stack.pop();
                StringBuilder sb = new StringBuilder();
                Node modifier = stack.pop();
                while (modifier instanceof Modifier) {
                    if (sb.length() > 0) {
                        sb.append('.');
                    }
                    sb.append(modifier.toString());
                    modifier = stack.pop();
                }
                String modifiable = sb.toString();
                stack.push(new Name(modifiable));
                stack.push(op);
            }
        }
        // evaluate expression
        if (!stack.isEmpty() && stack.peek() instanceof Operator) {
            Operator op = (Operator) stack.pop();
            Node arg1 = stack.pop();
            Node arg2 = stack.pop();
            // fold two expressions if they have the same operator
            boolean fold = arg1.isVisible() && arg2.isVisible()
                    && arg2 instanceof Expression
                    && ((Expression) arg2).getOperator().equals(op);
            Expression expression = fold ? new Expression((Expression) arg2, arg1) : new Expression(op, arg1, arg2);
            stack.push(expression);
        }
    }

    @Override
    public void visit(BooleanGroup node) {
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
        stack.push(booleanToES(node.getOperator()));
    }

    @Override
    public void visit(Relation node) {
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
        stack.push(comparitorToES(node.getComparitor()));
    }

    @Override
    public void visit(ModifierList node) {
        for (org.xbib.query.cql.Modifier modifier : node.getModifierList()) {
            modifier.accept(this);
        }
    }

    @Override
    public void visit(org.xbib.query.cql.Modifier node) {
        Node term = null;
        if (node.getTerm() != null) {
            node.getTerm().accept(this);
            term = stack.pop();
        }
        node.getName().accept(this);
        Node name = stack.pop();
        stack.push(new Modifier(name, term));
    }

    @Override
    public void visit(Term node) {
        stack.push(termToES(node));
    }

    @Override
    public void visit(Identifier node) {
        stack.push(new Name(node.getValue()));
    }

    @Override
    public void visit(Index node) {
        String context = node.getContext();
        String name = context != null ? context + "." + node.getName() : node.getName();
        Name esname = new Name(name, model.getVisibility(context));
        esname.setType(model.getElasticsearchType(name));
        stack.push(esname);
    }

    @Override
    public void visit(SimpleName node) {
        stack.push(new Name(node.getName()));
    }

    private Node termToES(Term node) {
        if (node.isLong()) {
            return new Token(Long.parseLong(node.getValue()));
        } else if (node.isFloat()) {
            return new Token(Double.parseDouble(node.getValue()));
        } else if (node.isIdentifier()) {
            return new Token(node.getValue());
        } else if (node.isDate()) {
            return new Token(DateUtil.parseDateISO(node.getValue()));
        } else if (node.isString()) {
            return new Token(node.getValue());
        }
        return null;
    }

    private Node termToESwithoutWildCard(Term node) {
        return node.isString() || node.isIdentifier()
                ? new Token(node.getValue().replaceAll("\\*", ""))
                : termToES(node);
    }

    private Operator booleanToES(BooleanOperator bop) {
        Operator op;
        switch (bop) {
            case AND:
                op = Operator.AND;
                break;
            case OR:
                op = Operator.OR;
                break;
            case NOT:
                op = Operator.ANDNOT;
                break;
            case PROX:
                op = Operator.PROX;
                break;
            default:
                throw new IllegalArgumentException("unknown CQL operator: " + bop);
        }
        return op;
    }

    private Operator comparitorToES(Comparitor op) {
        Operator esop;
        switch (op) {
            case EQUALS:
                esop = Operator.EQUALS;
                break;
            case GREATER:
                esop = Operator.RANGE_GREATER_THAN;
                break;
            case GREATER_EQUALS:
                esop = Operator.RANGE_GREATER_OR_EQUAL;
                break;
            case LESS:
                esop = Operator.RANGE_LESS_THAN;
                break;
            case LESS_EQUALS:
                esop = Operator.RANGE_LESS_OR_EQUALS;
                break;
            case NOT_EQUALS:
                esop = Operator.NOT_EQUALS;
                break;
            case WITHIN:
                esop = Operator.RANGE_WITHIN;
                break;
            case ADJ:
                esop = Operator.PHRASE;
                break;
            case ALL:
                esop = Operator.ALL;
                break;
            case ANY:
                esop = Operator.ANY;
                break;
            default:
                throw new IllegalArgumentException("unknown CQL comparitor: " + op);
        }
        return esop;
    }
}
