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
package org.xbib.tools.convert.zdb;

import org.xbib.csv.CSVGenerator;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.rdf.RdfMetadataHandler;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.oai.xml.MetadataHandler;
import org.xbib.oai.xml.XmlMetadataHandler;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Property;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.OAIHarvester;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URI;

import static com.google.common.collect.Queues.newConcurrentLinkedQueue;

/**
 * Fetch OAI result from ZDB OAI service.
 * Output is written to CSV file.
 */
public class FromOAI2CSV extends OAIHarvester {

    private final static Logger logger = LoggerFactory.getLogger(FromOAI2CSV.class.getName());

    @Override
    public String getName() {
        return "zdb-oai-csv";
    }

    @Override
    protected FromOAI2CSV prepare() throws IOException {
        String[] inputs = settings.getAsArray("input");
        if (inputs == null) {
            throw new IllegalArgumentException("no input given");
        }
        input = newConcurrentLinkedQueue();
        for (String uri : inputs) {
            input.offer(URI.create(uri));
        }
        try {
            URI outputURI = URI.create(settings.get("output"));
            FileOutputStream out = new FileOutputStream(outputURI.getPath());
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            CSVGenerator generator = new CSVGenerator(writer);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromOAI2CSV();
            }
        };
    }

    protected MetadataHandler xmlMetadataHandler() {
        return new XmlPacketHandler().setWriter(new StringWriter());
    }

    protected class XmlPacketHandler extends XmlMetadataHandler {

        public void endDocument() throws SAXException {
            super.endDocument();
            logger.info("got XML document {}", getIdentifier());
            setWriter(new StringWriter());
        }
    }

    protected MetadataHandler ntripleMetadataHandler() {
        final RdfMetadataHandler metadataHandler = new RdfMetadataHandler();
        final RdfResourceHandler resourceHandler = rdfResourceHandler();
        // TODO
        StringWriter sw = new StringWriter();
        metadataHandler.setHandler(resourceHandler)
                .setTripleWriter(new NTripleWriter(sw));
        return metadataHandler;
    }

    protected RdfResourceHandler rdfResourceHandler() {
        return resourceHandler;
    }

    private final static RdfResourceHandler resourceHandler = new OAIResourceHandler();

    private static class OAIResourceHandler extends RdfResourceHandler {

        public OAIResourceHandler() {
            super(new SimpleResourceContext());
        }

        @Override
        public Property toProperty(Property property) {
            return property;
        }

        @Override
        public Object toObject(QName name, String content) {
            logger.info("name={} content={}", name, content);
            return super.toObject(name, content);
        }
    }
}
