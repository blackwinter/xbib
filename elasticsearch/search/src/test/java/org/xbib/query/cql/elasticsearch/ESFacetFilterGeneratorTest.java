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
package org.xbib.query.cql.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elasticsearch.search.CQLRequest;

/**
 *  Elasticsearch filter generation
 */
public class ESFacetFilterGeneratorTest extends Assert {

    private final static Logger logger = LogManager.getLogger(ESFacetFilterGeneratorTest.class.getName());

    @Test
    public void testFacetFilter() throws Exception {

        String cql = "Köln";
        String cqlFilter = "dc.format = online and dc.date = 2012";

        // creating CQL from SearchRetrieve request
        CQLRequest cqlRequest  = new CQLRequest().newRequest()
                .index("*")
                .type("*")
                .from(0)
                .size(10)
                .cql(cql)
                .cqlFilter(cqlFilter)
                .cqlFacetFilter(cqlFilter)
                .facet("10:dc.format", null, null)
                ;
        assertEquals("{\"from\":0,\"size\":10,\"query\":{\"match\":{\"_all\":{\"query\":\"Köln\"}}},\"filter\":{\"query\":{\"bool\":{\"must\":{\"term\":{\"dc:format.dcterms:medium\":\"online\"}},\"must\":{\"term\":{\"dc:date.xbib:date\":\"2012\"}}}}}}", cqlRequest.toString());
    }

}
