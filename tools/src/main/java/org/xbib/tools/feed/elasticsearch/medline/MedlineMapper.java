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
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.tools.util.ArticleVocabulary;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MedlineMapper implements ArticleVocabulary {

    private final static Logger logger = LogManager.getLogger(MedlineMapper.class);

    private Set<Author> authors = new LinkedHashSet<>();

    private Author author;

    private Datestamp pubdate, articledate, creationdate;

    private String medlinedate;

    private String title;

    private String volume;

    private String issue;

    private String pagination;

    private String journal;

    private String issn;

    private String shortTitle;

    MedlineMapper() {
    }

    public Resource map(Map<String, Object> map) throws IOException {
        Resource r = new MemoryResource().a(FABIO_ARTICLE);
        author = new Author();
        map(r, null, map);
        String year = null;
        if (pubdate == null) {
            pubdate = articledate;
        }
        if (pubdate == null) {
            pubdate = creationdate;
        }
        if (pubdate != null) {
            year = pubdate.year.substring(0,4);
            r.add(DC_DATE, new MemoryLiteral(year).type(Literal.GYEAR));
            StringBuilder sb = new StringBuilder();
            if (pubdate.year != null) {
                sb.append(pubdate.year);
            }
            if (pubdate.month != null) {
                sb.append('-').append(pubdate.month);
            }
            if (pubdate.day != null) {
                sb.append('-').append(pubdate.day);
            }
            r.add(PRISM_PUBLICATIONDATE, sb.toString());
        } else if (medlinedate != null) {
            year = medlinedate.substring(0,4);
            r.add(DC_DATE, new MemoryLiteral(year).type(Literal.GYEAR));
            r.add(PRISM_PUBLICATIONDATE, medlinedate);
        }
        if (author != null && author.lastName != null) {
            authors.add(author);
            r.newResource(DC_CREATOR)
                    .a(FOAF_AGENT)
                    .add(FOAF_FAMILYNAME, author.lastName)
                    .add(FOAF_GIVENNAME, author.foreName);
        }
        r.newResource(FRBR_PARTOF)
                .a(FABIO_JOURNAL)
                .add(PRISM_PUBLICATIONNAME, journal)
                .add(PRISM_ISSN, issn)
                .add(FABIO_HAS_SHORT_TITLE, shortTitle);
        WorkAuthor wa = new WorkAuthor()
                .workName(title)
                .chronology(year);
        for (Author author : authors) {
            wa.authorNameWithForeNames(author.lastName, author.foreName);
        }
        r.add(XBIB_KEY, wa.createIdentifier());
        return r;
    }

    private void map(Resource r, String prefix, Map<String, Object> map) throws IOException {
        for (String key : map.keySet()) {
            String p = prefix != null ? prefix + "." + key : key;
            Object value = map.get(key);
            if (value instanceof Map) {
                map(r, p, (Map<String, Object>) value);
            } else if (value instanceof List) {
                for (Object o : (List) value) {
                    if (o instanceof Map) {
                        map(r, p, (Map<String, Object>) o);
                    } else {
                        map(r, p, o.toString());
                    }
                }
            } else {
                if (value != null) {
                    map(r, p, value.toString());
                }
            }
        }
    }

    public void map(Resource r, String key, String value) throws IOException {
        switch (key) {
            case "ml:PMID": {
                IRI dereferencable = IRI.builder().scheme("http").host("xbib.info")
                        .path("/pmid/").fragment(value).build();
                r.id(dereferencable);
                r.add(FABIO_HAS_PUBMEDID, value);
                r.add(DC_IDENTIFIER, "PMID:" + value);
                break;
            }
            case "ml:OtherID": {
                r.add(DC_IDENTIFIER, value);
                if (value.startsWith("PMC")) {
                    r.add(FABIO_HAS_PUBMEDCENTRALID, value.substring(3));
                }
                break;
            }
            case "ml:Article.ml:ArticleTitle": {
                title = value;
                if (title.endsWith(".")) {
                    title = title.substring(0, value.length()-1);
                }
                r.add(DC_TITLE, title);
                break;
            }
            case "ml:Article.ml:Journal.ml:ISSN": {
                issn =value;
                break;
            }
            case "ml:Article.ml:Journal.ml:Title": {
                journal = value;
                break;
            }
            case "ml:Article.ml:Journal.ml:ISOAbbreviation": {
                shortTitle = value;
                break;
            }
            case "ml:Article.ml:AuthorList.ml:Author.ml:ForeName": {
                if (author == null) {
                    author = new Author();
                }
                author.foreName = value;
                break;
            }
            case "ml:Article.ml:AuthorList.ml:Author.ml:LastName": {
                if (author == null) {
                    author = new Author();
                } else if (author.lastName != null) {
                    authors.add(author);
                    r.newResource(DC_CREATOR)
                            .a(FOAF_AGENT)
                            .add(FOAF_FAMILYNAME, author.lastName)
                            .add(FOAF_GIVENNAME, author.foreName);
                    author = new Author();
                }
                author.lastName = value;
                break;
            }
            case "ml:Article.ml:Journal.ml:JournalIssue.ml:PubDate.ml:MedlineDate": {
                medlinedate = value;
                break;
            }
            case "ml:Article.ml:Journal.ml:JournalIssue.ml:PubDate.ml:Year":
            {
                if (pubdate == null) {
                    pubdate = new Datestamp();
                }
                pubdate.year = value;
                break;
            }
            case "ml:Article.ml:Journal.ml:JournalIssue.ml:PubDate.ml:Month": {
                if (pubdate == null) {
                    pubdate = new Datestamp();
                }
                pubdate.month = value;
                break;
            }
            case "ml:Article.ml:Journal.ml:JournalIssue.ml:PubDate.ml:Day": {
                if (pubdate == null) {
                    pubdate = new Datestamp();
                }
                pubdate.day = value;
                break;
            }
            case "ml:Article.ml:ArticleDate.ml:Year": {
                if (articledate == null) {
                    articledate = new Datestamp();
                }
                articledate.year = value;
                break;
            }
            case "ml:Article.ml:ArticleDate.ml:Month": {
                if (articledate == null) {
                    articledate = new Datestamp();
                }
                articledate.month = value;
                break;
            }
            case "ml:Article.ml:ArticleDate.ml:Day": {
                if (articledate == null) {
                    articledate = new Datestamp();
                }
                articledate.day = value;
                break;
            }
            case "ml:DateCreated.ml:Year":
            case "ml:Article.ml:Journal.ml:JournalIssue.ml:DateCreated.ml:Year":
            {
                if (creationdate == null) {
                    creationdate = new Datestamp();
                }
                creationdate.year = value;
                break;
            }
            case "ml:DateCreated.ml:Month":
            case "ml:Article.ml:Journal.ml:JournalIssue.ml:DateCreated.ml:Month":
            {
                if (creationdate == null) {
                    creationdate = new Datestamp();
                }
                creationdate.month = value;
                break;
            }
            case "ml:DateCreated.ml:Day":
            case "ml:Article.ml:Journal.ml:JournalIssue.ml:DateCreated.ml:Day":
            {
                if (creationdate == null) {
                    creationdate = new Datestamp();
                }
                creationdate.day = value;
                break;
            }
            case "ml:Article.ml:Journal.ml:JournalIssue.ml:Issue": {
                issue = value;
                r.newResource(FRBR_EMBODIMENT)
                        .a(FABIO_PERIODICAL_ISSUE)
                        .add(PRISM_NUMBER, value);
                break;
            }
            case "ml:Article.ml:Journal.ml:JournalIssue.ml:Volume": {
                volume = value;
                r.newResource(FRBR_EMBODIMENT)
                        .a(FABIO_PERIODICAL_VOLUME)
                        .add(PRISM_VOLUME, value);
                break;
            }
            case "ml:Article.ml:Pagination.ml:MedlinePgn": {
                pagination = value;
                int pos = pagination.indexOf('-');
                if (pos > 0) {
                    String spage = pagination.substring(0, pos);
                    String epage = pagination.substring(pos + 1);
                    int l = spage.length() - epage.length();
                    if (l > 0) {
                        epage = spage.substring(0, l) + epage;
                    }
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PRINT_OBJECT)
                            .add(PRISM_STARTING_PAGE, spage)
                            .add(PRISM_ENDING_PAGE, epage);
                } else {
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PRINT_OBJECT)
                            .add(PRISM_PAGERANGE, value);
                }
                break;
            }
            case "ml:Article.ml:ELocationID": {
                r.add(PRISM_DOI, value);
                break;
            }
            case "ml:Article.ml:Abstract.ml:AbstractText" : {
                r.newResource(FRBR_PART)
                        .a(FABIO_ABSTRACT)
                        .add(DCTERMS_ABSTRACT, value);
                break;
            }
            case "ml:MeshHeadingList.ml:MeshHeading.ml:DescriptorName" : {
                r.newResource(FABIO_HAS_SUBJECT_TERM)
                        .a(FABIO_SUBJECT_TERM)
                        .add(DC_SOURCE, "MeSH")
                        .add(RDFS_LABEL, value);
                break;
            }
            case "ml:CitationSubset" : {
                switch (value) {
                    case "AIM" :
                        value = "Abridged Index Medicus";
                        break;
                    case "B" :
                        value = "Biotechnology";
                        break;
                    case "C" :
                        value = "Communication Disorders";
                        break;
                    case "D" :
                        value = "Dental journals";
                        break;
                    case "E" :
                        value = "Ethics journals";
                        break;
                    case "F" :
                        value = "Older journals";
                        break;
                    case "H" :
                        value = "Health administration journals";
                        break;
                    case "IM" :
                        value = "Index Medicus";
                        break;
                    case "J" :
                        value = "Population information journals";
                        break;
                    case "K" :
                        value = "Consumer health journals";
                        break;
                    case "N" :
                        value = "Nursing journals";
                        break;
                    case "OM" :
                        value = "Old Medline";
                        break;
                    case "Q" :
                        value = "History of medicine journals";
                        break;
                    case "QIS" :
                        value = "non-Index Medicus History of medicine journals";
                        break;
                    case "QO" :
                        value = "older History of medicine journals";
                        break;
                    case "R" :
                        value = "Population and reproduction journals";
                        break;
                    case "S" :
                        value = "Space life sciences journals";
                        break;
                    case "T" :
                        value = "Health technology assessment journals";
                        break;
                    case "X" :
                        value = "AIDS/HIV journals";
                        break;

                }
                r.add(DC_SOURCE, value);
                break;
            }
        }
    }

    class Author implements Comparable<Author> {
        String lastName, foreName;

        String normalize() {
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
            return normalize().compareTo(o.normalize());
        }
    }

    class Datestamp {
        String year, month, day;
    }


}
