/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2015 Jörg Prante and xbib
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
package org.xbib.tools.merge.holdingslicenses.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class BibdatLookup {

    private final static Logger logger = LogManager.getLogger(BibdatLookup.class);

    private Map<String, String> name = new HashMap<>();

    private Map<String, String> region = new HashMap<>();

    private Map<String, String> organization = new HashMap<>();

    private Map<String, String> other = new HashMap<>();

    public void buildLookup(Client client, String index) throws IOException {
        int size = 1000;
        long millis = 1000L;
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setIndices(index)
                .setSize(size)
                .setScroll(TimeValue.timeValueMillis(millis))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        logger.info("bibdat index size = {}", searchResponse.getHits().getTotalHits());
        do {
            for (SearchHit hit : searchResponse.getHits()) {
                Map<String, Object> m = hit.getSource();
                // always set name, region, organization
                String name = m.containsKey("ShortName") ?
                        (String) ((Map<String, Object>) m.get("ShortName")).get("name") : null;
                String region = m.containsKey("LibraryService") ?
                        (String) ((Map<String, Object>) m.get("LibraryService")).get("libraryServiceRegion") : null;
                String organization = m.containsKey("LibraryService") ?
                        (String) ((Map<String, Object>) m.get("LibraryService")).get("libraryServiceOrganization") : null;

                // filter out "real libraries"

                // ISIL?
                String key = m.containsKey("Identifier") ?
                        (String) ((Map<String, Object>) m.get("Identifier")).get("identifierAuthorityISIL") : null;
                if (key == null) {
                    // no ISIL, skip
                    continue;
                }
                // Library organization type (statistical information) ?
                String type = m.containsKey("Organization") ?
                        (String) ((Map<String, Object>) m.get("Organization")).get("organizationType") : null;
                if (type == null) {
                    // no library organization type, skip
                    continue;
                }
                // Library organization state = "Adresse" ?
                String state = m.containsKey("Organization") ?
                        (String) ((Map<String, Object>) m.get("Organization")).get("organizationState") : null;
                if (state == null) {
                    // no library address, skip
                    continue;
                }
                if ("Adresse".equals(state) ) {
                    switch (type) {
                        // select only these types
                        case "Abteilungsbibliothek, Institutsbibliothek, Fachbereichsbibliothek (Universität)":
                        case "Wissenschaftliche Spezialbibliothek":
                        case "Öffentliche Bibliothek":
                        case "Mediathek":
                        case "Zentrale Hochschulbibliothek, nicht Universität":
                        case "Zentrale Universitätsbibliothek":
                        case "Abteilungsbibliothek, Fachbereichsbibliothek (Hochschule, nicht Universität)":
                        case "Regionalbibliothek":
                        case "Öffentliche Bibliothek für besondere Benutzergruppen":
                        case "Nationalbibliothek":
                        case "Zentrale Fachbibliothek":
                        case "Verbundsystem/ -katalog":
                            if (name != null && !this.name.containsKey(key)) {
                                this.name.put(key, name);
                            }
                            if (region != null && !this.region.containsKey(key)) {
                                this.region.put(key, region);
                            }
                            if (organization != null && !this.organization.containsKey(key)) {
                                this.organization.put(key, organization);
                            }
                            break;
                        default:
                            logger.warn("library organization type is {} for {}, skipped", type, key);
                            if (!other.containsKey(key)) {
                                // save at least region
                                other.put(key, region);
                            }
                            break;
                    }
                } else {
                    if (region != null && !other.containsKey(key)) {
                        other.put(key, region);
                    }
                }
            }
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
        } while (searchResponse.getHits().getHits().length > 0);
    }

    public Map<String, String> lookupName() {
        return name;
    }

    public Map<String, String> lookupRegion() {
        return region;
    }

    public Map<String, String> lookupOrganization() {
        return organization;
    }

    public Map<String, String> lookupOther() {
        return other;
    }

}
