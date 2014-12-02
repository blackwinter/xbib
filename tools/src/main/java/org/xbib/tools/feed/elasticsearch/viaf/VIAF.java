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
import org.xbib.io.InputService;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.rdfxml.RdfXmlContentParser;
import org.xbib.tools.Feeder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * VIAF indexer to Elasticsearch
 */
public class VIAF extends Feeder {

    private final static Logger logger = LogManager.getLogger(VIAF.class.getSimpleName());

    @Override
    public String getName() {
        return "viaf-es";
    }
    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return VIAF::new;
    }

    @Override
    public void process(URI uri) throws Exception {
        IRINamespaceContext namespaceContext = IRINamespaceContext.getInstance();
        InputStream in = InputService.getInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        long linecounter = 0;
        while ((line = reader.readLine()) != null) {
            RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext,
                    settings.get("index", "viaf"),
                    settings.get("type", "viaf"));
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            RdfXmlContentParser parser = new RdfXmlContentParser(new StringReader(line));
            parser.setBuilder(builder);
            parser.parse();
            linecounter++;
            if (linecounter % 10000 == 0) {
                logger.info("{}", linecounter);
            }
        }
        in.close();
    }

}

