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
package org.xbib.oai.server.verb;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;

import org.xbib.oai.OAIConstants;
import org.xbib.oai.server.OAIServer;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.oai.server.ServerOAIRequest;
import org.xbib.oai.server.ServerOAIResponse;
import org.xbib.xml.XSI;

public abstract class AbstractVerb {

    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    protected final ServerOAIRequest request;

    protected final ServerOAIResponse response;
    
    public AbstractVerb(ServerOAIRequest request, ServerOAIResponse response) {
        this.request = request;
        this.response = response;
    }

    public abstract void execute(OAIServer adapter) throws OAIException;
    
    protected void beginDocument() throws XMLStreamException {
        response.getConsumer().add(eventFactory.createStartDocument());
    }
    
    protected void endDocument() throws XMLStreamException, IOException {
        response.getConsumer().add(eventFactory.createEndDocument());
    }
    
    protected void beginElement(String name) throws XMLStreamException {
        response.getConsumer().add(eventFactory.createStartElement(toQName(OAIConstants.NS_URI, name), null, null));
    }
    
    protected void endElement(String name) throws XMLStreamException {
        response.getConsumer().add(eventFactory.createEndElement(toQName(OAIConstants.NS_URI, name), null));
    }
   
    protected void element(String name, String value) throws XMLStreamException {
        response.getConsumer().add(eventFactory.createStartElement(toQName(OAIConstants.NS_URI, name), null, null));
        response.getConsumer().add(eventFactory.createCharacters(value));
        response.getConsumer().add(eventFactory.createEndElement(toQName(OAIConstants.NS_URI, name), null));
    }

    protected void element(String name, Date value) throws XMLStreamException {
        response.getConsumer().add(eventFactory.createStartElement(toQName(OAIConstants.NS_URI, name), null, null));
        response.getConsumer().add(eventFactory.createCharacters(formatDate(value)));
        response.getConsumer().add(eventFactory.createEndElement(toQName(OAIConstants.NS_URI, name), null));
    }

    protected void beginOAIPMH(URL baseURL) throws XMLStreamException {
        response.getConsumer().add(eventFactory.createStartElement(toQName(OAIConstants.NS_URI, "OAI-PMH"), null, null));
        response.getConsumer().add(eventFactory.createNamespace(OAIConstants.NS_URI));
        response.getConsumer().add(eventFactory.createNamespace(XSI.NS_PREFIX, XSI.NS_URI));
        response.getConsumer().add(eventFactory.createAttribute(XSI.NS_PREFIX, XSI.NS_URI,
                "schemaLocation", "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd"));
        element("responseDate", new Date());
        request(request.getParameterMap(), baseURL);
    }

    protected void endOAIPMH() throws XMLStreamException {
        response.getConsumer().add(eventFactory.createEndElement(toQName(OAIConstants.NS_URI, "OAI-PMH"), null));
    }
    
    protected void request(Map<String,List<String>> attrs, URL baseURL) throws XMLStreamException {
        response.getConsumer().add(eventFactory.createStartElement(toQName(OAIConstants.NS_URI, OAIConstants.REQUEST), null, null));
        for (Map.Entry<String,List<String>> me : attrs.entrySet()) {
            for (String value : me.getValue()) {
                response.getConsumer().add(eventFactory.createAttribute(me.getKey(),value));
            }
        }
        response.getConsumer().add(eventFactory.createCharacters(baseURL.toExternalForm()));
        response.getConsumer().add(eventFactory.createEndElement(toQName(OAIConstants.NS_URI, OAIConstants.REQUEST), null));
    }

    private QName toQName(String namespaceUri, String qname) {
        int i = qname.indexOf(':');
        if (i == -1) {
            return new QName(namespaceUri, qname);
        } else {
            String prefix = qname.substring(0, i);
            String localPart = qname.substring(i + 1);
            return new QName(namespaceUri, localPart, prefix);
        }
    }

    private final TimeZone tz = TimeZone.getTimeZone("GMT");
    
    private String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(tz);
        return format.format(date);
    }
}
