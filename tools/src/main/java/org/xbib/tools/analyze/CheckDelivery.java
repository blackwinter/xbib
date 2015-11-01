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
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.QueryBuilder;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.support.client.search.SearchClient;
import org.xbib.tools.CommandLineInterpreter;
import org.xbib.tools.merge.serials.entities.TitleRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.xbib.common.settings.Settings.settingsBuilder;

public class CheckDelivery implements CommandLineInterpreter {

    private final static Logger logger = LogManager.getLogger(CheckDelivery.class.getName());

    private static Settings settings;

    private Set<String> notfoundset = new HashSet<>();

    public CheckDelivery reader(Reader reader) {
        settings = settingsBuilder().loadFromReader(reader).build();
        return this;
    }

    public CheckDelivery settings(Settings newSettings) {
        settings = newSettings;
        return this;
    }

    public CheckDelivery writer(Writer writer) {
        return this;
    }

    @Override
    public void run() throws Exception {
        SearchClient search = new SearchClient().newClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("source.cluster"))
                .put("host", settings.get("source.host"))
                .put("port", settings.getAsInt("source.port", 9300))
                .put("sniff", settings.getAsBoolean("source.sniff", false))
                .put("autodiscover", settings.getAsBoolean("source.autodiscover", false))
                .build());
        Client client = search.client();
        FileReader fileReader = new FileReader(settings.get("input"));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        int found = 0;
        int notfound = 0;
        int notfoundwithonline = 0;
        int notfoundissn = 0;
        while  ((line = bufferedReader.readLine()) != null) {
            String[] s = line.split(",");
            if (s.length != 2 && s.length !=3) {
                logger.warn("invalid line: {}", line);
                continue;
            }
            String zdbid = s[0].replaceAll("\\-", "").toLowerCase();
            String year = s[1];
            if (year.length() != 4) {
                logger.warn("invalid line: {}", line);
                continue;
            }
            int count = s.length == 3 ? Integer.parseInt(s[2]) : 1;
            QueryBuilder queryBuilder = boolQuery()
                    .must(termQuery("xbib:zdbid", zdbid))
                    .must(termQuery("dc:date", year));
            CountRequestBuilder countRequest = client.prepareCount()
                    .setIndices(settings.get("articles-index", "articles"))
                    .setQuery(queryBuilder);
            CountResponse countResponse = countRequest.execute().actionGet();
            if (countResponse.getCount() > 0) {
                found += count;
            } else {
                // parallel e-journal available?
                queryBuilder = termQuery("IdentifierZDB.identifierZDB", zdbid);
                SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
                        .setIndices(settings.get("zdb-index", "zdb"))
                        .setQuery(queryBuilder)
                        .setSize(1);
                SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
                if (searchResponse.getHits().getTotalHits() > 0) {
                    TitleRecord m = new TitleRecord(searchResponse.getHits().getAt(0).getSource());
                    zdbid = m.getOnlineExternalID();
                    if (zdbid != null) {
                        queryBuilder = boolQuery()
                                .must(termQuery("xbib:zdbid", zdbid))
                                .must(termQuery("dc:date", year));
                        countRequest = client.prepareCount()
                                .setIndices(settings.get("articles-index", "articles"))
                                .setQuery(queryBuilder);
                        countResponse = countRequest.execute().actionGet();
                        if (countResponse.getCount() > 0) {
                            found += count;
                        } else {
                            notfound += count;
                            notfoundwithonline += count;
                            if (m.getIdentifiers().get("issn") != null) {
                                notfoundissn += count;
                                notfoundset.add(zdbid);
                            }
                        }
                    } else {
                        if (m.getIdentifiers().get("issn") != null) {
                            notfoundissn += count;
                        }
                        notfound += count;
                    }
                } else {
                    logger.warn("not found: {}", zdbid);
                }
            }
        }
        bufferedReader.close();
        logger.info("found={} notfound={} notfoundissn={} notfoundwithonline={}",
                found, notfound, notfoundissn, notfoundwithonline);
        FileWriter fileWriter = new FileWriter("notfoundset.txt");
        for (String s : notfoundset) {
            fileWriter.write(s);
            fileWriter.write("\n");
        }
        fileWriter.close();
    }

}
