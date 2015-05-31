package org.xbib.tools.merge.zdb.entities;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.util.Strings;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

public class Volume extends Manifestation {

    protected final Manifestation manifestation;

    protected List<String> parents = newLinkedList();

    protected Object conference;

    protected String volumeDesignation;

    protected String numbering;

    protected List<String> genres;

    protected final List<VolumeHolding> holdings;

    public Volume(Map<String, Object> map, Manifestation manifestation) {
        super(map);
        this.manifestation = manifestation;
        this.holdings = newArrayList();
    }

    public Manifestation manifestation() {
        return manifestation;
    }

    public String getVolumeDesignation() {
        return volumeDesignation;
    }

    public String getNumbering() {
        return numbering;
    }

    @Override
    protected void build() {
        makeIdentity();
        makeCorporate();
        makeConference();
        makeFullTitle();
        makePublisher();
        makeDate();
        makeGenre();
        makeIdentifiers();
    }

    public void addParent(String parent) {
        this.parents.add(parent);
    }

    public void addHolding(VolumeHolding holding) {
        this.holdings.add(holding);
    }

    public List<VolumeHolding> getHoldings() {
        return holdings;
    }

    protected void makeIdentity() {
        String s = getString("RecordIdentifier.identifierForTheRecord");
        this.id = s != null ? s : "undefined";
        s = getString("IdentifierZDB.identifierZDB");
        this.externalID = s != null ? s : id;
    }

    @Override
    protected void makeCorporate() {
        this.corporate = getString("CorporateName.corporateName");
    }

    protected void makeConference() {
        this.conference = getAnyObject("Conference");
    }

    @Override
    protected void makeFullTitle() {
        // shorten title (series statement after '/' or ':')
        // but combine with corporate name, meeting name, and part specification
        StringBuilder sb = new StringBuilder();
        sb.append(clean(getString("TitleStatement.titleMain")));
        String titleRemainder = getString("TitleStatement.titleRemainder");
        if (titleRemainder != null) {
            sb.append(" ; ").append(titleRemainder);
        }
        String titleAddendum = getString("TitleAddendum.title");
        if (titleAddendum != null) {
            sb.append(" ; ").append(titleAddendum);
        }
        this.volumeDesignation = getString("SortableVolumeDesignation.volumeDesignation");
        if (volumeDesignation != null) {
            sb.append(" ; ").append(volumeDesignation);
        }
        // add part name / part number
        String partName = clean(getString("SeriesAddedEntryUniformTitle.title"));
        if (!Strings.isNullOrEmpty(partName)) {
            sb.append(" ; ").append(partName);
        }
        // numbering is already in partName
        this.numbering = getString("SeriesAddedEntryUniformTitle.number");
        setTitle(sb.toString());
    }

    protected void makePublisher() {
        this.publisherName = getString("PublisherName.publisherName");
        if (this.publisherName == null) {
            this.publisherName = getString("PublisherName.printerName");
        }
        this.publisherPlace = getString("PublicationPlace.publisherPlace");
        if (this.publisherPlace == null) {
            this.publisherPlace = getString("PublicationPlace.printingPlace");
        }
        this.language = getString("Language.language", null);
        Object o = getAnyObject("Country.countryISO");
        if (o instanceof List) {
            this.country = (List<String>) o;
        } else if (o instanceof String) {
            List<String> l = newLinkedList();
            l.add((String) o);
            this.country = l;
        } else {
            this.country =  newLinkedList();
        }
    }

    protected void makeGenre() {
        Object o = getAnyObject("TypeMonograph");
        if (o instanceof List) {
            this.genres = (List<String>) o;
        } else if (o instanceof String) {
            List<String> l = newLinkedList();
            l.add((String) o);
            this.genres = l;
        } else {
            List<String> l = newLinkedList();
            this.genres = l;
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
        Integer lastDate = getInteger("DateLast.date");
        if (lastDate == null) {
            lastDate = getInteger("DateProper.date");
        }
        // only single date by default
        this.lastDate = null;
    }

    protected void makeIdentifiers() {
        Map<String, Object> m = newHashMap();
        // get and convert all ISSN
        Object o = map.get("IdentifierISSN");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            List<String> issns = newLinkedList();
            List<Map<String, Object>> l = (List<Map<String, Object>>) o;
            for (Map<String, Object> aL : l) {
                Object oo = aL.get("identifierISSN");
                if (!(oo instanceof List)) {
                    oo = Arrays.asList(oo);
                }
                for (String s : (List<String>)oo) {
                    if (s != null) {
                        issns.add(s.replaceAll("\\-", "").toLowerCase());
                    }
                }
            }
            m.put("issn", issns);
        }
        // get and convert all ISBN
        o = map.get("IdentifierISBN");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            List<String> issns = newLinkedList();
            List<Map<String, Object>> l = (List<Map<String, Object>>) o;
            for (Map<String, Object> aL : l) {
                Object oo = aL.get("identifierISBN");
                if (!(oo instanceof List)) {
                    oo = Arrays.asList(oo);
                }
                for (String s : (List<String>)oo) {
                    if (s != null) {
                        issns.add(s.replaceAll("\\-", "").toLowerCase());
                    }
                }
            }
            m.put("isbn", issns);
        }
        this.identifiers = m;
    }

    public String build(XContentBuilder builder, String tag, Set<String> visited) throws IOException {
        if (visited != null) {
            if (visited.contains(id)) {
                return null;
            }
            visited.add(id);
        }
        String taggedId = tag != null ? tag + "." + id : id;
        builder.startObject();
        builder.field("@id", taggedId)
                .field("@type", "Volume")
                .array("@parent", parents)
                .fieldIfNotNull("@tag", tag)
                .field("title", getTitle())
                .field("date", firstDate);
        String s = corporateName();
        if (s != null) {
            builder.field("corporateName", s);
        }
        s = meetingName();
        if (s != null) {
            builder.field("meetingName", s);
        }
        if (conference != null) {
            builder.field("conference");
            builder.map((Map<String, Object>) conference);
        }
        builder.field("volume", volumeDesignation)
                .field("number", numbering)
                .field("genre", genres)
                .field("country", country())
                .field("language", language())
                .field("publishedat", getPublisherPlace())
                .field("publishedby", getPublisher());
        if (hasIdentifiers()) {
            builder.field("identifiers", getIdentifiers());
        }
        builder.endObject();
        return taggedId;
    }

}
