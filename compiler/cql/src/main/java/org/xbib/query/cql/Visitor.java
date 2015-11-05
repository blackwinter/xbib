package org.xbib.query.cql;

/**
 * CQL abstract syntax tree visitor
 */
public interface Visitor {

    void visit(SortedQuery node);

    void visit(Query node);

    void visit(PrefixAssignment node);

    void visit(ScopedClause node);

    void visit(BooleanGroup node);

    void visit(SearchClause node);

    void visit(Relation node);

    void visit(Modifier node);

    void visit(ModifierList node);

    void visit(Term node);

    void visit(Identifier node);

    void visit(Index node);

    void visit(SimpleName node);

    void visit(SortSpec node);

    void visit(SingleSpec node);

}
