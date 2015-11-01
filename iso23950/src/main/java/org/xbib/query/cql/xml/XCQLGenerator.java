/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.query.cql.xml;

import org.xbib.query.cql.BooleanGroup;
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
import org.xbib.query.cql.model.CQLQueryModel;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.util.XMLEventConsumer;
import java.util.Arrays;

/**
 * XCQL is the CQL query language expressed in an XML form. XCQL is used
 * to echo a query in a search/retrieve or scan response. It is not used
 * in search/retrieve or scan requests. The XML schema, xcql.xsd, that
 * defines the elements used in XCQL. XCQL encodes the structure of
 * the CQL within 'searchClause' and 'triple' elements. Thus the parentheses
 * which record this information in CQL are not used, nor is there a need
 * for a left to right precedence rule as this is explicit in the XML.
 * The 'triple' element contains three elements. The first is the boolean
 * for the triple and the second and third positions are either
 * 'searchClause' or 'triple', wrapped in 'leftOperand' and 'rightOperand'
 * respectively. 'searchClause' may contain 'index', 'relation' and
 * 'term' elements. Each 'triple' or 'searchClause' element may contain
 * a 'prefixes' array which specifies the prefixes used for mapping
 * context sets to their URI identifiers from that point onwards.
 */
public class XCQLGenerator implements Visitor {

    private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private final XMLEventConsumer consumer;
    private final QName root;
    private boolean namespaceDeclNeeded;

