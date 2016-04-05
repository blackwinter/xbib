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
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.BlankMemoryResource;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.util.ArticleVocabulary;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BioMedCentralMapper implements ArticleVocabulary {

    private final static Logger logger = LogManager.getLogger(BioMedCentralMapper.class);

    private StringBuilder title = new StringBuilder();

    private Set<Author> authors = new LinkedHashSet<>();

    private Author author;

    private String date;

    private String volume;

    private String issue;

    private String pagination;

    private String journal;

    private String issn;

    private String doi;

    public Resource map(Map<String, Object> map) throws IOException {
        Resource r = new BlankMemoryResource();
        map(r, null, map);
        r.add(DC_TITLE, title.toString());
        String year = date.substring(0,4);
        r.add(DC_DATE, new MemoryLiteral(year).type(Literal.GYEAR));
        r.add(PRISM_DOI, doi);
        r.newResource(FRBR_EMBODIMENT)
                .a(FABIO_PERIODICAL_ISSUE)
                .add(PRISM_NUMBER, issue);
        r.newResource(FRBR_EMBODIMENT)
                .a(FABIO_PERIODICAL_VOLUME)
                .add(PRISM_VOLUME, volume);
        r.newResource(FRBR_EMBODIMENT)
                .a(FABIO_PRINT_OBJECT)
                .add(PRISM_STARTING_PAGE, pagination);
        r.newResource(FRBR_PARTOF)
                .a(FABIO_JOURNAL)
                .add(PRISM_PUBLICATIONNAME, journal)
                .add(PRISM_ISSN, issn);
        WorkAuthor wa = new WorkAuthor()
                .workName(title)
                .chronology(year);
        for (Author author : authors) {
            wa.authorNameWithForeNames(author.lastName, author.foreName);
            r.newResource(DC_CREATOR)
                    .a(FOAF_AGENT)
                    .add(FOAF_FAMILYNAME, author.lastName)
                    .add(FOAF_GIVENNAME, author.foreName);
        }
        r.add(XBIB_KEY, wa.createIdentifier());
        return r;
    }

    @SuppressWarnings("unchecked")
    private void map(Resource r, String p, Map<String, Object> map) throws IOException {
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String path = p != null ? p + "." + key : key;
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
        logger.info("path={} value={}", path, value);
        switch (path) {
            case "bibl.title.p.it" : {
                if (title.length() > 0) {
                    title.append(" ");
                }
                title.append(value);
                break;
            }
            case "bibl.aug.au.snm" : {
                if (author != null) {
                    if (author.lastName != null) {
                        authors.add(author);
                        author = new Author();
                    }
                } else {
                    author = new Author();
                }
                author = new Author();
                author.setLastName(value);
                break;
            }
            case "bibl.aug.au.fnm" : {
                if (author != null) {
                    if (author.foreName != null) {
                        authors.add(author);
                        author = new Author();
                    }
                } else {
                    author = new Author();
                }
                author.setForeName(value);
                break;
            }
            case "bibl.aug.au.cnm" : {
                break;
            }
            case "bibl.insg.ins.p" : {
                break;
            }
            case "bibl.source" : {
                journal = value;
                break;
            }
            case "bibl.issn" : {
                issn = value;
                break;
            }
            case "bibl.pubdate" : {
                date = value;
                break;
            }
            case "bibl.volume" : {
                volume = value;
                break;
            }
            case "bibl.issue" : {
                issue = value;
                break;
            }
            case "bibl.fpage" : {
                pagination = value;
                break;
            }
            case "bibl.xrefbib.pubidlist.pubid" : {
                // doi
                doi = value;
                break;
            }
            default:
                break;
        }
    }

    static class Author implements Comparable<Author> {
        private String lastName, foreName, normalized;

        void setLastName(String lastName) {
            this.lastName = lastName;
            this.normalized = normalize();
        }
        void setForeName(String foreName) {
            this.foreName = foreName;
            this.normalized = normalize();
        }

        private String normalize() {
            StringBuilder sb = new StringBuilder();
            if (lastName != null) {
                sb.append(lastName);
            }
            if (foreName != null) {
                sb.append(' ').append(foreName);
            }
            return sb.toString();
        }

        @Override
        public int compareTo(Author o) {
            return normalized.compareTo(o.normalized);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Author && normalized.equals(((Author)o).normalized);
        }

        @Override
        public int hashCode() {
            return normalized.hashCode();
        }
    }

}
