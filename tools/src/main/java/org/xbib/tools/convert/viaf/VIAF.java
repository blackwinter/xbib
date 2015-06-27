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
package org.xbib.tools.convert.viaf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.InputService;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.io.rdfxml.RdfXmlContentParser;
import org.xbib.tools.Converter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

/**
 * VIAF converter
 */
public class VIAF extends Converter {

    private final static Logger logger = LogManager.getLogger(VIAF.class.getSimpleName());

    private static BlockingQueue<String> pump;

    private static ExecutorService pumpService;

    public static void main(String[] args) {
        try {
            Converter viaf = new VIAF()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"));
            pump = new SynchronousQueue(true);
            pumpService = Executors.newFixedThreadPool(settings.getAsInt("pumps", 1));
            viaf.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            pumpService.shutdownNow();
        }
        System.exit(0);
    }

    private VIAF() {
    }

    @Override
    public String getName() {
        return "viaf-rdf-ntriple";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return VIAF::new;
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        for (int i = 0; i < settings.getAsInt("pumps", 1); i++) {
            pumpService.submit(new VIAFPipeline());
        }
        String line;
        long linecounter = 0;
        while ((line = reader.readLine()) != null) {
            pump.put(line);
            linecounter++;
            if (linecounter % 10000 == 0) {
                logger.info("{}", linecounter);
            }
        }
        in.close();
        for (int i = 0; i < settings.getAsInt("pumps", 1); i++) {
            pump.put("|");
        }
    }

    private class VIAFPipeline implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {
            FileOutputStream out = new FileOutputStream(settings.get("output"));
            try {
                while (true) {
                    String line = pump.take();
                    if ("|".equals(line)) {
                        break;
                    }
                    RdfXmlContentParser parser = new RdfXmlContentParser(new StringReader(line));
                    //RdfContentBuilder builder = turtleBuilder(out);
                    parser.setRdfContentBuilderProvider(() ->  turtleBuilder(out));
                    parser.parse();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
            return true;
        }
    }

}
