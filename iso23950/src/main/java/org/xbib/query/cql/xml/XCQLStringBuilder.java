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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import java.util.Iterator;

public class XCQLStringBuilder implements XMLEventConsumer {

    private StringBuilder sb = new StringBuilder();

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;
            QName qname = element.getName();
            sb.append('<');
            if (qname.getPrefix().length() > 0) {
                sb.append(qname.getPrefix()).append(':');
            }
            sb.append(qname.getLocalPart());
            Iterator iterator = element.getAttributes();
            while (iterator.hasNext()) {
                Attribute attribute = (Attribute) iterator.next();
                sb.append(' ').append(attribute.getName()).append('=').append('\"').append(attribute.getValue()).append('\"');
            }
            sb.append('>');
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            QName qname = element.getName();
            sb.append('<').append('/');
            if (qname.getPrefix().length() > 0) {
                sb.append(qname.getPrefix()).append(':');
            }
            sb.append(qname.getLocalPart()).append('>');
        } else if (event.isCharacters()) {
            Characters characters = (Characters) event;
            sb.append(characters.getData());
        }
    }

    public String getResult() {
        return sb.toString();
    }
}
