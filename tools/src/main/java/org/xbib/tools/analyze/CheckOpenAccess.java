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
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;

import java.io.BufferedReader;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class CheckOpenAccess extends Analyzer {

    private final static Logger logger = LogManager.getLogger(CheckOpenAccess.class);

    @Override
    public int run(Settings settings) throws Exception {
        try {
            SearchTransportClient search = new SearchTransportClient().init(Settings.settingsBuilder()
                    .put("cluster.name", settings.get("elasticsearch.cluster"))
                    .put("host", settings.get("elasticsearch.host"))
                    .put("port", settings.getAsInt("elasticsearch.port", 9300))
                    .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                    .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                    .build().getAsMap());
            Client client = search.client();
            BufferedReader fileReader = getFileReader(settings.get("input"));
            String line;
            int oa = 0;
            int nonoa = 0;
            while ((line = fileReader.readLine()) != null) {
                String[] s = line.split(",");
                if (s.length != 2 && s.length != 3) {
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
                QueryBuilder queryBuilder =
                        boolQuery().must(termQuery("_id", zdbid)).filter(termQuery("openaccess", true));
                SearchRequestBuilder countRequestBuilder = client.prepareSearch()
                        .setIndices(settings.get("ezdb-index", "ezdb"))
                        .setTypes(settings.get("ezdb-type", "Manifestation"))
                        .setSize(0)
                        .setQuery(queryBuilder);
                SearchResponse countResponse = countRequestBuilder.execute().actionGet();
                if (countResponse.getHits().getTotalHits() > 0) {
                    oa += count;
                } else {
                    nonoa += count;
                }
            }
            fileReader.close();
            logger.info("oa={} nonoa={}", oa, nonoa);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return 1;
        }
        return 0;
    }

}