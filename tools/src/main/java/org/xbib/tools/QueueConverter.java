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
package org.xbib.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.element.PipelineElement;
import org.xbib.pipeline.queue.MetricQueuePipelineExecutor;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public abstract class QueueConverter<T, R extends PipelineRequest, P extends Pipeline<T, R>, E extends PipelineElement>
        extends Converter<T, R, P> {

    private final static Logger logger = LogManager.getLogger(QueueConverter.class.getSimpleName());

    private static QueueConverter converter;

    protected MetricQueuePipelineExecutor<T, R, P, E> executor;

    @Override
    public QueueConverter<T, R, P, E> prepare() throws IOException {
        super.prepare();
        converter = this;
        return this;
    }

    @Override
    public void run() throws Exception {
        try {
            logger.info("preparing");
            prepare();
            logger.info("executing");
            executor = new MetricQueuePipelineExecutor<T, R, P, E>()
                    .setConcurrency(settings.getAsInt("concurrency", 1))
                    .setPipelineProvider(pipelineProvider())
                    .prepare()
                    .execute()
                    .waitFor();
            logger.info("execution completed");
        } finally {
            cleanup();
            if (executor != null) {
                executor.shutdown(getPoisonElement()); // must be non null
                executor.shutdown();
                writeMetrics(executor.getMetric(), writer);
            }
        }
    }

    public BlockingQueue<E> queue() {
        return executor.queue();
    }

    public void countDown() {
        executor.countDown();
    }

    protected PipelineProvider<P> pipelineProvider() {
        return new PipelineProvider<P>() {
            int i = 0;

            @Override
            public P get() {
                return (P) new DefaultQueueConverterPipeline(converter, i++);
            }
        };
    }

    public class DefaultQueueConverterPipeline
            extends QueueConverterPipeline<T, DefaultPipelineRequest, Pipeline<T, DefaultPipelineRequest>, E> {

        public DefaultQueueConverterPipeline(QueueConverter converter, int num) {
            super(converter, num);
        }

        @Override
        public DefaultPipelineRequest next() {
            return new DefaultPipelineRequest(getElement());
        }

        @Override
        public void remove() {

        }

        @Override
        protected void process(DefaultPipelineRequest request) throws IOException {
            converter.process(request.object);
        }

        @Override
        public MeterMetric getMetric() {
            return executor.getMetric();
        }
    }

    public class DefaultPipelineRequest implements PipelineRequest {

        private final E object;

        public DefaultPipelineRequest(E object) {
            this.object = object;
        }

        public E get() {
            return object;
        }

    }

    protected abstract E getPoisonElement();

    protected abstract void process(E element) throws IOException;

}
