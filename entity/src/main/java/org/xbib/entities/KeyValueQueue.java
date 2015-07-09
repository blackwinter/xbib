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
package org.xbib.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.keyvalue.KeyValue;
import org.xbib.io.keyvalue.KeyValueStreamListener;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.util.JobQueue;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KeyValueQueue<K, V>
    extends JobQueue<List<KeyValue<K,V>>> implements KeyValueStreamListener<K,V> {

    private final static Logger logger = LogManager.getLogger(KeyValueQueue.class.getName());

    private final Specification specification;

    private final Map map;

    private LinkedList<KeyValue<K,V>> keyvalues;

    private boolean closed;

    public KeyValueQueue(int workers) {
        super(workers);
        this.specification = new DefaultSpecification();
        this.map = new HashMap<>();
    }

    public Specification specification() {
        return specification;
    }

    public Map map() {
        return map;
    }

    @Override
    public KeyValueQueue<K, V> begin() {
        keyvalues = new LinkedList<KeyValue<K,V>>();
        return this;
    }

    @Override
    public KeyValueQueue<K, V> keyValue(K key, V value) {
        keyvalues.add(new KeyValue<K,V>(key, value));
        return this;
    }

    @Override
    public KeyValueQueue<K, V> keys(List<K> keys) {
        // unused
        return this;
    }

    @Override
    public KeyValueQueue<K, V> values(List<V> values) {
        // unused
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public KeyValueQueue<K, V> end() {
        if (closed) {
            return this;
        }
        try {
            submit((List<KeyValue<K, V>>) keyvalues.clone());
            keyvalues.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            closed = true;
            try {
                finish(60, TimeUnit.SECONDS);
            } catch (InterruptedException e1) {
                // ignore
            }
        }
        return this;
    }

    private final List<KeyValue<K, V>> poison = new LinkedList<KeyValue<K,V>>();

    @Override
    protected List<KeyValue<K, V>> poison() {
        return poison;
    }

    public List<RdfContentBuilderProvider> contentBuilderProviders() {
        return new LinkedList<RdfContentBuilderProvider>();
    }

    @Override
    public KeyValueListWorker newWorker() {
        return new KeyValueListWorker();
    }

    public class KeyValueListWorker extends DefaultWorker implements Worker<List<KeyValue<K,V>>> {

        @Override
        public void execute(List<KeyValue<K, V>> job) throws IOException {
            for (KeyValue<K,V> kv : job) {
                K key = kv.key();
                V value = kv.value();
                if (key == null) {
                    break;
                } else {
                    build(key, value);
                }
            }
        }

        public void build(K key, V value) throws IOException {
        }

    }

}
