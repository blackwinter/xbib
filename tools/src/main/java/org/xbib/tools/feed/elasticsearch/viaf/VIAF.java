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
package org.xbib.tools.feed.elasticsearch.viaf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.tools.convert.Converter;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.rdfxml.RdfXmlContentParser;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * VIAF indexer to Elasticsearch
 */
public class VIAF extends Feeder {

    private final static Logger logger = LogManager.getLogger(VIAF.class);

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new VIAF().setPipeline(p);
    }

    /**
     * One RDF/XML per line
     * @param uri the URI
     * @throws Exception
     */
    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();
            Reader r = new InputStreamReader(in, UTF8);
            BufferedReader reader = new BufferedReader(r);
            String line;
            while ((line = reader.readLine()) != null) {
                RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext,
                        settings.get("index", "viaf"),
                        settings.get("type", "viaf"));
                params.setHandler((content, p) -> {
                    if (settings.getAsBoolean("mock", false)) {
                        logger.info("{}/{}/{} {}", p.getIndex(), p.getType(), p.getId(), content);
                    } else {
                        ingest.index(p.getIndex(), p.getType(), p.getId(), content);
                    }
                });
                // lines are not pure XML, they look like "109429104        <rdf:RDF xmlns..."
                int pos = line.indexOf("<rdf:RDF");
                if (pos >= 0) {
                    line = line.substring(pos);
                }
                new RdfXmlContentParser(new StringReader(line))
                        .setRdfContentBuilderProvider(() -> routeRdfXContentBuilder(params))
                        .parse();
            }
            reader.close();
        }
    }

}

