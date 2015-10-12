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
import org.xbib.elasticsearch.support.client.search.SearchClient;
import org.xbib.io.Request;
import org.xbib.io.Session;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.http.HttpResponse;
import org.xbib.io.http.HttpResponseListener;
import org.xbib.io.http.netty.NettyHttpSession;
import org.xbib.tools.CommandLineInterpreter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Sets.newTreeSet;
import static org.elasticsearch.index.query.FilterBuilders.existsFilter;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.xbib.common.settings.ImmutableSettings.settingsBuilder;

public class ISSNsOfZDBInJournalTOCs implements CommandLineInterpreter {

    private final static Logger logger = LogManager.getLogger(ISSNsOfZDBInJournalTOCs.class.getName());

    private final static Set<String> issns = newTreeSet();

    private static Settings settings;

    private String issn;

    public ISSNsOfZDBInJournalTOCs reader(Reader reader) {
        settings = settingsBuilder().loadFromReader(reader).build();
        return this;
    }

    public ISSNsOfZDBInJournalTOCs settings(Settings newSettings) {
        settings = newSettings;
        return this;
    }

    public ISSNsOfZDBInJournalTOCs writer(Writer writer) {
        return this;
    }

    @Override
    public void run() throws Exception {
        NettyHttpSession session = new NettyHttpSession();
        session.open(Session.Mode.READ);
        HttpRequest httpRequest = session.newRequest();
        final AtomicInteger found = new AtomicInteger();
        final AtomicInteger notfound =  new AtomicInteger();;

        HttpResponseListener listener = new HttpResponseListener() {
            @Override
            public void receivedResponse(HttpResponse response) throws IOException {
                logger.info("status={}", response.getStatusCode());
            }

            @Override
            public void onConnect(Request request) throws IOException {
            }

            @Override
            public void onDisconnect(Request request) throws IOException {
            }

            @Override
            public void onReceive(Request request, CharSequence message) throws IOException {
                if (!message.toString().contains("has returned 0 articles")) {
                    issns.add(issn);
                    found.incrementAndGet();
                } else {
                    notfound.incrementAndGet();
                }
            }

            @Override
            public void onError(Request request, CharSequence errorMessage) throws IOException {

            }
        };

        SearchClient search = new SearchClient().newClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                .build());
        Client client = search.client();
        try {
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
                    .setIndices(settings.get("ezdb-index", "ezdb"))
                    .setTypes(settings.get("ezdb-type", "Manifestation"))
                    .setSize(1000) // per shard
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMinutes(10));

            QueryBuilder queryBuilder = filteredQuery(matchAllQuery(), existsFilter("identifiers.issn"));
            searchRequestBuilder.setQuery(queryBuilder)
                    .addFields("identifiers.issn");

            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            while (searchResponse.getScrollId() != null) {
                searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMinutes(10))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    if (hit.getFields().containsKey("identifiers.issn")) {
                        List<Object> l = hit.getFields().get("identifiers.issn").getValues();
                        for (Object o : l) {
                            issn = o.toString();
                            if (issn.length() > 8) {
                                issn = issn.substring(0,8); // cut to first 8 letters
                            }
                            logger.info("issn={}", issn);
                            httpRequest.setMethod("GET")
                                    .setURL(URI.create("http://www.journaltocs.ac.uk/api/journals/" + issn))
                                    .addParameter("output", "articles")
                                    .addParameter("user", "joergprante@gmail.com")
                                    .prepare()
                                    .execute(listener)
                                    .waitFor();
                            logger.info("found={} notfound={}", found.get(), notfound.get());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            search.shutdown();
        }
        session.close();
        FileWriter writer = new FileWriter(settings.get("output","journaltocs-issns.txt"));
        for (String issn : issns) {
            writer.write(issn);
            writer.write("\n");
        }
        writer.close();
    }

}