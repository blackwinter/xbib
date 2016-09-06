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
package org.xbib.util.concurrent;

import org.xbib.common.settings.Settings;
import org.xbib.marc.FieldList;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public abstract class SimpleForkJoinPipeline<R> {

    private final int workerCount;

    private final BlockingQueue<R> queue;

    private final ExecutorService service;

    private final Set<Worker> workers;

    protected final List<FieldFilter> titleFilters;

    protected final List<FieldFilter> fieldFilters;

    public SimpleForkJoinPipeline(int workerCount) {
        this.workerCount = workerCount;
        this.queue = new SynchronousQueue<>(true);
        this.service = Executors.newFixedThreadPool(workerCount);
        this.workers = new HashSet<>();
        this.titleFilters = new ArrayList<>();
        this.fieldFilters = new ArrayList<>();
    }

    protected abstract Worker newWorker();

    protected abstract R poison();

    public void execute() {
        for (int i = 0; i < workerCount; i++) {
            Worker worker = newWorker();
            workers.add(worker);
            service.submit(worker);
        }
    }

    public Collection<Worker> getWorkers() {
        return workers;
    }

    public void submit(R job) {
        if (workers.isEmpty()) {
            throw new RuntimeException("no workers available");
        }
        try {
            queue.put(job);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void finish(long timeout, TimeUnit timeUnit) throws InterruptedException {
        for (int i = 0; i < workers.size(); i++) {
            queue.put(poison());
        }
        service.shutdownNow();
        service.awaitTermination(timeout, timeUnit);
    }

    public interface Worker<J> extends Runnable {
        void execute(J job) throws IOException;
    }

    public List<FieldFilter> addTitleFilters(Settings settings){
        return getFilters(settings, "title", titleFilters);
    }

    public List<FieldFilter> addFieldFilters(Settings settings){
        return getFilters(settings, "fields", fieldFilters);
    }

    private List<FieldFilter> getFilters(Settings settings, String subgroup, List<FieldFilter> filters) {
        Settings filterSettings = settings.getAsSettings("filters");
        if (filterSettings != null){
            Settings subfilterSettings = filterSettings.getAsSettings(subgroup);
            if (subfilterSettings != null){
                Iterator<Entry<String, String>> it = subfilterSettings.getAsMap().entrySet().iterator();
                while (it.hasNext()){
                    Entry<String, String> entry = it.next();
                    String[] fieldAndSubfield = entry.getKey().split("\\$.{2}\\$", 2);
                    filters.add(new FieldFilter(fieldAndSubfield[0], fieldAndSubfield[1], entry.getValue()));
                }
            }
        }
        return filters;
    }

    public class DefaultWorker extends Thread implements Worker<R> {
        @Override
        public void run() {
            try {
                while (true) {
                    R job = queue.take();
                    if (job.equals(poison())) {
                        break;
                    }
                    if (passesTitleFilters((List<FieldList>) job)) {
                        execute(job);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable t) {
                workers.remove(this);
                onFailure(t);
                throw new RuntimeException(t);
            }
        }

        public void execute(R job) throws IOException {
            // empty, do nothing
        }

        public void onFailure(Throwable t) {
            t.printStackTrace();
        }

        protected boolean passesTitleFilters(List<FieldList> job) {
            if (titleFilters == null || titleFilters.isEmpty()){
                return true;
            }
            for (FieldFilter filter : titleFilters) {
                if (filter.matchesAnyOf(job)){
                    return true;
                }
            }
            // no match found:
            return false;
        }
    }

}
