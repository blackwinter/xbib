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
package org.xbib.tools.convert.ezb;

import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.io.turtle.TurtleContentParams;
import org.xbib.rdf.io.xml.XmlContentParser;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.tools.Converter;
import org.xbib.util.URIUtil;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

/**
 * Converter for "Elektronische Zeitschriftenbibliothek" (EZB)
 * <p>
 * Format documentation
 * <p>
 * http://www.zeitschriftendatenbank.de/fileadmin/user_upload/ZDB/pdf/services/Datenlieferdienst_ZDB_EZB_Lizenzdatenformat.pdf
 */
public class EZBXML extends Converter {

    @Override
    public String getName() {
        return "ezbxml-turtle";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new EZBXML();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        IRINamespaceContext namespaceContext = IRINamespaceContext.getInstance();
        FileOutputStream out = new FileOutputStream(settings.get("output"));
        TurtleContentParams params = new TurtleContentParams(namespaceContext, true);
        RdfContentBuilder builder = turtleBuilder(out, params);

        AbstractXmlHandler handler = new EZBHandler(params, builder)
                .setDefaultNamespace("ezb", "http://ezb.uni-regensburg.de/ezeit/");

        try (InputStream in = InputService.getInputStream(uri)) {
            new XmlContentParser(in).setNamespaces(false)
                    .setHandler(handler)
                    .parse();
        } finally {
            builder.close();
            out.close();
        }
    }

    class EZBHandler extends AbstractXmlResourceHandler {

        //ResourceSerializer resourceSerializer;

        public EZBHandler(RdfContentParams params, RdfContentBuilder builder) {
            super(params);
            setBuilder(builder);
            //setBuilder(resourceSerializer);
            //this.resourceSerializer = resourceSerializer;
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            if ("license_entry_id".equals(name.getLocalPart()) && identifier == null) {
                IRI id = IRI.builder().scheme("iri")
                        .host(settings.get("index"))
                        .query(settings.get("type"))
                        .fragment(value)
                        .build();
                getResource().id(id);
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "license_set".equals(name.getLocalPart());
        }

        @Override
        public void closeResource() throws IOException {
            // attach closeResource to output write
            /*try {
                if (resourceContext().getResource() != null) {
                    resourceSerializer.write(resourceContext().getResource());
                } else {
                    logger.warn("no resource to output");
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }*/
            super.closeResource();
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
                            return "full-text-online"; //"Volltext nur online";
                        case 2:
                            return "full-text-online-and-print"; //"Volltext online und Druckausgabe";
                        case 9:
                            return "local"; //"lokale Zeitschrift";
                        case 11:
                            return "digitized"; //"retrodigitalisiert";
                        default:
                            throw new IllegalArgumentException("unknown type_id: " + content);
                    }
                }
                case "license_type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1:
                            return "local-license"; // "Einzellizenz";
                        case 2:
                            return "consortia-license"; //"Konsortiallizenz";
                        case 4:
                            return "supra-regional-license"; // "Nationallizenz";
                        default:
                            throw new IllegalArgumentException("unknown license_type_id: " + content);
                    }
                }
                case "price_type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1:
                            return "no-fee"; //"lizenzfrei";
                        case 2:
                            return "no-fee-included-in-print"; //"Kostenlos mit Druckausgabe";
                        case 3:
                            return "fee"; //"Kostenpflichtig";
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
            return null;
        }

        @Override
        public IRINamespaceContext getNamespaceContext() {
            return getParams().getNamespaceContext();
        }
    }

    /*private class ResourceBuilder implements TripleListener {

        @Override
        public TripleListener begin() {
            return this;
        }

        @Override
        public TripleListener startPrefixMapping(String prefix, String uri) {
            return this;
        }

        @Override
        public TripleListener endPrefixMapping(String prefix) {
            return this;
        }

        @Override
        public ResourceBuilder newIdentifier(IRI identifier) {
            resourceContext.getResource().id(identifier);
            return this;
        }

        @Override
        public ResourceBuilder triple(Triple triple) {
            resourceContext.getResource().add(triple);
            return this;
        }

        @Override
        public TripleListener end() {
            return this;
        }
    }*/

    /*class ElasticOut extends RdfOutput {
        @Override
        public RdfOutput output(ResourceContext<Resource> resourceContext) throws IOException {
            if (resourceContext == null) {
                return this;
            }
             if (resourceContext.getResources() != null) {
                for (Resource resource : resourceContext.getResources()) {
                    // multiple documents. Rewrite IRI for ES index/type addressing
                    String index = settings.get("index", "ezb");
                    String type = settings.get("type", "ezb");
                    if (index.equals(resource.id().getHost())) {
                        IRI iri = IRI.builder().scheme("http").host(index).query(type)
                                .fragment(resource.id().getFragment()).complete();
                        resource.add("iri", resource.id().getFragment());
                        resource.id(iri);
                    } else {
                        IRI iri = IRI.builder().scheme("http").host(index).query(type)
                                .fragment(resource.id().toString()).complete();
                        resource.add("iri", resource.id().toString());
                        resource.id(iri);
                    }
                    sink.output(resourceContext, resource, resourceContext.getContentBuilder());
                }
            } else if (resourceContext.getResource() != null) {
                // single document
                sink.output(resourceContext, resourceContext.getResource(), resourceContext.getContentBuilder());
            } else {
                 logger.warn("no resource to output");
             }
            return this;
        }
    }*/

}
