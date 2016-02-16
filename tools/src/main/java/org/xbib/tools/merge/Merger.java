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
package org.xbib.tools.merge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;
import org.xbib.tools.Processor;
import org.xbib.tools.input.ElasticsearchInput;
import org.xbib.tools.metrics.Metrics;
import org.xbib.tools.output.ElasticsearchOutput;
import org.xbib.util.IndexDefinition;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.Worker;
import org.xbib.util.concurrent.WorkerProvider;
import org.xbib.util.concurrent.WorkerRequest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public abstract class Merger<W extends Worker<Pipeline<W,R>, R>, R extends WorkerRequest>
        implements Processor {

    private final static Logger logger = LogManager.getLogger(Merger.class);

    protected Settings settings;

    protected Pipeline<W, R> pipeline;

    protected SearchTransportClient search;

    protected Ingest ingest;

    protected ElasticsearchInput elasticsearchInput;

    protected ElasticsearchOutput elasticsearchOutput;

    protected Map<String,IndexDefinition> inputIndexDefinitionMap;

    protected Map<String,IndexDefinition> outputIndexDefinitionMap;

    protected Metrics metrics;

    public int run(Settings settings) throws Exception {
        this.elasticsearchInput = new ElasticsearchInput();
        this.elasticsearchOutput = new ElasticsearchOutput();
        this.metrics = new Metrics();
        this.settings = settings;
        logger.info("starting, settings = {}", settings.getAsMap());
        int concurrency = settings.getAsInt("concurrency", Runtime.getRuntime().availableProcessors());
        logger.info("configuring fork/join pipeline with concurrency {}", concurrency);
        pipeline = newPipeline();
        pipeline.setQueue(new SynchronousQueue<>(true));
        int returncode = 0;
        try {
            prepareResources();
            pipeline.setConcurrency(concurrency)
                    .setWorkerProvider(provider())
                    .prepare()
                    .execute();
            prepareRequests();
            scheduleMetrics();
            waitFor();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            returncode = 1;
        } finally {
            disposeRequests();
            disposeResources();
            disposeMetrics();
            pipeline.shutdown();
            Map<W, Throwable> throwables = pipeline.getWorkerErrors().getThrowables();
            if (!throwables.isEmpty()) {
                logger.error("found {} worker exceptions", throwables.size());
                for (Map.Entry<W, Throwable> entry : throwables.entrySet()) {
                    W w = entry.getKey();
                    Throwable t = entry.getValue();
                    logger.error(w + ": " + t.getMessage(), t);
                }
                returncode = 1;
            }
            // clear interrupt status, so Runner can continue
            Thread.interrupted();
            logger.info("finish");
        }
        return returncode;
    }

    public Map<String,IndexDefinition> getInputIndexDefinitionMap() {
        return inputIndexDefinitionMap;
    }

    public Map<String,IndexDefinition> getOutputIndexDefinitionMap() {
        return outputIndexDefinitionMap;
    }

    protected Pipeline<W,R> newPipeline() {
        return new ForkJoinPipeline<>();
    }

    public Pipeline<W,R> getPipeline() {
        return pipeline;
    }

    protected abstract void waitFor() throws IOException;

    protected abstract WorkerProvider<W> provider();

    protected void prepareRequests() throws Exception {
        // do nothing
    }

    protected  void prepareResources() throws Exception {
        Map<String,Settings> outputMap = settings.getGroups("output");
        for (Map.Entry<String,Settings> entry : outputMap.entrySet()) {
            if ("elasticsearch".equals(entry.getKey())) {
                logger.info("preparing Elasticsearch for output");
                prepareElasticsearchForOutput(entry.getValue());
            }
        }
        if (this.outputIndexDefinitionMap == null) {
            throw new IllegalArgumentException("no output definition map found");
        }
        logger.info("prepareResources: {}", settings.getAsMap());
        // we have to prepare this input before the workers come up!
        Map<String,Settings> inputMap = settings.getGroups("input");
        for (Map.Entry<String,Settings> entry : inputMap.entrySet()) {
            if ("elasticsearch".equals(entry.getKey())) {
                logger.info("preparing Elasticsearch for input");
                prepareElasticsearchForInput(entry.getValue());
            }
        }
        if (this.inputIndexDefinitionMap == null) {
            throw new IllegalArgumentException("no input definition map found");
        }
    }

    protected void prepareElasticsearchForInput(Settings elasticsearchSettings) throws IOException {
        this.search = elasticsearchInput.createSearch(elasticsearchSettings);
        this.inputIndexDefinitionMap = elasticsearchInput.makeIndexDefinitions(search, elasticsearchSettings.getGroups("index"));
    }

    protected void prepareElasticsearchForOutput(Settings elasticsearchSettings) throws IOException {
        this.ingest = elasticsearchOutput.createIngest(elasticsearchSettings);
        metrics.scheduleIngestMetrics(settings, ingest.getMetric());
        this.outputIndexDefinitionMap = elasticsearchOutput.makeIndexDefinitions(ingest, elasticsearchSettings.getGroups("index"));
        logger.info("creation of {}", outputIndexDefinitionMap.keySet());
        for (Map.Entry<String,IndexDefinition> entry : outputIndexDefinitionMap.entrySet()) {
            elasticsearchOutput.createIndex(ingest, entry.getValue());
        }
        logger.info("startup of {}", outputIndexDefinitionMap.keySet());
        elasticsearchOutput.startup(ingest, outputIndexDefinitionMap);
    }

    protected void disposeRequests() throws IOException {
        elasticsearchInput.close(search, inputIndexDefinitionMap);
    }

    protected void disposeResources() throws IOException {
        elasticsearchOutput.close(ingest, outputIndexDefinitionMap);
    }

    protected void scheduleMetrics() {
        if (getPipeline().getWorkers() == null || getPipeline().getWorkers().isEmpty()) {
            logger.warn("no workers for metrics");
            return;
        }
        for (Worker<Pipeline<W,R>, R> worker : getPipeline().getWorkers()) {
            metrics.scheduleMetrics(settings, "meter", worker.getMetric());
        }
        metrics.scheduleIngestMetrics(settings, ingest.getMetric());
    }

    protected void disposeMetrics() throws IOException {
        metrics.disposeMetrics();
    }



}
