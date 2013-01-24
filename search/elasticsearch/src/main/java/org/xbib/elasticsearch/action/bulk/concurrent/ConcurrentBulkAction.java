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
package org.xbib.elasticsearch.action.bulk.concurrent;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.TransportRequestOptions;

/**
 */
public class ConcurrentBulkAction extends Action<BulkRequest, BulkResponse, ConcurrentBulkRequestBuilder> {

    public static final ConcurrentBulkAction INSTANCE = new ConcurrentBulkAction();
    public static final String NAME = "concurrent_bulk";

    private ConcurrentBulkAction() {
        super(NAME);
    }

    @Override
    public BulkResponse newResponse() {
        return new BulkResponse(null, 0L);
    }

    @Override
    public ConcurrentBulkRequestBuilder newRequestBuilder(Client client) {
        return new ConcurrentBulkRequestBuilder(client);
    }

    @Override
    public TransportRequestOptions transportOptions(Settings settings) {
        return TransportRequestOptions.options()
                .withType(TransportRequestOptions.Type.fromString(settings.get("action.bulk.transport.type", TransportRequestOptions.Type.LOW.toString())))
                .withCompress(settings.getAsBoolean("action.bulk.compress", true));
    }
}
