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
package org.xbib.tools.convert.dnb.gnd;

import org.xbib.io.InputService;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.io.rdfxml.RdfXmlReader;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.tools.Converter;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * Convert GND from RDF/XML to Turtle or Ntriples
 */
public class FromRdfXml extends Converter {

    @Override
    public String getName() {
        return "gnd-turtle-ntriples";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromRdfXml();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        String outName = settings.get("output") + ".gz";
        OutputStream out = new FileOutputStream(outName);
        out = new GZIPOutputStream(out) {
            {
                def.setLevel(Deflater.BEST_COMPRESSION);
            }
        };
        String marker = settings.get("translatePicaSortMarker");
        if ("turtle".equals(settings.get("format"))) {
            TurtleWriter turtle = new TurtleWriter(new OutputStreamWriter(out, "UTF-8"));
            turtle.setSortLanguageTag(marker);
            RdfXmlReader reader = new RdfXmlReader();
            reader.parse(new InputStreamReader(in, "UTF-8"), turtle);
            turtle.close();
            in.close();
            out.close();
        } else if ("ntriples".equals(settings.get("format"))) {
            NTripleWriter ntriples = new NTripleWriter(new OutputStreamWriter(out, "UTF-8"));
            ntriples.setSortLanguageTag(marker);
            RdfXmlReader reader = new RdfXmlReader();
            reader.parse(new InputStreamReader(in, "UTF-8"), ntriples);
            ntriples.close();
            in.close();
            out.close();
        }
    }
}
