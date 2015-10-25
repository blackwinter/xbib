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
package org.xbib.pipeline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.metric.MeterMetric;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Basic pipeline for pipeline requests.
 * This abstract class can be used for creating custom Pipeline classes.
 *
 * @param <R> the pipeline request type
 */
public abstract class AbstractPipeline<R extends PipelineRequest>
        implements Pipeline<R> {

    private final static Logger logger = LogManager.getLogger(AbstractPipeline.class);

    private BlockingQueue<R> queue;

    private MeterMetric metric;

    @Override
    public Pipeline<R> setQueue(BlockingQueue<R> queue) {
        this.queue = queue;
        return this;
    }

    public BlockingQueue<R> getQueue() {
        return queue;
    }

    /**
     * Call this thread. Take next request and pass them to request listeners.
     * At least, this pipeline itself can listen to requests and handle errors.
     * Only PipelineExceptions are handled for each listener. Other execptions will quit the
     * pipeline request executions.
     * @return a metric about the pipeline request executions.
     * @throws Exception if pipeline execution was sborted by a non-PipelineException
     */
    @Override
    public R call() throws Exception {
        metric = new MeterMetric(5L, TimeUnit.SECONDS);
        R r = null;
        try {
            r = queue.poll(5L, TimeUnit.SECONDS);
            while (r != null) {
                newRequest(this, r);
                metric.mark();
                r = queue.poll(5L, TimeUnit.SECONDS);
            }
            close();
        } catch (InterruptedException e) {
            logger.warn("interrupted");
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw t;
        } finally {
            metric.stop();
        }
        return r;
    }

    /**
     * Return the metric.
     *
     * @return the metric of this pipeline
     */
    public MeterMetric getMetric() {
        return metric;
    }

    /**
     * A new request for the pipeline is processed.
     * @param pipeline the pipeline
     * @param request the pipeline request
     */
    public abstract void newRequest(Pipeline<R> pipeline, R request);

}
