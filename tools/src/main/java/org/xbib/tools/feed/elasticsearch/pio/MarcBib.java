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
package org.xbib.tools.feed.elasticsearch.pio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.etl.marc.dialects.nlz.NlzEntityBuilderState;
import org.xbib.etl.marc.dialects.nlz.NlzEntityQueue;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.turtle.TurtleContentParams;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.feed.elasticsearch.marc.BibliographicFeeder;
import org.xbib.util.Finder;
import org.xbib.util.IndexDefinition;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.URIWorkerRequest;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;
import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public class MarcBib extends BibliographicFeeder {

    private final static Logger logger = LogManager.getLogger(MarcBib.class.getSimpleName());

    private final static IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    private final static Serials serials = new Serials();

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
    protected WorkerProvider<Converter> provider() {
        return p -> new MarcBib().setPipeline(p);
    }

    protected NlzEntityQueue createQueue(Map<String,Object> params) throws Exception {
        return new BibQueue(serials.getMap());
    }

    @Override
    public void prepareResources() throws IOException {
        super.prepareResources();
        // serials file
        Map<String,Settings> inputMap = settings.getGroups("input");
        Settings settings = inputMap.get("serials");
        Queue<URI> input = new Finder()
                .find(settings.get("path"), settings.get("name"))
                .getURIs();
        logger.info("parsing initial set of serials...");
        try {
            serials.process(settings, input.poll());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        logger.info("serials done, size={}", serials.getMap().size());
        if (serials.getMap().isEmpty()) {
            throw new IOException("no serials???");
        }
        // extra turtle output
        TurtleContentParams params = new TurtleContentParams(namespaceContext, true);
        if (fileOutput.getMap().containsKey("turtle")) {
            setRdfContentBuilder(turtleBuilder(fileOutput.getMap().get("turtle").getOut(), params));
        }
        if (fileOutput.getMap().containsKey("errors")) {
            setErrorRdfContentBuilder(turtleBuilder(fileOutput.getMap().get("errors").getOut(), params));
        }
        if (fileOutput.getMap().containsKey("noserial")) {
            setMissingRdfContentBuilder(turtleBuilder(fileOutput.getMap().get("noserial").getOut(), params));
        }
        // extra text file for missing serials
        if (fileOutput.getMap().containsKey("missingserials")) {
            setMissingSerialsWriter(new OutputStreamWriter(fileOutput.getMap().get("missingserials").getOut(), "UTF-8"));
        }
    }

    private RdfContentBuilder rdfContentBuilder;
    private RdfContentBuilder errorRdfContentBuilder;
    private RdfContentBuilder missingRdfContentBuilder;
    private Writer missingSerialsWriter;

    private void setRdfContentBuilder(RdfContentBuilder rdfContentBuilder) {
        this.rdfContentBuilder = rdfContentBuilder;
    }

    private void setErrorRdfContentBuilder(RdfContentBuilder errorRdfContentBuilder) {
        this.errorRdfContentBuilder = errorRdfContentBuilder;
    }

    private void setMissingRdfContentBuilder(RdfContentBuilder missingRdfContentBuilder) {
        this.missingRdfContentBuilder = missingRdfContentBuilder;
    }

    private void setMissingSerialsWriter(Writer writer) {
        this.missingSerialsWriter = writer;
    }

    @Override
    public Feeder setPipeline(Pipeline<Converter,URIWorkerRequest> pipeline) {
        super.setPipeline(pipeline);
        if (pipeline instanceof BuilderPipeline) {
            BuilderPipeline builderPipeline = (BuilderPipeline) pipeline;
            setRdfContentBuilder(builderPipeline.getRdfContentBuilder());
            setErrorRdfContentBuilder(builderPipeline.getErrorRdfContentBuilder());
            setMissingRdfContentBuilder(builderPipeline.getMissingRdfContentBuilder());
            setMissingSerialsWriter(builderPipeline.getMissingSerialsWriter());
        }
        return this;
    }

    private class BuilderPipeline extends FeederPipeline {

        private RdfContentBuilder getRdfContentBuilder() {
            return rdfContentBuilder;
        }

        private RdfContentBuilder getErrorRdfContentBuilder() {
            return errorRdfContentBuilder;
        }

        private RdfContentBuilder getMissingRdfContentBuilder() {
            return missingRdfContentBuilder;
        }

        private Writer getMissingSerialsWriter() {
            return missingSerialsWriter;
        }
    }

    private class BibQueue extends NlzEntityQueue {

        BibQueue(Map<String, Resource> serialsMap) throws Exception {
            super(serialsMap, settings.get("package", "org.xbib.analyzer.marc.nlz"),
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements",  "/org/xbib/analyzer/marc/nlz/bib.json")
            );
        }

        @Override
        public void afterCompletion(NlzEntityBuilderState state) throws IOException {
            IndexDefinition indexDefinition = indexDefinitionMap.get("bib");
            if (indexDefinition == null) {
                throw new IOException("no 'bib' index definition configured");
            }
            RouteRdfXContentParams params = new RouteRdfXContentParams(
                    namespaceContext,
                    indexDefinition.getConcreteIndex(),
                    indexDefinition.getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordIdentifier(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
            getMetric().mark();
            if (indexDefinition.isMock()) {
                logger.info("{}", builder.string());
            }
        }
    }
}
