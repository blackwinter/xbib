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
package org.xbib.tools.feed.elasticsearch.ezb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.index.VersionType;
import org.xbib.util.InputService;
import org.xbib.iri.IRI;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.content.RdfXContentParams;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.xml.XmlContentParser;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.tools.TimewindowFeeder;
import org.xbib.util.URIUtil;
import org.xbib.util.concurrent.WorkerProvider;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer for "Elektronische Zeitschriftenbibliothek" (EZB)
 * Format documentation:
 * http://www.zeitschriftendatenbank.de/fileadmin/user_upload/ZDB/pdf/services/Datenlieferdienst_ZDB_EZB_Lizenzdatenformat.pdf
 */
public final class EZBXML extends TimewindowFeeder {

    private final static Logger logger = LogManager.getLogger(EZBXML.class.getSimpleName());

    private final IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    @Override
    protected WorkerProvider provider() {
        return p -> new EZBXML().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        RdfContentParams params = new RdfXContentParams(namespaceContext);
        InputStream in = InputService.getInputStream(uri);
        Pattern pattern = Pattern.compile("(\\d{4,})");
        Matcher matcher = pattern.matcher(uri.toString());
        Long version = Long.parseLong(matcher.find() ? matcher.group(1) : "1");
        logger.info("version of {} = {}", uri, version);
        EZBHandler handler = new EZBHandler(params, version);
        handler.setDefaultNamespace("ezb", "http://ezb.uni-regensburg.de/ezeit/");
        new XmlContentParser(in)
                .setNamespaces(false)
                .setHandler(handler)
                .parse();
        in.close();
    }

    @Override
    public EZBXML cleanup() throws IOException {
        if (settings.getAsBoolean("aliases", false) && !settings.getAsBoolean("mock", false) && ingest != null && ingest.client() != null) {
            updateAliases();
        } else {
            logger.info("not doing alias settings");
        }
        if (ingest != null) {
            ingest.stopBulk(getConcreteIndex());
        }
        super.cleanup();
        return this;
    }

    class EZBHandler extends AbstractXmlResourceHandler {

        private String id;

        private long version;

        public EZBHandler(RdfContentParams params, long version) {
            super(params);
            this.version = version;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            if ("license_entry_id".equals(name.getLocalPart()) && identifier == null) {
                this.id = value;
                if (settings.get("identifier") != null) {
                    this.id = "(" + settings.get("identifier") + ")" + value;
                }
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "license_set".equals(name.getLocalPart());
        }

        @Override
        public void closeResource() throws IOException {
            super.closeResource();
            if (settings.get("collection") != null) {
                getResource().add("collection", settings.get("collection"));
            }
            RouteRdfXContentParams params = new RouteRdfXContentParams(getNamespaceContext(),
                    getConcreteIndex(), getType());
            params.setHandler((content, p) -> {
                if (ingest.client() != null) {
                    IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(ingest.client())
                            .setIndex(p.getIndex())
                            .setType(p.getType())
                            .setId(id)
                            .setVersionType(VersionType.EXTERNAL)
                            .setVersion(version)
                            .setSource(content);
                    ingest.bulkIndex(indexRequestBuilder.request());
                }
            });
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("{}", builder.string());
            }
        }

        @Override
        public boolean skip(QName name) {
            return "ezb-export".equals(name.getLocalPart())
                    || "release".equals(name.getLocalPart())
                    || "version".equals(name.getLocalPart())
                    || name.getLocalPart().startsWith("@");
        }

        @Override
        public Object toObject(QName name, String content) {
            switch (name.getLocalPart()) {
                case "reference_url":
                    // fall-through
                case "readme_url":
                    return URIUtil.decode(content, Charset.forName("UTF-8"));
                case "zdbid": {
                    return content.replaceAll("\\-", "").toLowerCase();
                }
                case "type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1:
                            return "online"; //"Volltext nur online";
                        case 2:
                            return "online-and-print"; //"Volltext online und Druckausgabe";
                        case 9:
                            return "self-hosted"; //"lokale Zeitschrift";
                        case 11:
                            return "digitalization"; //"retrodigitalisiert";
                        default:
                            throw new IllegalArgumentException("unknown type_id: " + content);
                    }
                }
                case "license_type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1:
                            return "solitary"; // "Einzellizenz";
                        case 2:
                            return "consortial"; //"Konsortiallizenz";
                        case 4:
                            return "national"; // "Nationallizenz";
                        default:
                            throw new IllegalArgumentException("unknown license_type_id: " + content);
                    }
                }
                case "price_type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1:
                            return "no"; //"lizenzfrei";
                        case 2:
                            return "no-with-print"; //"Kostenlos mit Druckausgabe";
                        case 3:
                            return "yes"; //"Kostenpflichtig";
                        default:
                            throw new IllegalArgumentException("unknown price_type_id: " + content);
                    }
                }
                case "ill_code": {
                    switch (content) {
                        case "n":
                            return "no"; // "nein";
                        case "l":
                            return "copy-loan"; //"ja, Leihe und Kopie";
                        case "k":
                            return "copy"; //"ja, nur Kopie";
                        case "e":
                            return "copy-electronic";  //"ja, auch elektronischer Versand an Nutzer";
                        case "ln":
                            return "copy-loan-domestic";  //"ja, Leihe und Kopie (nur Inland)";
                        case "kn":
                            return "copy-domestic";  //"ja, nur Kopie (nur Inland)";
                        case "en":
                            return "copy-electronic-domestic";  //"ja, auch elektronischer Versand an Nutzer (nur Inland)";
                        default:
                            throw new IllegalArgumentException("unknown ill_code: " + content);
                    }
                }
            }
            return super.toObject(name, content);
        }

        @Override
        public XmlHandler setNamespaceContext(IRINamespaceContext namespaceContext) {
            return this;
        }

        @Override
        public IRINamespaceContext getNamespaceContext() {
            return namespaceContext;
        }
    }

}
