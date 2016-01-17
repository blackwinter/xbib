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
package org.xbib.tools.feed.elasticsearch.xref;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.tools.convert.Converter;
import org.xbib.util.InputService;
import org.xbib.util.Finder;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.text.InvalidCharacterException;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.convert.articles.SerialsDB;
import org.xbib.util.Entities;
import org.xbib.util.URIUtil;
import org.xbib.util.concurrent.WorkerProvider;
import org.xbib.xml.XMLUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Index article DB into Elasticsearch
 */
public class JsonCoins extends Feeder {

    private final static Logger logger = LogManager.getLogger(JsonCoins.class.getSimpleName());

    private final static IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    static {
        namespaceContext.add(new HashMap<String, String>() {{
            put(RdfConstants.NS_PREFIX, RdfConstants.NS_URI);
            put("dc", "http://purl.org/dc/elements/1.1/");
            put("dcterms", "http://purl.org/dc/terms/");
            put("foaf", "http://xmlns.com/foaf/0.1/");
            put("frbr", "http://purl.org/vocab/frbr/core#");
            put("fabio", "http://purl.org/spar/fabio/");
            put("prism", "http://prismstandard.org/namespaces/basic/3.0/");
        }});
    }

    private final static JsonFactory jsonFactory = new JsonFactory();

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final static SerialsDB serialsdb = new SerialsDB();

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new JsonCoins().setPipeline(p);
    }

    @Override
    public void prepareSource() throws IOException {
        try {
            Queue<URI> input = new Finder()
                    .find(settings.get("path"),settings.get("serials"))
                    .getURIs();
            logger.info("parsing initial set of serials...");
            try {
                serialsdb.process(settings, input.poll());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            logger.info("serials done, size={}", serialsdb.getMap().size());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        if (serialsdb.getMap().isEmpty()) {
            throw new IllegalArgumentException("no serials?");
        }
        super.prepareSource();
    }

    @Override
    public void process(URI uri) throws Exception {
        logger.info("start of processing {}", uri);
        String index = settings.get("index");
        String type = settings.get("type");
        InputStream in = InputService.getInputStream(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }
        RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext);
        params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, UTF8))) {
            JsonParser parser = jsonFactory.createParser(reader);
            JsonToken token = parser.nextToken();
            Resource resource = null;
            String key = null;
            String value;
            Result result = Result.OK;
            while (token != null) {
                switch (token) {
                    case START_OBJECT: {
                        resource = new MemoryResource().blank();
                        break;
                    }
                    case END_OBJECT: {
                        String indexType = type;
                        switch (result) {
                            case OK:
                                indexType = type;
                                break;
                            case MISSINGSERIAL:
                                indexType = "noserials";
                                break;
                            case ERROR:
                                indexType = "errors";
                                break;
                        }
                        if (resource != null) {
                            params.setIndex(index);
                            params.setType(indexType);
                            params.setId(resource.id().toString());
                            RdfContentBuilder builder = routeRdfXContentBuilder(params);
                            builder.receive(resource);
                            if (settings.getAsBoolean("mock", false)) {
                                logger.info("{}", builder.string());
                            }
                            resource = null;
                        }
                        break;
                    }
                    case START_ARRAY: {
                        break;
                    }
                    case END_ARRAY: {
                        break;
                    }
                    case FIELD_NAME: {
                        key = parser.getCurrentName();
                        break;
                    }
                    case VALUE_STRING:
                    case VALUE_NUMBER_INT:
                    case VALUE_NUMBER_FLOAT:
                    case VALUE_NULL:
                    case VALUE_TRUE:
                    case VALUE_FALSE: {
                        value = parser.getText();
                        if ("coins".equals(key)) {
                            result = parseCoinsInto(resource, value);
                        }
                        break;
                    }
                    default:
                        throw new IOException("unknown token: " + token);
                }
                token = parser.nextToken();
            }
        }
        logger.info("end of processing {}", uri);
    }

    protected interface URIListener extends URIUtil.ParameterListener {

        void close();

        boolean hasErrors();

        boolean missingSerial();
    }

    protected enum Result {
        OK, ERROR, MISSINGSERIAL
    }

    private final static Pattern[] doipatterns = new Pattern[]{
            Pattern.compile("^info:doi/"),
            Pattern.compile("^http://dx.doi.org/"),
            Pattern.compile("^http://doi.org/")
    };

    protected Result parseCoinsInto(Resource resource, String value) {
        IRI coins = IRI.builder()
                .scheme("http")
                .host("localhost")
                .query(XMLUtil.unescape(value))
                .build();
        final Resource r = resource;
        URIListener listener = new URIListener() {
            boolean error = false;
            boolean missingserial = false;
            String spage = null;
            String epage = null;
            Resource j = null;
            String title = null;
            String year = null;
            String volume = null;
            String issue = null;
            Set<Author> authors = new LinkedHashSet<>();
            Author author = null;
            String work = null;
            Iterator<Node> issns = null;
            String doiURI;

            @Override
            public void received(String k, String v) {
                if (v == null) {
                    return;
                }
                v = v.trim();
                if (v.isEmpty()) {
                    return;
                }
                if (v.indexOf('\uFFFD') >= 0) { // Unicode replacement character
                    error = true;
                }
                switch (k) {
                    case "rft_id": {
                        // lowercase important, DOI is case-insensitive
                        String s = URIUtil.decode(v, UTF8).toLowerCase();
                        // remove URL/URI prefixes
                        for (Pattern pattern : doipatterns) {
                            s = pattern.matcher(s).replaceAll("");
                        }
                        try {
                            doiURI = URIUtil.encode(s, UTF8);
                            // encode as URI, but info URI RFC wants slash as unencoded character
                            // anyway we use xbib.info/doi/
                            doiURI = doiURI.replaceAll("%2F", "/");
                            IRI iri = IRI.builder().scheme("http")
                                    .host("xbib.info")
                                    .path("/doi/")
                                    .fragment(doiURI)
                                    .build();
                            r.id(iri)
                                    .a(FABIO_ARTICLE)
                                    .add(PRISM_DOI, s);
                        } catch (Exception e) {
                            logger.warn("can't complete IRI from DOI " + v, e);
                        }
                        break;
                    }
                    case "rft.atitle": {
                        v = Entities.HTML40.unescape(v);
                        v = v.replaceAll("\\<[^>]*>","");
                        if (v.endsWith(".")) {
                            v = v.substring(0, v.length()-1);
                        }
                        r.add(DC_TITLE, v);
                        work = v;
                        break;
                    }
                    case "rft.jtitle": {
                        v = Entities.HTML40.unescape(v);
                        title = v;
                        String cleanTitle = v.replaceAll("\\p{C}","")
                                .replaceAll("\\p{Space}","")
                                .replaceAll("\\p{Punct}","");
                        j = r.newResource(FRBR_PARTOF)
                                .a(FABIO_JOURNAL)
                                .add(PRISM_PUBLICATION_NAME, v);
                        Resource serial = serialsdb.getMap().get(cleanTitle);
                        if (serial != null) {
                            issns = serial.objects(PRISM_ISSN);
                            while (issns.hasNext()) {
                                Node issn  = issns.next();
                                j.add(PRISM_ISSN, issn.toString());
                            }
                            Iterator<Node> publishers = serial.objects(DC_PUBLISHER);
                            while (publishers.hasNext()) {
                                Node publisher = publishers.next();
                                j.add(DC_PUBLISHER, publisher.toString());
                            }
                        } else {
                            missingserial = true;
                        }
                        break;
                    }
                    case "rft.aulast": {
                        v = Entities.HTML40.unescape(v);
                        if (author != null) {
                            if (author.lastName != null) {
                                authors.add(author);
                                if (author.foreName != null) {
                                    r.newResource(DC_CREATOR)
                                            .a(FOAF_AGENT)
                                            .add(FOAF_FAMILYNAME, author.lastName)
                                            .add(FOAF_GIVENNAME, author.foreName);
                                } else {
                                    r.newResource(DC_CREATOR)
                                            .a(FOAF_AGENT)
                                            .add(FOAF_NAME, author.lastName);
                                }
                                author = new Author();
                                author.lastName = v;
                            } else {
                                author.lastName = v;
                            }
                        } else {
                            author = new Author();
                            author.lastName = v;
                        }
                        break;
                    }
                    case "rft.aufirst": {
                        v = Entities.HTML40.unescape(v);
                        if (author != null) {
                            if (author.foreName != null) {
                                authors.add(author);
                                r.newResource(DC_CREATOR)
                                        .a(FOAF_AGENT)
                                        .add(FOAF_FAMILYNAME, author.lastName)
                                        .add(FOAF_GIVENNAME, author.foreName);
                                author = new Author();
                                author.foreName = v;
                            } else {
                                author.foreName = v;
                            }
                        } else {
                            author = new Author();
                            author.foreName = v;
                        }
                        break;
                    }
                    case "rft.au": {
                        // fix author strings
                        if ("&NA;".equals(v)) {
                            v = null;
                        } else {
                            v = Entities.HTML40.unescape(v);
                        }
                        // dubious author names contain question marks e.g. 10.1007/s101820500188
                        if (v != null && v.indexOf('?') >= 0) {
                            error = true;
                        }
                        author = new Author();
                        author.lastName = v;
                        r.newResource(DC_CREATOR)
                                .a(FOAF_AGENT)
                                .add(FOAF_NAME, v);
                        authors.add(author);
                        break;
                    }
                    case "rft.date": {
                        year = v;
                        r.add(DC_DATE, new MemoryLiteral(v).type(Literal.GYEAR));
                        r.add(PRISM_PUBLICATION_DATE, v);
                        break;
                    }
                    case "rft.volume": {
                        volume = v;
                        r.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PERIODICAL_VOLUME)
                                .add(PRISM_VOLUME, v);
                        break;
                    }
                    case "rft.issue": {
                        issue = v;
                        r.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PERIODICAL_ISSUE)
                                .add(PRISM_NUMBER, v);
                        break;
                    }
                    case "rft.spage": {
                        if (spage != null) {
                            r.newResource(FRBR_EMBODIMENT)
                                    .a(FABIO_PRINT_OBJECT)
                                    .add(PRISM_STARTING_PAGE, spage)
                                    .add(PRISM_ENDING_PAGE, epage);
                            spage = null;
                            epage = null;
                        } else {
                            spage = v;
                        }
                        break;
                    }
                    case "rft.epage": {
                        if (epage != null) {
                            r.newResource(FRBR_EMBODIMENT)
                                    .a(FABIO_PRINT_OBJECT)
                                    .add(PRISM_STARTING_PAGE, spage)
                                    .add(PRISM_ENDING_PAGE, epage);
                            spage = null;
                            epage = null;
                        } else {
                            epage = v;
                        }
                        break;
                    }
                    case "rft_val_fmt":
                    case "rft.genre":
                    case "ctx_ver":
                    case "rfr_id":
                        // skip
                        break;
                    default: {
                        logger.info("unknown element: {}", k);
                        break;
                    }
                }
            }

            public void close() {
                // pending fields...
                if (spage != null || epage != null) {
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PRINT_OBJECT)
                            .add(PRISM_STARTING_PAGE, spage)
                            .add(PRISM_ENDING_PAGE, epage);
                }
                if (author != null) {
                    authors.add(author);
                    if (author.foreName != null) {
                        r.newResource(DC_CREATOR)
                                .a(FOAF_AGENT)
                                .add(FOAF_FAMILYNAME, author.lastName)
                                .add(FOAF_GIVENNAME, author.foreName);
                    } else {
                        r.newResource(DC_CREATOR)
                                .a(FOAF_AGENT)
                                .add(FOAF_NAME, author.lastName);
                    }
                }
                if (work != null) {
                    // create bibliographic key
                    WorkAuthor wa = new WorkAuthor();
                    if (wa.isBlacklisted(work)) {
                        logger.warn("blacklisted: {} title={}", doiURI, work);
                    }
                    wa.workName(work)
                            .chronology(year);
                    for (Author author : authors) {
                        wa.authorNameWithForeNames(author.lastName, author.foreName);
                    }
                    String key = wa.createIdentifier();
                    r.add(XBIB_KEY, key);
                    authors.clear();
                }
            }

            public boolean hasErrors() {
                return error;
            }

            public boolean missingSerial() {
                return missingserial;
            }

        };
        try {
            URIUtil.parseQueryString(coins.toURI(), UTF8, listener);
        } catch (InvalidCharacterException | URISyntaxException e) {
            logger.warn("can't parse query string: " + coins, e);
        }
        listener.close();
        return listener.hasErrors() ? Result.ERROR :
                listener.missingSerial() ? Result.MISSINGSERIAL :
                        Result.OK;
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

    private final static IRI DC_TITLE = IRI.create("dc:title");

    private final static IRI DC_CREATOR = IRI.create("dc:creator");

    private final static IRI DC_DATE = IRI.create("dc:date");

    private final static IRI DC_PUBLISHER = IRI.create("dc:publisher");

    private final static IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    private final static IRI FABIO_JOURNAL = IRI.create("fabio:Journal");

    private final static IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");

    private final static IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");

    private final static IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");

    private final static IRI FOAF_AGENT = IRI.create("foaf:agent");

    private final static IRI FOAF_FAMILYNAME = IRI.create("foaf:familyName");

    private final static IRI FOAF_GIVENNAME = IRI.create("foaf:givenName");

    private final static IRI FOAF_NAME = IRI.create("foaf:name");

    private final static IRI FRBR_PARTOF = IRI.create("frbr:partOf");

    private final static IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");

    private final static IRI PRISM_DOI = IRI.create("prism:doi");

    private final static IRI PRISM_PUBLICATION_DATE = IRI.create("prism:publicationDate");

    private final static IRI PRISM_PUBLICATION_NAME = IRI.create("prism:publicationName");

    private final static IRI PRISM_ISSN = IRI.create("prism:issn");

    private final static IRI PRISM_VOLUME = IRI.create("prism:volume");

    private final static IRI PRISM_NUMBER = IRI.create("prism:number");

    private final static IRI PRISM_STARTING_PAGE = IRI.create("prism:startingPage");

    private final static IRI PRISM_ENDING_PAGE = IRI.create("prism:endingPage");

    private final static IRI XBIB_KEY = IRI.create("xbib:key");

}
