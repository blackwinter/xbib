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
package org.xbib.sru.explain;

import org.xbib.sru.SRUConstants;
import org.xbib.xml.XMLFilterReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A filter reader for Explain
 *
 */
public class ExplainFilterReader extends XMLFilterReader {

    private ExplainRequest explain;
    private ExplainResponse response;
    private String content;

    public ExplainFilterReader(ExplainRequest explain, ExplainResponse response) {
        this.explain = explain;
        this.response = response;
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localname, String qname, Attributes atts)
            throws SAXException {
        if (ZEEREX.NS_URI.equals(uri) && localname.equals("serverInfo")) {
            ServerInfo serverInfo = new ServerInfo();
            for (int i = 0; i < atts.getLength(); i++) {
                String attrName = atts.getQName(i);
                if (attrName.equals("protocol")) {
                    serverInfo.setProtocol(atts.getValue(i));

                } else if (attrName.equals("version")) {
                    serverInfo.setVersion(atts.getValue(i));

                } else if (attrName.equals("transport")) {
                    serverInfo.setTransport(atts.getValue(i));

                } else if (attrName.equals("method")) {
                    serverInfo.setMethod(atts.getValue(i));

                }
            }
            explain.setServerInfo(serverInfo);
        }
    }

    @Override
    public void endElement(String uri, String localname, String qname) throws SAXException {
        if (SRUConstants.NS_URI.equals(uri) && localname.equals("version")) {
            explain.setVersion(content);
        }
        if (ZEEREX.NS_URI.equals(uri)) {
            if (localname.equals("host")) {
                explain.getServerInfo().setHost(content);
            }
            if (localname.equals("port")) {
                explain.getServerInfo().setPort(Integer.parseInt(content));
            }
            if (localname.equals("database")) {
                explain.getServerInfo().setDatabase(content);
            }
        }
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        this.content = new String(chars, start, length);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }
}
