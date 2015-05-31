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
package org.xbib.tools.feed.elasticsearch.oai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.tools.util.ArticleVocabulary;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DOAJArticleMapper implements ArticleVocabulary {

    private final static Logger logger = LogManager.getLogger(DOAJArticleMapper.class);

    public Resource map(Map<String, Object> map) throws IOException {
        Resource r = new MemoryResource();
        map(r, null, map);
        return r;
    }

    private void map(Resource r, String p, Map<String, Object> map) throws IOException {
        for (String key : map.keySet()) {
            String path = p != null ? p + "." + key : key;
            Object value = map.get(key);
            if (value instanceof Map) {
                map(r, path, (Map<String, Object>) value);
            } else if (value instanceof List) {
                for (Object o : (List) value) {
                    if (o instanceof Map) {
                        map(r, path, (Map<String, Object>) o);
                    } else {
                        map(r, path, o.toString());
                    }
                }
            } else {
                if (value != null) {
                    map(r, path, value.toString());
                }
            }
        }
    }

    public void map(Resource r, String path, String value) throws IOException {
        switch (path) {
            case "title" : {
                r.add(DC_TITLE, value);
                break;
            }
            case "creator" : {
                r.add(DC_CREATOR, value);
                break;
            }
            case "identifier" : {
                if (value.startsWith("10.")) {
                    r.add(PRISM_DOI, value);
                } else if (value.startsWith("https://doaj.org/")) {
                    r.add(XBIB_DOAJ_ID, value);
                } else if (value.length() == 9 && value.indexOf('-') == 4) {
                    r.add(PRISM_ISSN, value);
                } else if (value.startsWith("http://dx.doi.org/")) {
                    value = value.substring("http://dx.doi.org/".length());
                    r.add(PRISM_DOI, value);
                } else if (value.startsWith("doi:")) {
                    value = value.substring("doi:".length());
                    r.add(PRISM_DOI, value);
                } else {
                    logger.warn("unknown identifier: {}", value);
                }
                break;
            }
            case "date" : {
                r.add(PRISM_PUBLICATIONDATE, value);
                if (value.length() >= 4) {
                    r.add(DC_DATE,  new MemoryLiteral(value.substring(0,4)).type(Literal.GYEAR));
                }
                break;
            }
            case "publisher" : {
                r.add(DC_PUBLISHER, value);
                break;
            }
            case "type" : {
                r.add(DC_TYPE, value);
                break;
            }
            case "rights" : {
                r.add(DC_RIGHTS, value);
                break;
            }
            case "description" : {
                value = value.replaceAll("\\<[^>]*>","");
                r.add(DCTERMS_ABSTRACT, value);
                break;
            }
            case "relation" : {
                if (!value.startsWith("https://doaj.org/")) {
                    r.add(DC_SOURCE, value);
                }
                break;
            }
            case "source" : {
                r.add(DCTERMS_BIBLIOGRAPHIC_CITATION, value);
                break;
            }
        }
    }

}
