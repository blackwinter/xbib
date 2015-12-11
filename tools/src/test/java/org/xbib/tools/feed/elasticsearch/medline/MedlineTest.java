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
package org.xbib.tools.feed.elasticsearch.medline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xbib.common.settings.Settings;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RdfXContentParams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;

public class MedlineTest {

    private final static Logger logger = LogManager.getLogger(MedlineTest.class);

    @Test
    public void testMedline() throws Exception {
        URL url = getClass().getClassLoader().getResource("org/xbib/tools/feed/elasticsearch/medline/medline.xml");
        if (url != null) {
            logger.info("{}", url.toURI());
            Medline medline = new Medline();
            Settings settings = Settings.settingsBuilder()
                    .put("uri", url.toString())
                    .put("mock", true)
                    .build();
            medline.bootstrap(settings.getAsReader(), new StringWriter());
        } else {
            logger.warn("not found");
        }
    }

    @Test
    public void testMedlineFabio() throws Exception {
        IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();
        namespaceContext.add(new HashMap<String, String>() {{
                put(RdfConstants.NS_PREFIX, RdfConstants.NS_URI);
                put("dc", "http://purl.org/dc/elements/1.1/");
                put("dcterms", "http://purl.org/dc/terms/");
                put("foaf", "http://xmlns.com/foaf/0.1/");
                put("frbr", "http://purl.org/vocab/frbr/core#");
                put("fabio", "http://purl.org/spar/fabio/");
                put("prism", "http://prismstandard.org/namespaces/basic/3.0/");
            }});

        URL url = getClass().getClassLoader().getResource("org/xbib/tools/feed/elasticsearch/medline/medline.json");
        if (url != null) {
            Map<String, Object> map = XContentHelper.convertFromJsonToMap(new InputStreamReader(url.openStream(), "UTF-8"));
            MedlineMapper mf = new MedlineMapper();
            RdfContentBuilder builder;
            try {
                RdfXContentParams params = new RdfXContentParams(namespaceContext);
                builder = rdfXContentBuilder(params);
                builder.receive(mf.map(map));
                logger.info("medline mapper result={}", params.getGenerator().get());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }
}
