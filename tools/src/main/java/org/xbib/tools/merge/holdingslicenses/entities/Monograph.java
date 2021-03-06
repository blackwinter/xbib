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
package org.xbib.tools.merge.holdingslicenses.entities;

import org.xbib.common.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Monograph extends TitleRecord {

    private final static String[] PERSONS = new String[] {
            "Person", "PersonCreator", "PersonContributor", "PersonAddressee"
    };

    private final static String[] CORPORATEBODIES = new String[] {
            "CorporateBody"
    };

    private List<Map<String,Object>> person;

    private List<Map<String,Object>> corporateBody;

    private Map<String,Object> conference;

    private String volumeDesignation;

    private String numbering;

    private List<String> genres;

    public Monograph(Map<String, Object> map) {
        super(map);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getVolumeDesignation() {
        return volumeDesignation;
    }

    public String getNumbering() {
        return numbering;
    }

    public List<Map<String,Object>> getPerson() {
        return person;
    }

    public List<Map<String,Object>> getCorporateBody() {
        return corporateBody;
    }

    public List<String> getGenres() {
        return genres;
    }

    public Map<String,Object> getConference() {
        return conference;
    }

    @Override
    protected void build() {
        makeIdentity();
        makePerson();
        makeCorporate();
        makeConference();
        makeTitle();
        makePublisher();
        makeDate();
        makeGenre();
        makeIdentifiers();
    }

    protected void makeIdentity() {
        String s = getString("RecordIdentifier.identifierForTheRecord");
        this.identifier = s != null ? s : "undefined";
        this.externalID = this.identifier;
    }

    protected void makePerson() {
        this.person = new ArrayList<>();
        for (String s : PERSONS) {
            Object o = getMap().get(s);
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            for (Map<String,Object> m : (List<Map<String, Object>>) o) {
                Map<String,Object> map = new HashMap<>();
                map.putAll(m);
                map.put("type", s);
                person.add(map);
            }
        }
    }

    @Override
    protected void makeCorporate() {
        this.corporateBody = new ArrayList<>();
        for (String s : CORPORATEBODIES) {
            Object o = getMap().get(s);
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            for (Map<String,Object> m : (List<Map<String, Object>>) o) {
                Map<String,Object> map = new HashMap<>();
                map.putAll(m);
                map.put("type", s);
                corporateBody.add(map);
            }
        }
    }

    protected void makeConference() {
        this.conference = getAnyObject("Conference");
    }

    @Override
    protected void makeTitle() {
        // shorten title (series statement after '/' or ':')
        // but combine with corporate name, meeting name, and part specification
        StringBuilder sb = new StringBuilder();
        String titleMain = getString("TitleStatement.titleMain");
        sb.append(clean(titleMain));
        titleComponents = new LinkedHashSet<>();
        titleComponents.addAll(split(titleMain));
        String titleRemainder = getString("TitleStatement.titleRemainder");
        if (!Strings.isNullOrEmpty(titleRemainder)) {
            sb.append(" ; ").append(titleRemainder);
            titleComponents.addAll(split(titleRemainder));
        }
        String titleAddendum = getString("TitleAddendum.title");
        if (!Strings.isNullOrEmpty(titleAddendum)) {
            sb.append(" ; ").append(titleAddendum);
            titleComponents.addAll(split(titleAddendum));
        }
        String subSeriesStatement = getString("SubseriesStatement.title");
        if (!Strings.isNullOrEmpty(subSeriesStatement)) {
            sb.append(" ; ").append(subSeriesStatement);
            titleComponents.addAll(split(subSeriesStatement));
        }
        this.volumeDesignation = getString("SortableVolumeDesignation.volumeDesignation");
        if (!Strings.isNullOrEmpty(volumeDesignation)) {
            sb.append(" ; ").append(volumeDesignation);
            titleComponents.addAll(split(volumeDesignation));
        }
        // add part name / part number
        String partName = clean(getString("SeriesAddedEntryUniformTitle.title"));
        if (!Strings.isNullOrEmpty(partName)) {
            sb.append(" ; ").append(partName);
            titleComponents.addAll(split(partName));
        }
        // numbering is already in partName
        this.numbering = getString("SeriesAddedEntryUniformTitle.number");
        setTitle(sb.toString());
        // extended title
        // proper title
        String titleProper = clean(getString("TitleProper.title"));
        if (!Strings.isNullOrEmpty(titleProper)) {
            sb.append(" ; ").append(titleProper);
            titleComponents.addAll(split(titleProper));
        }
        if (!Strings.isNullOrEmpty(corporate)) {
            sb.append(" / ").append(corporate);
            titleComponents.add(corporate);
        }
        if (!Strings.isNullOrEmpty(meeting)) {
            sb.append(" / ").append(meeting);
            titleComponents.add(meeting);
        }
    }

    @SuppressWarnings("unchecked")
    protected void makePublisher() {
        this.publisherName = getString("PublisherName.publisherName");
        if (this.publisherName == null) {
            this.publisherName = getString("PublisherName.printerName");
        }
        if (this.publisherName == null) {
            this.publisherName = getString("PublisherName.name");
        }
        this.publisherPlace = getString("PublicationPlace.publisherPlace");
        if (this.publisherPlace == null) {
            this.publisherPlace = getString("PublicationPlace.printingPlace");
        }
        if (this.publisherPlace == null) {
            this.publisherPlace = getString("PublicationPlace.place");
        }
        this.language = getString("Language.languageSource", null);
        Object o = getAnyObject("Country.countryISO");
        if (o instanceof List) {
            this.country = (List<String>) o;
        } else if (o instanceof String) {
            List<String> l = new LinkedList<>();
            l.add((String) o);
            this.country = l;
        } else {
            this.country =  new LinkedList();
        }
    }

    @SuppressWarnings("unchecked")
    protected void makeGenre() {
        this.resourceType = getString("TypeMedia");
        Object o = getAnyObject("TypeMonograph");
        if (o instanceof List) {
            this.genres = (List<String>) o;
        } else if (o instanceof String) {
            List<String> l = new LinkedList();
            l.add((String) o);
            this.genres = l;
        } else {
            this.genres = new LinkedList<>();
        }
    }

    protected void makeDate() {
        Integer firstDate = getInteger("DateFirst.date");
        if (firstDate == null) {
            firstDate = getInteger("DateProper.date");
        }
        if (firstDate == null) {
            firstDate = getInteger("DateOther.date");
        }
        if (firstDate == null) {
            firstDate = getInteger("Conference.conferenceDate");
        }
        this.firstDate = firstDate == null ? null : firstDate == 9999 ? null : firstDate;
        // only single date by default
        this.lastDate = null;
    }

    @SuppressWarnings("unchecked")
    protected void makeIdentifiers() {
        Map<String, Object> m = new HashMap<>();
        // get and convert all ISSN
        Object o = map.get("IdentifierISSN");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            List<String> issns = new LinkedList<>();
            List<Map<String, Object>> l = (List<Map<String, Object>>) o;
            for (Map<String, Object> aL : l) {
                Object oo = aL.get("identifierISSN");
                if (!(oo instanceof List)) {
                    oo = Collections.singletonList(oo);
                }
                for (String s : (List<String>)oo) {
                    if (s != null) {
                        issns.add(s.replaceAll("\\-", "").toLowerCase());
                    }
                }
            }
            if (!issns.isEmpty()) {
                m.put("issn", issns);
            }
        }
        // get and convert all ISBN
        o = map.get("IdentifierISBN");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Collections.singletonList(o);
            }
            List<String> isbns = new LinkedList<>();
            List<Map<String, Object>> l = (List<Map<String, Object>>) o;
            for (Map<String, Object> aL : l) {
                Object oo = aL.get("identifierISBN");
                if (!(oo instanceof List)) {
                    oo = Collections.singletonList(oo);
                }
                for (String s : (List<String>)oo) {
                    if (s != null) {
                        isbns.add(s.replaceAll("\\-", "").toLowerCase());
                    }
                }
            }
            if (!isbns.isEmpty()) {
                m.put("isbn", isbns);
            }
        }
        this.identifiers = m;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TitleRecord && externalID.equals(((TitleRecord)other).externalID);
    }

    @Override
    public int hashCode() {
        return externalID.hashCode();
    }

    @Override
    public int compareTo(TitleRecord m) {
        return externalID.compareTo(m.getExternalID());
    }

}
