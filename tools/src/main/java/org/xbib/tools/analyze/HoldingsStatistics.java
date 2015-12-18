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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.search.SearchClient;
import org.xbib.tools.Bootstrap;

import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.xbib.common.settings.Settings.settingsBuilder;

public class HoldingsStatistics implements Bootstrap {

    private final static Logger logger = LogManager.getLogger(HoldingsStatistics.class.getName());

    private Map<String,Integer> volume = new HashMap<>();

    private Map<String,Integer> online = new HashMap<>();

    private Map<String,Integer> singles = new HashMap<>();

    @Override
    public void bootstrap(Reader reader) throws Exception {
        bootstrap(reader, null);
    }

    @Override
    public void bootstrap(Reader reader, Writer writer) throws Exception {
        Settings settings = settingsBuilder().loadFromReader(reader).build();
        SearchClient search = new SearchClient().newClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                .build());
        Client client = search.client();
        QueryBuilder queryBuilder = matchAllQuery();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
                .setIndices(settings.get("ezdb-index", "ezdb"))
                .setTypes(settings.get("ezdb-type", "DateHoldings"))
                .setSize(1000) // per shard
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMinutes(1))
                .setQuery(queryBuilder)
                .addFields("institution.service.serviceisil", "institution.service.carriertype");
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        while (searchResponse.getScrollId() != null) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMinutes(1))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                List<Object> isils = hit.getFields().containsKey("institution.service.serviceisil") ?
                        hit.getFields().get("institution.service.serviceisil").getValues() : Collections.EMPTY_LIST;
                List<Object> carriers = hit.getFields().containsKey("institution.service.carriertype") ?
                        hit.getFields().get("institution.service.carriertype").getValues() : Collections.EMPTY_LIST;
                for (int i = 0; i < isils.size(); i++) {
                    String isil = isils.get(i).toString();
                    switch (carriers.get(i).toString()) {
                        case "volume" : {
                            if (volume.containsKey(isil)) {
                                volume.put(isil, volume.get(isil) + 1);
                            } else {
                                volume.put(isil, 1);
                            }
                            break;
                        }
                        case "online resource" : {
                            if (online.containsKey(isil)) {
                                online.put(isil, online.get(isil) + 1);
                            } else {
                                online.put(isil, 1);
                            }
                            break;
                        }
                    }
                }
                if (isils.size() == 1) {
                    String isil = isils.get(0).toString();
                    if (singles.containsKey(isil)) {
                        singles.put(isil, singles.get(isil) + 1);
                    } else {
                        singles.put(isil, 1);
                    }
                }
            }
        }

        SortedSet<Map.Entry<String, Integer>> sortedVolumes = new TreeSet<Map.Entry<String, Integer>>((e1, e2) -> {
            return e1.getValue().compareTo(e2.getValue());
        });
        sortedVolumes.addAll(volume.entrySet());
        FileWriter fileWriter = new FileWriter("volumes-statistics.txt");
        for (Map.Entry<String, Integer> entry : sortedVolumes) {
            fileWriter.write(entry.getKey());
            fileWriter.write("\t");
            fileWriter.write(Integer.toString(entry.getValue()));
            fileWriter.write("\n");
        }
        fileWriter.close();

        SortedSet<Map.Entry<String, Integer>> sortedOnline = new TreeSet<Map.Entry<String, Integer>>((e1, e2) -> {
            return e1.getValue().compareTo(e2.getValue());
        });
        sortedOnline.addAll(online.entrySet());
        fileWriter = new FileWriter("online-statistics.txt");
        for (Map.Entry<String, Integer> entry : sortedOnline) {
            fileWriter.write(entry.getKey());
            fileWriter.write("\t");
            fileWriter.write(Integer.toString(entry.getValue()));
            fileWriter.write("\n");
        }
        fileWriter.close();

        SortedSet<Map.Entry<String, Integer>> sortedSingles = new TreeSet<Map.Entry<String, Integer>>((e1, e2) -> {
            return e1.getValue().compareTo(e2.getValue());
        });
        sortedSingles.addAll(singles.entrySet());
        fileWriter = new FileWriter("single-statistics.txt");
        for (Map.Entry<String, Integer> entry : sortedSingles) {
            fileWriter.write(entry.getKey());
            fileWriter.write("\t");
            fileWriter.write(Integer.toString(entry.getValue()));
            fileWriter.write("\n");
        }
        fileWriter.close();
    }

}
