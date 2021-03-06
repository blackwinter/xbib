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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;
import org.xbib.service.client.Clients;
import org.xbib.service.client.http.SimpleHttpClient;
import org.xbib.service.client.http.SimpleHttpRequest;
import org.xbib.service.client.http.SimpleHttpRequestBuilder;
import org.xbib.service.client.http.SimpleHttpResponse;
import org.xbib.service.client.invocation.RemoteInvokerFactory;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class ISSNsOfZDBInJournalTOCs extends Analyzer {

    private final static Logger logger = LogManager.getLogger(ISSNsOfZDBInJournalTOCs.class.getName());


    @Override
    public int run(Settings settings) throws Exception {
        SearchTransportClient search = new SearchTransportClient();
        try {
            Set<String> issns = new TreeSet<>();
            RemoteInvokerFactory remoteInvokerFactory = RemoteInvokerFactory.DEFAULT;
            final AtomicInteger found = new AtomicInteger();
            final AtomicInteger notfound =  new AtomicInteger();;
            search = search.init(Settings.settingsBuilder()
                    .put("cluster.name", settings.get("elasticsearch.cluster"))
                    .put("host", settings.get("elasticsearch.host"))
                    .put("port", settings.getAsInt("elasticsearch.port", 9300))
                    .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                    .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                    .build().getAsMap());
            Client client = search.client();
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
                    .setIndices(settings.get("ezdb-index", "ezdb"))
                    .setTypes(settings.get("ezdb-type", "Manifestation"))
                    .setSize(1000) // per shard
                    .setScroll(TimeValue.timeValueMinutes(10));
            QueryBuilder queryBuilder =
                    boolQuery().must(matchAllQuery()).filter(existsQuery("identifiers.issn"));
            searchRequestBuilder.setQuery(queryBuilder)
                    .addFields("identifiers.issn");
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            do {
                for (SearchHit hit : searchResponse.getHits()) {
                    if (hit.getFields().containsKey("identifiers.issn")) {
                        List<Object> l = hit.getFields().get("identifiers.issn").getValues();
                        for (Object o : l) {
                            String s = o.toString();
                            if (s.length() > 8) {
                                s = s.substring(0,8); // cut to first 8 letters
                            }
                            setISSN(s);
                            SimpleHttpClient simpleHttpClient = Clients.newClient(remoteInvokerFactory, "none+http://www.journaltocs.ac.uk",
                                    SimpleHttpClient.class);
                            SimpleHttpRequest request = SimpleHttpRequestBuilder.forGet("/api/journals/" + s + "?output=articles&user=joergprante@gmail.com")
                                    .header(HttpHeaderNames.ACCEPT, "utf-8")
                                    .header(HttpHeaderNames.CONTENT_LENGTH, "0")
                                    .build();
                            SimpleHttpResponse response = simpleHttpClient.execute(request).get();
                            String content = new String(response.content(), StandardCharsets.UTF_8);
                            if (!content.contains("has returned 0 articles")) {
                                issns.add(getISSN());
                                found.incrementAndGet();
                            } else {
                                notfound.incrementAndGet();
                            }
                            logger.info("found={} notfound={}", found.get(), notfound.get());
                        }
                    }
                }
                searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMinutes(10))
                        .execute().actionGet();
            } while (searchResponse.getHits().getHits().length > 0);
            //session.close();
            BufferedWriter fileWriter = getFileWriter(settings.get("output","journaltocs-issns.txt"));
            for (String s : issns) {
                fileWriter.write(s);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return 1;
        } finally {
            search.shutdown();
        }
        return 0;
    }

    private String issn;

    public void setISSN(String issn) {
        this.issn = issn;
    }

    public String getISSN() {
        return issn;
    }

}
