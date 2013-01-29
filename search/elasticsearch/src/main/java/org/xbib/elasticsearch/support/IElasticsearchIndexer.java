/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.xbib.elasticsearch.support;

import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.net.URI;

/**
 * Elasticsearch Indexer Interface
 *
 * @author Jörg Prante <joergprante@gmail.com>
 */
public interface IElasticsearchIndexer {

    /**
     * Set settings
     *
     * @param settings
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer settings(Settings settings);

    /**
     * Create a new client
     *
     * @return Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer newClient();

    /**
     * Create a new client
     *
     * @param uri the URI to connect to
     * @return Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer newClient(URI uri);

    /**
     * Wait for helathy cluster
     *
     * @return
     * @throws java.io.IOException
     */
    IElasticsearchIndexer waitForHealthyCluster() throws IOException;

    /**
     * Set index
     *
     * @param index
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer index(String index);

    /**
     * Get index
     *
     * @return the index
     */
    String index();

    /**
     * Set type
     *
     * @param type
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer type(String type);

    /**
     * @return
     */
    String type();

    /**
     * Enable or disable automatic date detection
     *
     * @param dateDetection
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer dateDetection(boolean dateDetection);

    /**
     * Set maximum number of bulk actions
     *
     * @param bulkActions
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer maxBulkActions(int bulkActions);

    /**
     * Set maximum concurent bulk requests
     *
     * @param maxConcurentBulkRequests
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer maxConcurrentBulkRequests(int maxConcurentBulkRequests);

    /**
     * Create document
     *
     * @param index
     * @param type
     * @param id
     * @param source
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer create(String index, String type, String id, String source);

    /**
     * Index document
     *
     * @param index
     * @param type
     * @param id
     * @param source
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer index(String index, String type, String id, String source);

    /**
     * Delete document
     *
     * @param index
     * @param type
     * @param id
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer delete(String index, String type, String id);

    /**
     * Ensure that all bulk
     *
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer flush();

    IElasticsearchIndexer startBulkMode();
    
    IElasticsearchIndexer stopBulkMode();
    
    long getVolumeInBytes();

    /**
     * Create a new index
     *
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer newIndex();

    /**
     * Delete index
     *
     * @return this Elasticsearch Indexer Interface
     */
    IElasticsearchIndexer deleteIndex();

    /**
     * Shutdown this Elasticsearch Indexer
     */
    void shutdown();

}
