/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.query.cql;

import org.xbib.query.BreadcrumbModel;
import org.xbib.query.BreadcrumbTrail;
import org.xbib.query.BreadcrumbWriter;
import org.xbib.query.QueryModel;
import org.xbib.query.cql.model.CQLQueryModel;
import org.xbib.query.cql.model.Facet;
import org.xbib.query.cql.model.Filter;
import org.xbib.query.cql.model.Option;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;

/**
 * This is a CQL abstract syntax tree generator, useful for
 * normalizing CQL queries.
 *
 */
public final class CQLGenerator
        extends ArrayList<BreadcrumbTrail>
        implements Visitor, QueryModel, BreadcrumbWriter {

    /** helper for managing our CQL query model (facet/filter/option contexts, breadcrumb trails etc.) */
    private CQLQueryModel model;

    /** a replacement string */
    private String replacementString;

    /** string to be replaced */
    private String stringToBeReplaced;

    public CQLGenerator() {
        this.replacementString = null;
        this.stringToBeReplaced = null;
        this.model = new CQLQueryModel();
    }

    public CQLGenerator model(CQLQueryModel model) {
        this.model = model;
        return this;
    }
    
    public CQLQueryModel getModel() {
        return model;
    }

    public String getResult() {
        return model.getQuery();
    }

    @Override
    public void visit(SortedQuery node) {
        if (node.getSortSpec() != null) {
            node.getSortSpec().accept(this);
        }
        if (node.getQuery() != null) {
            node.getQuery().accept(this);
        }
        model.setQuery(node.toString());
    }

    @Override
    public void visit(Query node) {
        if (node.getPrefixAssignments() != null) {
            for (PrefixAssignment assignment : node.getPrefixAssignments()) {
                assignment.accept(this);
            }
        }
        if (node.getQuery() != null) {
            node.getQuery().accept(this);
        }
        if (node.getScopedClause() != null) {
            node.getScopedClause().accept(this);
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
    }

    @Override
    public void visit(PrefixAssignment node) {
        node.getPrefix().accept(this);
        node.getURI().accept(this);
        model.getNamespaceContext().addNamespace(node.getPrefix().getValue(), node.getURI().getValue());
    }

    @Override
    public void visit(ScopedClause node) {
        if (node.getScopedClause() != null) {
            node.getScopedClause().accept(this);
        }
        node.getSearchClause().accept(this);
        if (node.getBooleanGroup() != null) {
            node.getBooleanGroup().accept(this);
            BooleanOperator op = node.getBooleanGroup().getOperator();
            checkFilter(op, node);
            checkFilter(op, node.getScopedClause());
        }
    }

    @Override
    public void visit(BooleanGroup node) {
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
    }

    @Override
    public void visit(SearchClause node) {
        if (node.getQuery() != null) {
            node.getQuery().accept(this);
        }
        if (node.getTerm() != null) {
            node.getTerm().accept(this);
        }
        if (node.getIndex() != null) {
            node.getIndex().accept(this);
            String context = node.getIndex().getContext();
            if (CQLQueryModel.FACET_INDEX_NAME.equals(context)) {
                Facet<Term> facet = new Facet(node.getIndex().getName());
                facet.setValue(node.getTerm());
                model.addFacet(facet);
            } else if (CQLQueryModel.OPTION_INDEX_NAME.equals(context)) {
                Option<Term> option = new Option();
                option.setName(node.getIndex().getName());
                option.setValue(node.getTerm());
                model.addOption(option);
            }
        }
        if (node.getRelation() != null) {
            node.getRelation().accept(this);
        }
    }

    @Override
    public void visit(Relation node) {
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
    }

    @Override
    public void visit(Modifier node) {
        if (node.getTerm() != null) {
            node.getTerm().accept(this);
        }
        if (node.getName() != null) {
            node.getName().accept(this);
        }
    }

    @Override
    public void visit(ModifierList node) {
        for (Modifier modifier : node.getModifierList()) {
            modifier.accept(this);
        }
    }

    @Override
    public void visit(Term node) {
        if (replacementString != null && stringToBeReplaced.equals(node.getValue())) {
            node.setValue(replacementString);
        }
    }

    @Override
    public void visit(Identifier node) {
    }

    @Override
    public void visit(SimpleName node) {
    }

    @Override
    public void visit(Index node) {
        String context = node.getContext();
        if (context != null) {
            model.checkValidIndex(context, node.getName());
        }
    }

    /**
     * Write normalized CQL form.
     * @return the normalized form of the CQL query
     */
    @Override
    public String writeNormalizedForm() {
        return model.getQuery();
    }

    /**
     * Write a substitution query, for example when a term has been
     * suggested to be replaced by another term.
     *
     * @param oldTerm the term to be replaced
     * @param newTerm the replacement term
     * @return the new query with the term replaced
     */
    @Override
    public synchronized String writeSubstitutedForm(String oldTerm, String newTerm) {
        this.stringToBeReplaced = oldTerm;
        this.replacementString = newTerm;
        CQLParser parser = new CQLParser(new StringReader(model.getQuery()));
        parser.parse();
        parser.getCQLQuery().accept(this);
        String result = model.getQuery();
        this.stringToBeReplaced = null;
        this.replacementString = null;
        return result;
    }

    @Override
    public String writeWithBreadcrumbs() {
        return model.toCQL();
    }

    @Override
    public String writeWithoutBreadcrumbs() {
        return model.getQuery();
    }

    @Override
    public void write(BreadcrumbTrail trail, Writer writer) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void write(BreadcrumbModel model, Writer writer) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void checkFilter(BooleanOperator op, ScopedClause node) {
        if (node.getSearchClause().getIndex() != null
                && CQLQueryModel.FILTER_INDEX_NAME.equals(node.getSearchClause().getIndex().getContext())) {
            String filtername = node.getSearchClause().getIndex().getName();
            Comparitor filterop = node.getSearchClause().getRelation().getComparitor();
            Term filterterm = node.getSearchClause().getTerm();
            Filter<AbstractNode> filter2 = new Filter(filtername, filterterm, filterop);
            model.addFilter(op, filter2);
        }
    }
}
