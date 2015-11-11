package org.xbib.query.cql.elasticsearch;

import org.xbib.query.cql.elasticsearch.ast.Expression;
import org.xbib.query.cql.elasticsearch.ast.Modifier;
import org.xbib.query.cql.elasticsearch.ast.Name;
import org.xbib.query.cql.elasticsearch.ast.Operator;
import org.xbib.query.cql.elasticsearch.ast.Token;

public interface Visitor {

    void visit(Token node);

    void visit(Name node);

    void visit(Modifier node);

    void visit(Operator node);

    void visit(Expression node);

}
