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
package org.xbib.etl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.iri.IRI;
import org.xbib.io.StreamListener;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.rdf.memory.MemoryRdfGraph;
import org.xbib.util.concurrent.SimpleForkJoinPipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EntityQueue<S extends EntityBuilderState, E extends Entity, K, V>
    extends SimpleForkJoinPipeline<List<K>> implements StreamListener<K> {

    private final static Logger logger = LogManager.getLogger(EntityQueue.class.getName());

    private final Specification<E> specification;

    private final Map<String,Object> map;

    private LinkedList<K> objects;

    private boolean closed;

    public EntityQueue(Specification<E> specification, int workers) {
        super(workers);
        this.specification = specification;
        this.map = specification.getMap();
    }

    public Specification<E> getSpecification() {
        return specification;
    }

    public Map<String,Object> getMap() {
        return map;
    }

    @Override
    public void onBegin() {
        objects = new LinkedList<>();
    }

    @Override
    public void onObject(K object) {
        objects.add(object);
    }

    @Override
    public void onEnd() {
        if (closed) {
            return;
        }
        try {
            // poor man's copy-on-write
            submit((List<K>) objects.clone());
            objects.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            closed = true;
            try {
                finish(60, TimeUnit.SECONDS);
            } catch (InterruptedException e1) {
                // ignore
            }
        }
    }

    private final List<K> poison = new LinkedList<>();

    @Override
    protected List<K> poison() {
        return poison;
    }

    public Map<IRI,RdfContentBuilderProvider> contentBuilderProviders() {
        return new HashMap<>();
    }

    public void beforeCompletion(S state) throws IOException {
    }

    public void afterCompletion(S state) throws IOException {
    }

    @Override
    public EntityWorker newWorker() {
        return new EntityWorker();
    }

    public class EntityWorker extends DefaultWorker implements Worker<List<K>> {

        private S workerState;

        @SuppressWarnings("unchecked")
        public S newState() {
            return (S) new DefaultEntityBuilderState(new MemoryRdfGraph(), contentBuilderProviders());
        }

        @Override
        public void execute(List<K> request) throws IOException {
            this.workerState = newState();
            for (K key : request) {
                if (key == null) {
                    break;
                } else {
                    build(key);
                }
            }
            beforeCompletion(workerState);
            workerState.complete();
            afterCompletion(workerState);
        }

        public S getWorkerState() {
            return workerState;
        }

        public void build(K key) throws IOException {
        }

    }

}
