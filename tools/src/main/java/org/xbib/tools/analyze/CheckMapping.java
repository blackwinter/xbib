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
package org.xbib.tools.analyze;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.query.QueryBuilder;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;
import org.xbib.tools.Bootstrap;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.xbib.common.settings.Settings.settingsBuilder;

public class CheckMapping implements Bootstrap {

    private final static Logger logger = LogManager.getLogger(CheckMapping.class.getName());

    private static Settings settings;

    public CheckMapping settings(Settings newSettings) {
        settings = newSettings;
        return this;
    }

    @Override
    public int bootstrap(String[] args) throws Exception {
        if (args.length != 1) {
            return 1;
        }
        try (FileReader reader = new FileReader(args[0])) {
            return bootstrap(args, reader, null);
        }
    }

    @Override
    public int bootstrap(Reader reader) throws Exception {
        return bootstrap(null, reader, null);
    }

    @Override
    public int bootstrap(String[] args, Reader reader, Writer writer) throws Exception {
        settings = settingsBuilder().loadFromReader(reader).build();
        SearchTransportClient search = new SearchTransportClient().init(Settings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                .build().getAsMap());
        Client client = search.client();
        checkMapping(client, settings.get("index"));
        client.close();
        return 0;
    }

    protected void checkMapping(Client client, String index) throws IOException {
        GetMappingsRequestBuilder getMappingsRequestBuilder = client.admin().indices().prepareGetMappings()
                .setIndices(index);
        GetMappingsResponse getMappingsResponse = getMappingsRequestBuilder.execute().actionGet();
        ImmutableOpenMap<String, ImmutableOpenMap<String,MappingMetaData>> map = getMappingsResponse.getMappings();
        map.keys().forEach((Consumer<ObjectCursor<String>>) stringObjectCursor -> {
            ImmutableOpenMap<String, MappingMetaData> mappings = map.get(stringObjectCursor.value);
            for (ObjectObjectCursor<String, MappingMetaData> cursor : mappings) {
                String mappingName = cursor.key;
                MappingMetaData mappingMetaData = cursor.value;
                checkMapping(client, index, mappingName, mappingMetaData);
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected void checkMapping(Client client, String index, String type, MappingMetaData mappingMetaData) {
        try {
            CountRequestBuilder countRequestBuilder = client.prepareCount()
                    .setIndices(index)
                    .setTypes(type)
                    .setQuery(matchAllQuery());
            CountResponse countResponse = countRequestBuilder.execute().actionGet();
            long total = countResponse.getCount();
            if (total > 0L) {
                Map<String,Long> fields = new TreeMap();
                Map<String,Object> root = mappingMetaData.getSourceAsMap();
                checkMapping(client, index, type, "", "", root, fields);
                AtomicInteger empty = new AtomicInteger();
                SortedSet<Map.Entry<String, Long>> set = entriesSortedByValues(fields);
                set.forEach(entry -> {
                    logger.info("{} {} {}",
                            entry.getKey(),
                            entry.getValue(),
                            (double) (entry.getValue() * 100 / total));
                    if (entry.getValue() == 0) {
                        empty.incrementAndGet();
                    }
                });
                logger.info("check: index={} type={} numfields={} fieldsnotused={}",
                        index, type, set.size(), empty.get());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void checkMapping(Client client, String index, String type,
                                String path, String fieldName, Map<String,Object> map,
                                Map<String,Long> fields) {
        if (!path.isEmpty() && !path.endsWith(".")) {
            path = path + ".";
        }
        if (!"properties".equals(fieldName)) {
            path = path + fieldName;
        }
        for (String key : map.keySet()) {
            Object o = map.get(key);
            if (o instanceof Map) {
                Map<String, Object> child = (Map<String, Object>) o;
                o = map.get("type");
                String fieldType = o instanceof String ? o.toString() : null;
                if (!"standardnumber".equals(fieldType) && !"ref".equals(fieldType)) {
                    checkMapping(client, index, type, path, key, child, fields);
                }
            } else if ("type".equals(key)) {
                QueryBuilder filterBuilder = existsQuery(path);
                QueryBuilder queryBuilder = constantScoreQuery(filterBuilder);
                CountRequestBuilder countRequestBuilder = client.prepareCount()
                        .setIndices(index)
                        .setTypes(type)
                        .setQuery(queryBuilder);
                CountResponse countResponse = countRequestBuilder.execute().actionGet();
                fields.put(path, countResponse.getCount());
            }
        }
    }

    static <K,V extends Comparable<? super V>>
    SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                (e1, e2) -> {
                    int c = e2.getValue().compareTo(e1.getValue());
                    return c != 0 ? c : 1; // never return 0, keep all entries with same value
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}