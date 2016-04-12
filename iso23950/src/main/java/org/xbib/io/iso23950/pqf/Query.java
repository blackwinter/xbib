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
package org.xbib.io.iso23950.pqf;

import java.util.LinkedList;

/**
 * PQF abstract syntax tree
 *
 */
public class Query extends Node {

    private String attrschema;
    private LinkedList<AttrStr> attrspec = new LinkedList();
    private Query querystruct;
    private Setname setname;
    private Term term;
    private Expression expr;
    private PQF pqf;

    // ATTR CHARSTRING1 attrstr querystruct
    public Query(String attrschema, AttrStr attrspec, Query querystruct) {
        this.attrschema = attrschema;
        this.attrspec.add(attrspec);
        this.querystruct = querystruct;
        this.term = querystruct.getTerm();
        this.attrspec.addAll(querystruct.getAttrSpec());
    }

    // ATTR attrspec querystruct
    public Query(AttrStr attrspec, Query querystruct) {
        this.attrspec.add(attrspec);
        this.querystruct = querystruct;
        this.term = querystruct.getTerm();
        this.attrspec.addAll(querystruct.getAttrSpec());
    }

    // TERM TERMTYPE pqf
    public Query(PQF pqf) {
        this.pqf = pqf;
    }

    // simple
    public Query(Term term) {
        this.term = term;
    }

    // complex
    public Query(Expression expr) {
        this.expr = expr;
    }

    public Query(Setname setname) {
        this.setname = setname;
    }

    public void accept(Visitor visitor) {
        if (term != null) {
            term.accept(visitor);
        }
        if (setname != null) {
            setname.accept(visitor);
        }
        if (expr != null) {
            expr.accept(visitor);
        }
        if (querystruct != null) {
            querystruct.accept(visitor);
        }
        for (AttrStr attr : attrspec) {
            attr.accept(visitor);
        }
        if (pqf != null) {
            pqf.accept(visitor);
        }
        visitor.visit(this);
    }

    public String getSchema() {
        return attrschema;
    }

    public Setname getSetname() {
        return setname;
    }

    public Term getTerm() {
        return term;
    }

    public LinkedList<AttrStr> getAttrSpec() {
        return attrspec;
    }

    @Override
    public String toString() {
        return "[Query: term=" + term + " attrschema=" + attrschema + " setname=" + setname + " querystruct=" + querystruct + "]";
    }
}