    public XCQLGenerator(XMLEventConsumer consumer, QName root) {
        this.consumer = consumer;
        this.root = root;
        inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.FALSE);
        this.namespaceDeclNeeded = true;
    }

    @Override
    public void visit(SortedQuery node) {
        createStartElement(root, root.getNamespaceURI());
        if (node.getQuery() != null) {
            // the prefixes here are nasty
            if (node.getQuery().getPrefixAssignments().size() > 0) {
                createStartElement("prefixes", "http://www.loc.gov/zing/cql/xcql/");
                for (PrefixAssignment assignment : node.getQuery().getPrefixAssignments()) {
                    assignment.accept(this);
                }
                createEndElement("prefixes");
            }
            node.getQuery().accept(this);
        }
        /* we have to ignore the sort spec for XCQL */
        createEndElement(root);
    }

    @Override
    public void visit(Query node) {
        if (node.getQuery() != null) {
            node.getQuery().accept(this);
        }
        if (node.getScopedClause() != null) {
            node.getScopedClause().accept(this);
        }
    }

    @Override
    public void visit(PrefixAssignment node) {
        createStartElement("prefix");
        createStartElement("name");
        createData(node.getPrefix().toString());
        createEndElement("name");
        createStartElement("identifier");
        createData(node.getURI().getValue());
        createEndElement("identifier");
        createEndElement("prefix");
    }

    @Override
    public void visit(ScopedClause node) {
        if (node.getScopedClause() != null) {
            if (namespaceDeclNeeded) {
                createStartElement("triple", "http://www.loc.gov/zing/cql/xcql/");
                namespaceDeclNeeded = false;
            } else {
                createStartElement("triple");
            }
            if (node.getBooleanGroup() != null) {
                node.getBooleanGroup().accept(this);
            }
            createStartElement("leftOperand");
            node.getScopedClause().accept(this);
            createEndElement("leftOperand");
            createStartElement("rightOperand");
            node.getSearchClause().accept(this);
            createEndElement("rightOperand");
            createEndElement("triple");
        } else {
            node.getSearchClause().accept(this);
        }
    }

    @Override
    public void visit(BooleanGroup node) {
        createStartElement("boolean");
        createStartElement("value");
        createData(node.getOperator().toString());
        createEndElement("value");
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
        createEndElement("boolean");
    }

    @Override
    public void visit(SearchClause node) {
        if (node.getQuery() != null) {
            node.getQuery().accept(this);
        }
        if (node.getTerm() == null) {
            return; // query in parenthesis, just skip
        }
        if (namespaceDeclNeeded) {
            createStartElement("searchClause", "http://www.loc.gov/zing/cql/xcql/");
            namespaceDeclNeeded = false;
        } else {
            createStartElement("searchClause");
        }
        if (node.getIndex() != null) {
            node.getIndex().accept(this);
        } else {
            // default index
            createStartElement("index");
            createData("cql.anyIndexes");
            createEndElement("index");
        }
        if (node.getRelation() != null) {
            node.getRelation().accept(this);
        } else {
            // default relation
            createStartElement("relation");
            createStartElement("value");
            createData("=");
            createEndElement("value");
            createEndElement("relation");
        }
        node.getTerm().accept(this);
        createEndElement("searchClause");
    }

    @Override
    public void visit(Relation node) {
        createStartElement("relation");
        createStartElement("value");
        createData(node.getComparitor().toString());
        createEndElement("value");
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
        createEndElement("relation");
    }

    @Override
    public void visit(Modifier node) {
        createStartElement("modifier");
        createStartElement("type");
        createData(node.getName().getName());
        createEndElement("type");
        if (node.getOperator() != null) {
            createStartElement("comparison");
            createData(node.getOperator().toString());
            createEndElement("comparison");
        }
        if (node.getTerm() != null) {
            createStartElement("value");
            createData(node.getTerm().getValue());
            createEndElement("value");
        }
        createEndElement("modifier");
    }

    @Override
    public void visit(ModifierList node) {
        createStartElement("modifiers");
        for (Modifier modifier : node.getModifierList()) {
            modifier.accept(this);
        }
        createEndElement("modifiers");
    }

    @Override
    public void visit(Term node) {
        createStartElement("term");
        createData(node.getValue());
        createEndElement("term");
    }

    @Override
    public void visit(Identifier node) {
        createStartElement("identifier");
        createData(node.getValue());
        createEndElement("identifier");
    }

    @Override
    public void visit(Index node) {
        String context = node.getContext();
        if (CQLQueryModel.isVisible(context)) {
            createStartElement("index");
            String s = context != null
                    ? context + "." + node.getName() : node.getName();
            this.createData(s);
            createEndElement("index");
        }
    }

    @Override
    public void visit(SimpleName node) {
        createStartElement("name");
        createData(node.getName());
        createEndElement("name");
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

    private void createStartElement(String name) {
        createStartElement(new QName(name));
    }

    private void createStartElement(QName name) {
        try {
            consumer.add(eventFactory.createStartElement(name, null, null));
        } catch (XMLStreamException ex) {
            throw new RuntimeException("error while creating start element '" + name + "'");
        }
    }

    private void createStartElement(String name, String xmlnsURI) {
        createStartElement(new QName(name), xmlnsURI);
    }

    private void createStartElement(QName name, String xmlnsURI) {
        try {
            Attribute attr = eventFactory.createAttribute("xmlns", xmlnsURI);
            consumer.add(eventFactory.createStartElement(name, Arrays.asList(attr).iterator(), null));
        } catch (XMLStreamException ex) {
            throw new RuntimeException("error while creating start element '" + name + "'");
        }
    }

    private void createData(String value) {
        try {
            consumer.add(eventFactory.createCharacters(escape(value)));
        } catch (XMLStreamException ex) {
            throw new RuntimeException("error while creating character data '" + value + "'");
        }
    }

    private void createEndElement(String name) {
        createEndElement(new QName(name));
    }
    private void createEndElement(QName name) {
        try {
            consumer.add(eventFactory.createEndElement(name, null));
        } catch (XMLStreamException ex) {
            throw new RuntimeException("error while creating end element '" + name + "'");
        }
    }

    /**
     * Replace special characters with XML escapes:
     * <pre>
     * &amp; <small>(ampersand)</small> is replaced by &amp;amp;
     *
     * &lt; <small>(less than)</small> is replaced by &amp;lt;
     * &gt; <small>(greater than)</small> is replaced by &amp;gt;
     * &quot; <small>(double quote)</small> is replaced by &amp;quot;
     * </pre>
     *
     * @param string The string to be escaped.
     * @return The escaped string.
     */
    private String escape(String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = string.length(); i < len; i++) {
            char c = string.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
