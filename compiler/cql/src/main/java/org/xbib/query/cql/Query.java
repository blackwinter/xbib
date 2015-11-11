package org.xbib.query.cql;

import java.util.LinkedList;
import java.util.List;

/**
 * CQL query
 */
public class Query extends AbstractNode {

    private List<PrefixAssignment> prefixes = new LinkedList<PrefixAssignment>();

    private Query query;

    private ScopedClause clause;

    Query(PrefixAssignment assignment, Query query) {
        prefixes.add(assignment);
        this.query = query;
    }

    Query(ScopedClause clause) {
        this.clause = clause;
    }

    public List<PrefixAssignment> getPrefixAssignments() {
        return prefixes;
    }

    public Query getQuery() {
        return query;
    }

    public ScopedClause getScopedClause() {
        return clause;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (PrefixAssignment assignment : prefixes) {
            sb.append(assignment.toString()).append(' ');
        }
        if (query != null) {
            sb.append(query);
        }
        if (clause != null) {
            sb.append(clause);
        }
        return sb.toString();
    }

}
