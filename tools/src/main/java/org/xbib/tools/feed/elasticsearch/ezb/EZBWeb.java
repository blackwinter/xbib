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
package org.xbib.tools.feed.elasticsearch.ezb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.tools.TimewindowFeeder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Ingest inter library loan codes from EZB web service into Elasticsearch
 */
public class EZBWeb extends TimewindowFeeder {

    private final static Logger logger = LogManager.getLogger(EZBWeb.class.getName());

    @Override
    public String getName() {
        return "ezb-web-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return EZBWeb::new;
    }

    protected String getIndex() {
        return settings.get("index", "ezbweb");
    }

    protected String getType() {
        return settings.get("type", "ezbweb");
    }

    @Override
    public void process(URI uri) throws Exception {
        IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();
        namespaceContext.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        namespaceContext.addNamespace("xbib", "http://xbib.org/elements/1.0/");
        Iterator<String> it = searchZDB();
        URL url;
        while (it.hasNext()) {
            String zdbid = it.next();
            StringBuilder sb = new StringBuilder();
            sb.append(zdbid).insert(sb.length() - 1, '-');
            url = new URL(uri + sb.toString());
            InputStream in = null;
            // EZB API is flaky, retry if "Host is down" is thrown
            for (int tries = 0; tries < 12; tries++) {
                try {
                    in = url.openStream();
                    if (in != null) {
                        break;
                    }
                } catch (SocketException e) {
                    logger.warn(e.getMessage());
                    Thread.sleep(5000L);
                }
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            br.readLine(); // ZDB-Id: ...
            br.readLine(); // Treffer: ...
            br.readLine(); // empty line
            Scanner scanner = new Scanner(br);
            scanner.useDelimiter("\t|\n");
            while (scanner.hasNext()) {
                try {
                    String sigel = scanner.next();
                    String isil = scanner.next();
                    String name = scanner.next(); // unused
                    String code1 = scanner.next();
                    String code2 = scanner.next();
                    String code3 = scanner.next();
                    String comment = scanner.next();
                    String firstDate = scanner.next();
                    String firstVolume = scanner.next();
                    String firstIssue = scanner.next();
                    String lastDate = scanner.next();
                    String lastVolume = scanner.next();
                    String lastIssue = scanner.next();
                    String movingWall = scanner.next();
                    // AAAAA entry ("free serials")
                    if ("AAAAA".equals(sigel)) {
                        isil = "DE-ALL";
                    }
                    // fixes
                    if ("0".equals(firstVolume)) {
                        firstVolume = null;
                    }
                    if ("0".equals(firstIssue)) {
                        firstIssue = null;
                    }
                    if ("0".equals(lastVolume)) {
                        lastVolume = null;
                    }
                    if ("0".equals(lastIssue)) {
                        lastIssue = null;
                    }
                    // firstdate, lastdate might be empty -> ok
                    String key = zdbid + "."
                            + isil + "."
                            + firstDate + "."
                            + lastDate + "."
                            + (movingWall.isEmpty() ? "0" : movingWall);
                    Resource resource = new MemoryResource().blank();
                    resource.add("dc:identifier", key)
                            .add("xbib:identifier", zdbid)
                            .add("xbib:isil", isil)
                            .add("xbib:firstDate", firstDate)
                            .add("xbib:firstVolume", firstVolume)
                            .add("xbib:firstIssue", firstIssue)
                            .add("xbib:lastDate", lastDate)
                            .add("xbib:lastVolume", lastVolume)
                            .add("xbib:lastIssue", lastIssue)
                            .add("xbib:movingWall", movingWall)
                            .add("xbib:interlibraryloanCode",
                                    (code1.isEmpty() ? "x" : code1)
                                            + (code2.isEmpty() ? "x" : code2)
                                            + (code3.isEmpty() ? "x" : code3))
                            .add("xbib:comment", comment);
                    RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext,
                            getConcreteIndex(), getType());
                    params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), key, content));
                    RdfContentBuilder builder = routeRdfXContentBuilder(params);
                    builder.receive(resource);
                    if (settings.getAsBoolean("mock", false)) {
                        logger.info("{}", builder.string());
                    }
                    if (executor != null) {
                        // tell executor we increased document count by one
                        executor.metric().mark();
                        if (executor.metric().count() % 10000 == 0) {
                            try {
                                writeMetrics(executor.metric(), null);
                            } catch (Exception e) {
                                throw new IOException("metric failed", e);
                            }
                        }
                    }
                } catch (NoSuchElementException e) {
                    logger.error(url + " " + e.getMessage(), e);
                }
            }
            br.close();
        }
        if (writer != null) {
            writer.close();
        }
        if (reader != null) {
            reader.close();
        }
        ingest.stopBulk(getConcreteIndex());
        if (settings.getAsBoolean("aliases", false) && !settings.getAsBoolean("mock", false) && ingest.client() != null) {
            updateAliases();
        } else {
            logger.info("not doing alias settings");
        }
    }

    private Iterator<String> searchZDB() throws IOException {
        List<String> list = new LinkedList<String>();
        Client client = ingest.client();
        if (client == null) {
            // mock
            return list.iterator();
        }
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("ElectronicLocationAndAccess.nonpublicnote", "EZB");
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setIndices("zdb")
                .setSize(1000)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(5000))
                .setQuery(queryBuilder)
                .addField("IdentifierZDB.identifierZDB");
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        long total = searchResponse.getHits().getTotalHits();
        logger.info("zdb identifier hits={}", total);
        while (searchResponse.getScrollId() != null) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(5000))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                String zdbid = hit.getFields().get("IdentifierZDB.identifierZDB").getValue();
                list.add(zdbid);
            }
        }
        logger.info("search complete");
        return list.iterator();
    }

}
