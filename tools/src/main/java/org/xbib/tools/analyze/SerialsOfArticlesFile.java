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
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;

import java.io.BufferedWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class SerialsOfArticlesFile extends Analyzer {

    private final static Logger logger = LogManager.getLogger(SerialsOfArticlesFile.class.getSimpleName());

    private final static Set<String> issns = new TreeSet<>();

    @Override
    public void run(Settings settings) throws Exception {
        SearchTransportClient search = new SearchTransportClient().init(Settings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                .build().getAsMap());
        Client client = search.client();
        try {
            SearchRequestBuilder searchRequest = client.prepareSearch()
                    .setIndices(settings.get("articles-index", "articles"))
                    .setTypes(settings.get("articles-type", "articles"))
                    .setSize(1000) // per shard
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(1000));

            QueryBuilder queryBuilder = matchAllQuery();
            searchRequest.setQuery(queryBuilder)
                    .addFields("frbr:partOf.prism:issn");

            SearchResponse searchResponse = searchRequest.execute().actionGet();
            while (searchResponse.getScrollId() != null) {
                searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(1000))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    if (hit.getFields().containsKey("frbr:partOf.prism:issn")) {
                        List<Object> l = hit.getFields().get("frbr:partOf.prism:issn").getValues();
                        for (Object o : l) {
                            issns.add(o.toString());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            search.shutdown();
        }
        BufferedWriter fileWriter = getFileWriter(settings.get("output","serials.tsv"));
        for (String issn : issns) {
            fileWriter.write(issn);
            fileWriter.write("\n");
        }
        fileWriter.close();
    }

}
