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
package org.xbib.common.xcontent.xml;

import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.xbib.xml.namespace.XmlNamespaceContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

/**
 * XML parameters for XML XContent
 */
public class XmlXParams {

    private final static XmlFactory defaultXmlFactory = createXmlFactory(createXMLInputFactory(), createXMLOutputFactory());

    private final static QName defaultRoot = new QName("root");

    private final static XmlXParams DEFAULT_PARAMS =
            new XmlXParams(defaultRoot, XmlNamespaceContext.newInstance(), defaultXmlFactory);

    private final XmlNamespaceContext namespaceContext;

    private XmlFactory xmlFactory;

    private QName root;

    private boolean fatalNamespaceErrors;

    public XmlXParams() {
        this(null, null, null);
    }

    public XmlXParams(QName root) {
        this(root, null, null);
    }

    public XmlXParams(XmlNamespaceContext namespaceContext) {
        this(null, namespaceContext, null);
    }

    public XmlXParams(QName root, XmlNamespaceContext namespaceContext) {
        this(root, namespaceContext, null);
    }

    public XmlXParams(QName root, XmlNamespaceContext namespaceContext, XmlFactory xmlFactory) {
        this.namespaceContext = namespaceContext == null ? DEFAULT_PARAMS.getNamespaceContext() : namespaceContext;
        this.xmlFactory = xmlFactory == null ? defaultXmlFactory : xmlFactory;
        this.root = root;
        if (root == null ) {
            this.root = defaultRoot;
        }
        String prefix = this.root.getPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            this.namespaceContext.addNamespace(prefix, this.root.getNamespaceURI());
        }
    }

    public XmlNamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    public XmlFactory getXmlFactory() {
        return xmlFactory;
    }

    public QName getRoot() {
        return root;
    }

    public static XmlXParams getDefaultParams() {
        return DEFAULT_PARAMS;
    }

    public XmlXParams setFatalNamespaceErrors() {
        this.fatalNamespaceErrors = true;
        return this;
    }

    public boolean isFatalNamespaceErrors() {
        return fatalNamespaceErrors;
    }

    protected static XMLInputFactory createXMLInputFactory() {
        // load from service factories in META-INF/services
        // default impl is "com.sun.xml.internal.stream.XMLInputFactoryImpl"
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        try {
            inputFactory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
            inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
            inputFactory.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
            inputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE);
            inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        } catch (Exception e) {
            e.printStackTrace(); // we don't have a logger
        }
        return inputFactory;
    }

    protected static XMLOutputFactory createXMLOutputFactory() {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        try {
            outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputFactory;
    }

    protected static XmlFactory createXmlFactory(XMLInputFactory inputFactory, XMLOutputFactory outputFactory) {
        XmlFactory xmlFactory = new XmlFactory(inputFactory, outputFactory);
        try {
            xmlFactory.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlFactory;
    }
}
