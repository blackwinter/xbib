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
package org.xbib.tools.feed.elasticsearch.orcid;

import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.Resource;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.rdf.memory.BlankMemoryResource;
import org.xbib.util.ArticleVocabulary;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ORCIDMapper implements ArticleVocabulary {

    private String orcid;

    private String familyName;

    private String givenName;

    private String mbox;

    private Map<String,String> ids = new HashMap<>();

    private String idtype;

    private List<Work> works = new LinkedList<>();

    private Work work;

    private String workidtype;

    public Resource map(Map<String, Object> map) throws IOException {
        Resource r = new BlankMemoryResource();
        map(r, null, map);
        if (work != null) {
            works.add(work);
        }
        if (!works.isEmpty()) {
            r.setId(IRI.create(orcid));
            r.newResource("hasName").a(FOAF_AGENT)
                    .add(FOAF_FAMILYNAME, familyName)
                    .add(FOAF_GIVENNAME, givenName)
                    .add(FOAF_NAME, givenName + " " + familyName)
                    .add(FOAF_MBOX, mbox);
            for (Map.Entry<String,String> id : ids.entrySet()) {
                r.newResource("hasIdentifier")
                        .add(DC_TYPE, id.getKey())
                        .add(DC_IDENTIFIER, id.getValue());
            }
            for (Work w : works) {
                StringBuilder sb = new StringBuilder();
                if (w.datestamp.year != null) {
                    sb.append(w.datestamp.year);
                }
                if (w.datestamp.month != null) {
                    sb.append('-').append(w.datestamp.month);
                }
                if (w.datestamp.day != null) {
                    sb.append('-').append(w.datestamp.day);
                }
                Resource workResource = r.newResource("hasWork")
                        .add(DC_TITLE, w.title)
                        .add(FRBR_PARTOF, w.relatedtitle)
                        .add(DC_DATE, w.datestamp.year)
                        .add(PRISM_PUBLICATIONDATE, sb.toString());
                w.dois.forEach(doi -> workResource.add(PRISM_DOI, doi));
                w.pmids.forEach(pmid -> workResource.add(FABIO_HAS_PUBMEDID, pmid));
            }
        }
        return r;
    }

    private void map(Resource r, String prefix, Map<String, Object> map) throws IOException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String p = prefix != null ? prefix + "." + key : key;
            Object value = entry.getValue();
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
            case "orcid-profile.orcid-identifier.uri" : {
                orcid = value;
                break;
            }
            case "orcid-profile.orcid-bio.personal-details.family-name.value" : {
                familyName = value;
                break;
            }
            case "orcid-profile.orcid-bio.personal-details.given-names.value" : {
                givenName = value;
                break;
            }
            case "orcid-profile.orcid-bio.contact-details.email" : {
                mbox = value.startsWith("mailto:") ? value :  "mailto:" + value;
                break;
            }
            case "orcid-profile.orcid-bio.external-identifiers.external-identifier.external-id-common-name.value" : {
                // "Scopus Author ID"
                idtype = value;
                break;
            }
            case "orcid-profile.orcid-bio.external-identifiers.external-identifier.external-id-reference.value value" : {
                ids.put(idtype, value);
                break;
            }
            case "orcid-profile.orcid-activities.orcid-works.orcid-work.put-code": {
                if (work != null) {
                    works.add(work);
                }
                work = new Work();
                break;
            }
            case "orcid-profile.orcid-activities.orcid-works.orcid-work.work-title.title.value" : {
                work.title = value;
                break;
            }
            case "orcid-profile.orcid-activities.orcid-works.orcid-work.work-title.subtitle.value value" : {
                // journal title
                work.relatedtitle = value;
                break;
            }
            case "orcid-profile.orcid-activities.orcid-works.orcid-work.publication-date.year.value" : {
                work.datestamp.year = value;
                break;
            }
            case "orcid-profile.orcid-activities.orcid-works.orcid-work.publication-date.month.value" : {
                work.datestamp.month = value;
                break;
            }
            case "orcid-profile.orcid-activities.orcid-works.orcid-work.publication-date.day.value" : {
                work.datestamp.day = value;
                break;
            }
            case "orcid-profile.orcid-activities.orcid-works.orcid-work.work-external-identifiers.work-external-identifier.work-external-identifier-type" : {
                // DOI, ISSN, ISBN
                workidtype = value;
                break;
            }
            case "orcid-profile.orcid-activities.orcid-works.orcid-work.work-external-identifiers.work-external-identifier.work-external-identifier-id.value" : {
                switch (workidtype) {
                    case "DOI" :
                        int pos = value.indexOf('\n'); // ORCID data error?
                        if (pos >= 0) {
                            value = value.substring(pos+1);
                        }
                        work.dois.add(value);
                        break;
                    case "PMID" :
                        work.pmids.add(value);
                        break;
                    case "ISBN" :
                        work.isbns.add(value);
                        break;
                    default:
                        break;
                }
                break;
            }
            default:
                break;
        }
    }

    public AbstractXmlHandler getHandler(RdfContentParams params) {
        return new ORCIDMapper.Handler(params)
                .setDefaultNamespace("", "http://xbib.org/orcid");
    }

    static class Handler extends AbstractXmlResourceHandler {

        public Handler(RdfContentParams params) {
            super(params);
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return false;
        }

        @Override
        public boolean skip(QName name) {
            return name.getLocalPart().startsWith("@");
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            if ("uri".equals(name.getLocalPart())) {
                resource.setId(IRI.create(value));
            }
        }

        @Override
        public XmlHandler setNamespaceContext(IRINamespaceContext namespaceContext) {
            return this;
        }

        @Override
        public IRINamespaceContext getNamespaceContext() {
            return params.getNamespaceContext();
        }
    }

    static class Work implements Comparable<Work> {
        String title;
        String relatedtitle;
        Datestamp datestamp = new Datestamp();
        Set<String> dois = new LinkedHashSet<>();
        Set<String> pmids = new LinkedHashSet<>();
        Set<String> isbns = new LinkedHashSet<>();

        @Override
        public int compareTo(Work o) {
            if (!dois.isEmpty() && !o.dois.isEmpty()) {
                return dois.toString().compareTo(o.dois.toString());
            }
            if (!pmids.isEmpty() && !o.pmids.isEmpty()) {
                return pmids.toString().compareTo(o.pmids.toString());
            }
            return title.toLowerCase().compareTo(o.title.toLowerCase());
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Work && title.equals(((Work) o).title);
        }

        @Override
        public int hashCode() {
            return title.hashCode();
        }
    }

    static class Datestamp {
        String year, month, day;
    }

}
