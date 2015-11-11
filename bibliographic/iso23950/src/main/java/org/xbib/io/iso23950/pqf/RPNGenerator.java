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

import asn1.ASN1Any;
import asn1.ASN1Integer;
import asn1.ASN1Null;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1OctetString;
import java.util.Stack;
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
 * PQF abstract syntax tree
 *
 */
public class RPNGenerator implements Visitor {

    private Stack<ASN1Any> result;
    private RPNQuery rpnQuery;

    public RPNGenerator() {
        this.result = new Stack();
    }

    public RPNQuery getResult() {
        return rpnQuery;
    }

    @Override
    public void visit(PQF pqf) {
        if (!result.isEmpty()) {
            this.rpnQuery = new RPNQuery();
            rpnQuery.s_rpn = (RPNStructure) result.pop();
            if (pqf.getAttrSet() == null) {
                // Z39.50 BIB-1: urn:oid:1.2.840.10003.3.1
                rpnQuery.s_attributeSet = new AttributeSetId();
                rpnQuery.s_attributeSet.value = new ASN1ObjectIdentifier(new int[]{1, 2, 840, 10003, 3, 1});
            } else {
                // TODO: known attr set and their OIDs
            }
        } else {
            throw new SyntaxException("no valid PQF found");
        }
    }

    @Override
    public void visit(Query query) {
        Operand operand = new Operand();
        operand.c_attrTerm = new AttributesPlusTerm();
        operand.c_attrTerm.s_term = new z3950.v3.Term();
        operand.c_attrTerm.s_term.c_general = new ASN1OctetString(query.getTerm().getValue());
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
        if (attrs.size() > 0) {
            result.push(rpn);
        }
    }

    @Override
    public void visit(Expression expr) {
        String op = expr.getOperator();
        RPNStructure rpn = new RPNStructure();
        rpn.c_rpnRpnOp = new RPNStructure_rpnRpnOp();
        rpn.c_rpnRpnOp.s_op = new Operator();
        if ("@and".equals(op)) {
            rpn.c_rpnRpnOp.s_op.c_and = new ASN1Null();
        }
        if ("@or".equals(op)) {
            rpn.c_rpnRpnOp.s_op.c_or = new ASN1Null();
        }
        if ("@not".equals(op)) {
            rpn.c_rpnRpnOp.s_op.c_and_not = new ASN1Null();
        }
        rpn.c_rpnRpnOp.s_rpn1 = (RPNStructure) result.pop();
        rpn.c_rpnRpnOp.s_rpn2 = (RPNStructure) result.pop();
        result.push(rpn);
    }

    @Override
    public void visit(AttrStr attrspec) {
        AttributeElement ae = new AttributeElement();
        ae.s_attributeType = (ASN1Integer) result.pop();
        ae.s_attributeValue = new AttributeElement_attributeValue();
        ae.s_attributeValue.c_numeric = (ASN1Integer) result.pop();
        result.push(ae);
    }

    @Override
    public void visit(Term term) {
        result.push(new ASN1OctetString(term.getValue()));
    }

    @Override
    public void visit(Setname name) {
        result.push(new ASN1OctetString(name.getValue()));
    }

    @Override
    public void visit(Integer i) {
        result.push(new ASN1Integer(i));
    }

    @Override
    public void visit(String str) {
        result.push(new ASN1OctetString(str));
    }
}
