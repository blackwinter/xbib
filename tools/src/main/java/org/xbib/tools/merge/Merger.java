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
import org.xbib.tools.Processor;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.Worker;
import org.xbib.util.concurrent.WorkerProvider;
import org.xbib.util.concurrent.WorkerRequest;

import java.util.concurrent.SynchronousQueue;

public abstract class Merger<W extends Worker<Pipeline<W,R>, R>, R extends WorkerRequest>
        implements Processor {

    private final static Logger logger = LogManager.getLogger(Merger.class);


    public int run(Settings settings) throws Exception {
        try {
            int concurrency = settings.getAsInt("concurrency", Runtime.getRuntime().availableProcessors() * 2);
            logger.info("executing with concurrency {}", concurrency);
            Pipeline<W, R> pipeline = newPipeline();
            pipeline.setQueue(new SynchronousQueue<>(true));
            prepareOutput();
            pipeline.setConcurrency(concurrency)
                    .setWorkerProvider(provider())
                    .prepare()
                    .execute();
            prepareInput();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return 1;
        } finally {
            disposeInput();
            disposeOutput();
        }
        return 0;
    }

    protected Pipeline<W,R> newPipeline() {
        return new ForkJoinPipeline<>();
    }

    protected abstract WorkerProvider<W> provider();

    protected abstract void prepareInput() throws Exception;

    protected abstract void prepareOutput() throws Exception;

    protected abstract void disposeInput() throws Exception;

    protected abstract void disposeOutput() throws Exception;
}
