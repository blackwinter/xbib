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
package org.xbib.rdf.content;

import org.xbib.rdf.RdfContent;
import org.xbib.rdf.RdfContentType;
import org.xbib.rdf.io.ntriple.NTripleContent;
import org.xbib.rdf.io.rdfxml.RdfXmlContent;
import org.xbib.rdf.io.turtle.TurtleContent;
import org.xbib.rdf.io.xml.XmlContent;

public enum ExtendedRdfContentType implements RdfContentType {

    NTRIPLE(0) {
        @Override
        public String contentType() {
            return "application/n-triples";
        }

        @Override
        public String shortName() {
            return "n-triples";
        }

        @Override
        public RdfContent rdfContent() {
            return NTripleContent.nTripleContent;
        }
    },

    RDXFXML(1) {
        @Override
        public String contentType() {
            return "application/rdf+xml";
        }

        @Override
        public String shortName() {
            return "rdf/xml";
        }

        @Override
        public RdfContent rdfContent() {
            return RdfXmlContent.rdfXmlContent;
        }
    },

    TURTLE(2) {
        @Override
        public String contentType() {
            return "text/turtle";
        }

        @Override
        public String shortName() {
            return "ttl";
        }

        @Override
        public RdfContent rdfContent() {
            return TurtleContent.turtleContent;
        }
    },

    XML(3) {
        @Override
        public String contentType() {
            return "text/xml";
        }

        @Override
        public String shortName() {
            return "xml";
        }

        @Override
        public RdfContent rdfContent() {
            return XmlContent.xmlContent;
        }
    },

    XCONTENT(4) {
        @Override
        public String contentType() {
            return "application/x-content";
        }

        @Override
        public String shortName() {
            return "xcontent";
        }

        @Override
        public RdfContent rdfContent() {
            return RdfXContent.rdfXContent;
        }
    },

    ROUTEXCONTENT(5) {
        @Override
        public String contentType() {
            return "application/x-content-route";
        }

        @Override
        public String shortName() {
            return "xcontent-route";
        }

        @Override
        public RdfContent rdfContent() {
            return RouteRdfXContent.routeRdfXContent;
        }
    };

    private int index;

    ExtendedRdfContentType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public abstract String contentType();

    public abstract String shortName();

    public abstract RdfContent rdfContent();
}
