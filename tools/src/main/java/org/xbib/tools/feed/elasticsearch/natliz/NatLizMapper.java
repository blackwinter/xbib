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
package org.xbib.tools.feed.elasticsearch.natliz;

import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.iri.IRI;
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

public class NatLizMapper implements ArticleVocabulary {

    private Resource journal;

    private Set<Author> authors = new LinkedHashSet<>();

    private String title;

    private String year;

    public Resource map(Map<String, Object> map) throws IOException {
        Resource r = new BlankMemoryResource();
        journal = r.newResource("frbr:isPartOf");
        map(r, null, map);
        WorkAuthor wa = new WorkAuthor()
                .workName(title)
                .chronology(year);
        for (Author author : authors) {
            wa.authorNameWithForeNames(author.lastName, author.foreName);
        }
        String key = wa.createIdentifier();
        r.add(XBIB_KEY, key);
        r.setId(IRI.create("http://xbib.info/key/" + key));
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
        switch (path) {
            case "RecordIdentifier.identifierForTheRecord" : {
                r.add(DC_IDENTIFIER, value);
                break;
            }
            case "TitleStatement.titleMain" : {
                if (value.endsWith(".")) {
                    value = value.substring(0, value.length()-1);
                }
                r.add(DC_TITLE, value);
                title = value;
                break;
            }
            case "PersonCreator.personName" :{
                if (value.indexOf(',') > 0) {
                    // switch lastname, forename
                    String[] s = value.split(", ");
                    Author author = new Author();
                    author.setLastName(s[0]);
                    author.setForeName(s.length > 1 ? s[1] : null);
                    authors.add(author);
                    r.newResource(DC_CREATOR)
                            .a(FOAF_AGENT)
                            .add(FOAF_FAMILYNAME, author.lastName)
                            .add(FOAF_GIVENNAME, author.foreName );
                } else {
                    Author author = new Author();
                    author.setLastName(value);
                    authors.add(author);
                    r.newResource(DC_CREATOR)
                            .a(FOAF_AGENT)
                            .add(FOAF_NAME, author.lastName);
                }
                break;
            }
            case "CorporateBody.corporateBodyName" : {
                r.newResource(DC_CONTRIBUTOR)
                        .a(FOAF_AGENT)
                        .add(FOAF_NAME, value);

                break;
            }
            case "CreatorStatement.creatorStatement" : {
                r.newResource(DC_CREATOR)
                        .a(FOAF_AGENT)
                        .add(FOAF_NAME, value);
                Author author = new Author();
                author.setLastName(value);
                authors.add(author);
                break;
            }
            case "SourceIdentifierISSN.identifierOfTheSource" : {
                journal.add(PRISM_ISSN, value);
                break;
            }
            case "OnlineAccess.uri" : {
                if (value.startsWith("10.")) {
                    r.add(PRISM_DOI, value);
                } else if (value.startsWith("http://dx.doi.org/")) {
                    value = value.substring("http://dx.doi.org/".length());
                    r.add(PRISM_DOI, value);
                } else if (value.startsWith("doi:")) {
                    value = value.substring("doi:".length());
                    r.add(PRISM_DOI, value);
                } else {
                    r.add(DC_IDENTIFIER, value);
                }
                break;
            }
            case "DateProper.date" : {
                r.add(PRISM_PUBLICATIONDATE, value);
                if (value.length() >= 4) {
                    year = value.substring(0,4);
                    r.add(DC_DATE, new MemoryLiteral(year).type(Literal.GYEAR));
                }
                break;
            }
            case "PublisherName.printerName" : {
                journal.add(DC_PUBLISHER, value);
                break;
            }
            case "PublicationPlace.printingPlace" : {
                break;
            }
            case "SourceTitle.source" : {
                if (value.endsWith(".")) {
                    value = value.substring(0, value.length()-1);
                }
                journal.add(PRISM_PUBLICATIONNAME, value);
                break;
            }
            case "SourcePublisherPlace.source" : {
                break;
            }
            case "SourceDescriptionVolume.source" : {
                r.add(DCTERMS_BIBLIOGRAPHIC_CITATION, value);
                // parse embodiments "11 (1968), S. 639-644"
                String[] s = value.split(",");
                String volissue = s.length > 0 ? s[0] : "";
                if (!volissue.isEmpty()) {
                    // cut after blank
                    int pos = volissue.indexOf(' ');
                    volissue = pos > 0 ? volissue.substring(0, pos) : volissue;
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PERIODICAL_ISSUE)
                            .add(PRISM_NUMBER, volissue);
                }
                String pagination =  s.length > 1 ? s[1] : "";
                int pos = pagination.indexOf('-');
                if (pos > 0) {
                    String spage = pagination.substring(0, pos);
                    String epage = pagination.substring(pos + 1);
                    int l = spage.length() - epage.length();
                    if (l > 0) {
                        epage = spage.substring(0, l) + epage;
                    }
                    // remove "S. "
                    spage = spage.replaceAll("S\\.", "").trim();
                    epage = epage.replaceAll("S\\.", "").trim();
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PRINT_OBJECT)
                            .add(PRISM_STARTING_PAGE, spage)
                            .add(PRISM_ENDING_PAGE, epage);
                } else if (!pagination.isEmpty()) {
                    pagination = pagination.replaceAll("S\\.","").trim();
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PRINT_OBJECT)
                            .add(PRISM_PAGERANGE, pagination);
                }

                break;
            }
            case "ClassifierDigitization.classifier" : {
                r.add(DC_SOURCE, value);
                break;
            }
            case "Annotation.abstract" : {
                r.add(DCTERMS_ABSTRACT, value);
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
