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
package org.xbib.tools.feed.elasticsearch.orcid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.io.archive.tar.TarConnection;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RdfXContentParams;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.IndexDefinition;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;

public class ORCID extends Feeder {

    private final static Logger logger = LogManager.getLogger(ORCID.class);

    private final static IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    static {
        namespaceContext.add(new HashMap<String, String>() {{
            put(RdfConstants.NS_PREFIX, RdfConstants.NS_URI);
            put("dc", "http://purl.org/dc/elements/1.1/");
            put("dcterms", "http://purl.org/dc/terms/");
            put("foaf", "http://xmlns.com/foaf/0.1/");
            put("frbr", "http://purl.org/vocab/frbr/core#");
            put("fabio", "http://purl.org/spar/fabio/");
            put("prism", "http://prismstandard.org/namespaces/basic/3.0/");
        }});
    }

    @Override
    @SuppressWarnings("unchecked")
    protected WorkerProvider<Converter> provider() {
        return p -> new ORCID().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        // public_profiles.tar(.gz)
        IndexDefinition indexDefinition = indexDefinitionMap.get("orcid");
        TarConnection connection = new TarConnection();
        connection.setPath(Paths.get(uri.getSchemeSpecificPart()), StandardOpenOption.READ);
        Session<StringPacket> session = connection.createSession();
        if (session == null) {
            throw new IOException("can not open for input: " + uri);
        }
        session.open(Session.Mode.READ);
        StringPacket packet;
        while ((packet = session.read()) != null) {
            if (packet.name().endsWith("json")) {
                if (packet.packet().length() > 0) {
                    ORCIDMapper orcidMapper = new ORCIDMapper();
                    Map<String, Object> map = XContentHelper.convertToMap(packet.packet());
                    RdfXContentParams params = new RdfXContentParams(namespaceContext);
                    RdfContentBuilder builder = rdfXContentBuilder(params);
                    Resource resource = orcidMapper.map(map);
                    builder.receive(resource);
                    if (settings.getAsBoolean("mock", false)) {
                        if (resource.id() != null) {
                            logger.info("{} {}", resource.id(), params.getGenerator().get());
                        }
                    } else if (!resource.isEmpty()){
                        if (resource.id() != null) {
                            ingest.index(indexDefinition.getConcreteIndex(), indexDefinition.getType(),
                                    resource.id().toString(), params.getGenerator().get());
                        }
                    }
                }
            } /*else if (packet.name().endsWith("xml")) {
                if (packet.packet().length() > 0 && packet.packet().length() < 25 * 1024 * 1024) {
                    ORCIDMapper orcidMapper = new ORCIDMapper();
                    RdfXContentParams params = new RdfXContentParams(namespaceContext);
                    RdfContentBuilder builder = rdfXContentBuilder(params);
                    AbstractXmlHandler handler = orcidMapper.getHandler(params);
                    new XmlContentParser(new StringReader(packet.packet()))
                            .setNamespaces(false)
                            .setHandler(handler)
                            .parse();
                    Resource resource = handler.getResource();
                    builder.receive(resource);
                    if (settings.getAsBoolean("mock", false)) {
                        logger.info("{} {}", resource.id(), params.getGenerator().get());
                    } else {
                        if (resource.id() != null) {
                            Map<String, Object> map = XContentHelper.convertToMap(params.getGenerator().get());
                            Resource mappedResource = orcidMapper.map(map);
                            mappedResource.id(resource.id());
                            params = new RdfXContentParams(namespaceContext);
                            builder = rdfXContentBuilder(params);
                            builder.receive(mappedResource);
                            ingest.index(settings.get("index", "orcid"), settings.get("type", "orcid"),
                                    mappedResource.id().toString(), params.getGenerator().get());
                        }
                    }
                }
            }*/
        }
        session.close();
    }

}
