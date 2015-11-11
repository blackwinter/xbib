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
package org.xbib.io.iso23950.cql;

import asn1.ASN1Any;
import asn1.ASN1Integer;
import asn1.ASN1Null;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1OctetString;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;
import org.xbib.query.cql.BooleanGroup;
import org.xbib.query.cql.BooleanOperator;
import org.xbib.query.cql.Identifier;
import org.xbib.query.cql.Index;
import org.xbib.query.cql.Modifier;
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
import org.xbib.query.cql.Term;
import org.xbib.query.cql.Visitor;
import z3950.v3.AttributeElement;
import z3950.v3.AttributeElement_attributeValue;
import z3950.v3.AttributeList;
import z3950.v3.AttributeSetId;
import z3950.v3.AttributesPlusTerm;
import z3950.v3.Operand;
import z3950.v3.Operator;
import z3950.v3.RPNQuery;
import z3950.v3.RPNStructure;
import z3950.v3.RPNStructure_rpnRpnOp;

/**
 * This is a RPN (Type-1 query) generator for CQL queries.
 *
 * @see <a href="http://www.loc.gov/z3950/agency/markup/09.html">RPN</a>
 * 
 */
public final class RPNGenerator implements Visitor {

    /** BIB-1 Use attributes ResourceBundle */
    private static final ResourceBundle bib =
            ResourceBundle.getBundle("org.xbib.io.iso23950.cql.bib-1");
    /** Dublin Core Use attributes ResourceBundle */
    private static final ResourceBundle dc =
            ResourceBundle.getBundle("org.xbib.io.iso23950.cql.dc");
    /** a context map */
    private final Map<String, ResourceBundle> contexts = new HashMap() {{
        put("bib", bib);
        put("dc", dc);
    }};
    private Stack<ASN1Any> result;
    private RPNQuery rpnQuery;

    public RPNGenerator() {
        this.result = new Stack();
    }

    public RPNQuery getQueryResult() {
        return rpnQuery;
    }

    @Override
    public void visit(SortedQuery node) {
        if (node.getSortSpec() != null) {
            node.getSortSpec().accept(this);
        }
        if (node.getQuery() != null) {
            node.getQuery().accept(this);
        }
        if (!result.isEmpty()) {
            this.rpnQuery = new RPNQuery();
            rpnQuery.s_rpn = (RPNStructure) result.pop();
            // Z39.50 BIB-1: urn:oid:1.2.840.10003.3.1
            rpnQuery.s_attributeSet = new AttributeSetId();
            rpnQuery.s_attributeSet.value = new ASN1ObjectIdentifier(new int[]{1, 2, 840, 10003, 3, 1});
        } else {
            throw new SyntaxException("unable to generate RPN from CQL");
        }
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
    }

    @Override
    public void visit(ScopedClause node) {
        if (node.getScopedClause() != null) {
            node.getScopedClause().accept(this);
        }
        node.getSearchClause().accept(this);
        if (node.getBooleanGroup() != null) {
            node.getBooleanGroup().accept(this);
            RPNStructure rpn = new RPNStructure();
            rpn.c_rpnRpnOp = new RPNStructure_rpnRpnOp();
            rpn.c_rpnRpnOp.s_op = new Operator();
            BooleanOperator op = node.getBooleanGroup().getOperator();
            switch (op) {
                case AND:
                    rpn.c_rpnRpnOp.s_op.c_and = new ASN1Null();
                    break;
                case OR:
                    rpn.c_rpnRpnOp.s_op.c_or = new ASN1Null();
                    break;
                case NOT:
                    rpn.c_rpnRpnOp.s_op.c_and_not = new ASN1Null();
            }
            rpn.c_rpnRpnOp.s_rpn1 = (RPNStructure) result.pop();
            rpn.c_rpnRpnOp.s_rpn2 = (RPNStructure) result.pop();
            result.push(rpn);
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
        }
        if (node.getRelation() != null) {
            node.getRelation().accept(this);
        }
        Operand operand = new Operand();
        operand.c_attrTerm = new AttributesPlusTerm();
        operand.c_attrTerm.s_term = new z3950.v3.Term();
        operand.c_attrTerm.s_term.c_general = new ASN1OctetString(node.getTerm().getValue());
        Stack<AttributeElement> attrs = new Stack();
        ASN1Any any = !result.isEmpty() && result.peek() instanceof AttributeElement ? result.pop() : null;
        while (any != null) {
            attrs.push((AttributeElement) any);
            any = !result.isEmpty() && result.peek() instanceof AttributeElement ? result.pop() : null;
        }
        operand.c_attrTerm.s_attributes = new AttributeList();
        operand.c_attrTerm.s_attributes.value = attrs.toArray(new AttributeElement[attrs.size()]);
        RPNStructure rpn = new RPNStructure();
        rpn.c_op = operand;
        result.push(rpn);
    }

    @Override
    public void visit(Relation node) {
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
        int t = 2;
        int n = 3;
        switch (node.getComparitor()) {
            case LESS: // 2=1
                n = 1;
                break;
            case LESS_EQUALS: // 2=2
                n = 2;
                break;
            case EQUALS: // 2=3
                n = 3;
                break;
            case GREATER_EQUALS: // 2=4
                n = 4;
                break;
            case GREATER: // 2=5    
                n = 5;
                break;
            case NOT_EQUALS: // 2=6
                n = 6;
                break;
            case ALL: // 4=6
                t = 4;
                n = 6;
                break;
            case ANY: // 4=105
                t = 4;
                n = 104;
                break;
        }
        if (n != 3) {
            AttributeElement ae = new AttributeElement();
            ae.s_attributeType = new ASN1Integer(t);
            ae.s_attributeValue = new AttributeElement_attributeValue();
            ae.s_attributeValue.c_numeric = new ASN1Integer(n);
            result.push(ae);
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
        int t = 5;
        int n = 100;
        // check for '*'
        String v = node.getValue();
        if (v.endsWith("*")) {
            if (v.startsWith("*")) {
                n = 3;
            } else {
                n = 1;
            }
        } else if (v.startsWith("*")) {
            n = 2;
        }
        if (n != 100) {
            AttributeElement ae = new AttributeElement();
            ae.s_attributeType = new ASN1Integer(t);
            ae.s_attributeValue = new AttributeElement_attributeValue();
            ae.s_attributeValue.c_numeric = new ASN1Integer(n);
            result.push(ae);
            v = v.replaceAll("\\*", "");
        }
        ASN1OctetString s = new ASN1OctetString(v);
        result.push(s);
    }

    @Override
    public void visit(Identifier node) {
    }

    @Override
    public void visit(SimpleName node) {
        ASN1OctetString s = new ASN1OctetString(node.getName());
        result.push(s);
    }

    @Override
    public void visit(Index node) {
        String context = node.getContext();
        if (context == null) {
            context = "dc"; // default context
        }
        int t = 1;
        int n = getUseAttr(context, node.getName());
        AttributeElement ae = new AttributeElement();
        ae.s_attributeType = new ASN1Integer(t);
        ae.s_attributeValue = new AttributeElement_attributeValue();
        ae.s_attributeValue.c_numeric = new ASN1Integer(n);
        result.push(ae);        
    }

    private int getUseAttr(String context, String attrName) {
        try {
            return Integer.parseInt(contexts.get(context).getString(attrName));
        } catch (MissingResourceException e) {
            throw new SyntaxException("unknown use attribute '" + attrName + "' for context " + context, e);
        }
    }
}
