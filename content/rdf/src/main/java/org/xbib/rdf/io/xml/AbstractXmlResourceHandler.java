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
package org.xbib.rdf.io.xml;

import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.Resource;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Stack;

/**
 * The XML resource handler can create nested RDF resources from arbitrary XML.
 */
public abstract class AbstractXmlResourceHandler
        extends AbstractXmlHandler implements XmlResourceHandler {

    protected final Stack<Resource> stack = new Stack<>();

    public AbstractXmlResourceHandler(RdfContentParams params) {
        super(params);
    }

    @Override
    public void openResource() throws IOException {
        super.openResource();
        stack.push(getResource());
    }

    @Override
    public void closeResource() throws IOException {
        super.closeResource();
        stack.clear();
    }

    /**
     * Open a predicate. Create new resource, even if there will be only a single literal.
     * It will be compacted later.
     *
     * @param parent the parent
     * @param name   the name
     * @param level  the level
     */
    @Override
    public void openPredicate(QName parent, QName name, int level) {
        String elementName = makePrefix(name.getPrefix(), name.getLocalPart());
        IRI p = toProperty(getResource().newPredicate(elementName));
        stack.push(stack.peek().newResource(p));
    }

    @Override
    public void addToPredicate(QName parent, String content) {
    }

    @Override
    public void closePredicate(QName parent, QName name, int level) {
        Resource r = stack.pop();
        String elementName = makePrefix(name.getPrefix(), name.getLocalPart());
        if (level < 0) {
            // it's a resource
            if (!stack.isEmpty()) {
                // avoid empty resource
                if (!r.isEmpty()) {
                    IRI p = toProperty(getResource().newPredicate(elementName));
                    stack.peek().add(p, r);
                }
            }
        } else {
            // it's a property with object
            String s = content();
            if (s != null) {
                if (!stack.isEmpty()) {
                    IRI p = toProperty(getResource().newPredicate(elementName));
                    Object o = getResource().newObject(toObject(name, s));
                    if (o instanceof Literal) {
                        r.add(p, (Literal) o);
                    } else if (o instanceof Resource) {
                        Resource resource = (Resource) o;
                        if (!resource.isEmpty()) {
                            r.add(p, resource);
                        }
                    }
                    // compact predicate because it has only a single value
                    stack.peek().compactPredicate(p);
                    // optional rename. This can help if OAI source
                    // emits both string.object under same element name which leads
                    // to ElasticsearchIllegalArgumentException "unknown property"
                    if (o instanceof Literal) {
                        String newElementName = toElementName(elementName);
                        if (!elementName.equals(newElementName)) {
                            stack.peek().rename(elementName, newElementName);
                        }
                    }
                }
            }
        }
    }

    public IRI toProperty(IRI property) {
        return property;
    }

    public String toElementName(String elementName) {
        return elementName;
    }

    @Override
    public Object toObject(QName name, String content) {
        return content;
    }
}
