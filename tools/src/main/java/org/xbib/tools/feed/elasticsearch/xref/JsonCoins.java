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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.rdf.io.turtle.TurtleContentParams;
import org.xbib.rdf.memory.BlankMemoryResource;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.input.FileInput;
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
import org.xbib.text.InvalidCharacterException;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.CharacterEntities;
import org.xbib.util.IndexDefinition;
import org.xbib.util.URIBuilder;
import org.xbib.util.URIFormatter;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.URIWorkerRequest;
import org.xbib.util.concurrent.WorkerProvider;
import org.xbib.xml.XMLUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;
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

    private final static SerialsDB serialsdb = new SerialsDB();

    private final static Lock lock = new ReentrantLock();

    @Override
    @SuppressWarnings("unchecked")
    protected WorkerProvider<Converter> provider() {
        return p -> new JsonCoins().setPipeline(p);
    }

    @Override
    public void prepareRequests() throws IOException, InterruptedException {
        super.prepareRequests();
        Map<String,Settings> inputMap = settings.getGroups("input");
        Settings settings = inputMap.get("serials");
        Queue<URI> input = new Finder()
                .find(settings.get("path"), settings.get("name"))
                .getURIs();
        logger.info("parsing initial set of serials...");
        try {
            serialsdb.process(settings, input.poll());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("serials done, size={}", serialsdb.getMap().size());
        if (serialsdb.getMap().isEmpty()) {
            throw new IllegalArgumentException("no serials?");
        }
    }

    @Override
    public void prepareResources() throws IOException {
        super.prepareResources();
        // extra turtle output
        TurtleContentParams params = new TurtleContentParams(namespaceContext, true);
        if (fileOutput.getMap().containsKey("turtle")) {
            setRdfContentBuilder(turtleBuilder(fileOutput.getMap().get("turtle").getOut(), params));
        }
        if (fileOutput.getMap().containsKey("errors")) {
            setErrorRdfContentBuilder(turtleBuilder(fileOutput.getMap().get("errors").getOut(), params));
        }
        if (fileOutput.getMap().containsKey("noserial")) {
            setMissingRdfContentBuilder(turtleBuilder(fileOutput.getMap().get("noserial").getOut(), params));
        }
        // extra text file for missing serials
        if (fileOutput.getMap().containsKey("missingserials")) {
            setMissingSerialsWriter(new OutputStreamWriter(fileOutput.getMap().get("missingserials").getOut(), "UTF-8"));
        }
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            IndexDefinition indexDefinition = indexDefinitionMap.get("bib");
            RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext);
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            JsonParser parser = jsonFactory.createParser(reader);
            JsonToken token = parser.nextToken();
            Resource resource = null;
            String key = null;
            String value;
            Result result = Result.OK;
            String index = indexDefinition.getConcreteIndex();
            String type = indexDefinition.getType();
            while (token != null) {
                switch (token) {
                    case START_OBJECT: {
                        resource = new BlankMemoryResource();
                        break;
                    }
                    case END_OBJECT: {
                        String indexType = type;
                        switch (result) {
                            case OK:
                                indexType = type;
                                if (rdfContentBuilder != null) {
                                    try {
                                        lock.lock();
                                        rdfContentBuilder.receive(resource);
                                    } finally {
                                        lock.unlock();
                                    }
                                }
                                break;
                            case MISSINGSERIAL:
                                indexType = "noserials";
                                if (missingRdfContentBuilder != null) {
                                    try {
                                        lock.lock();
                                        missingRdfContentBuilder.receive(resource);
                                    } finally {
                                        lock.unlock();
                                    }
                                }
                                break;
                            case ERROR:
                                indexType = "errors";
                                if (errorRdfContentBuilder != null) {
                                    try {
                                        lock.lock();
                                        errorRdfContentBuilder.receive(resource);
                                    } finally {
                                        lock.unlock();
                                    }
                                }
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
                        try {
                            value = parser.getText();
                        } catch (JsonParseException e) {
                            // Unexpected end-of-input: was expecting closing quote for a string value
                            value = null;
                            logger.warn("parse error: URI " + uri + " key " + key);
                        }
                        if (value != null && "coins".equals(key)) {
                            result = parseCoinsInto(resource, value);
                        }
                        break;
                    }
                    default:
                        throw new IOException("unknown token: " + token);
                }
                try {
                    token = parser.nextToken();
                } catch (JsonParseException e) {
                    logger.error("parse error " + e.getMessage() + ", terminating early: URI " + uri, e);
                    token = null;
                }
            }
        }
    }

    private interface URIListener extends URIBuilder.ParameterListener {

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

    private Result parseCoinsInto(Resource resource, String value) {
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
            String year = null;
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
                        String s = URIBuilder.decode(v, StandardCharsets.UTF_8).toLowerCase();
                        // remove URL/URI prefixes
                        for (Pattern pattern : doipatterns) {
                            s = pattern.matcher(s).replaceAll("");
                        }
                        try {
                            doiURI = URIFormatter.encode(s, StandardCharsets.UTF_8);
                            // encode as URI, but info URI RFC wants slash as unencoded character
                            // anyway we use xbib.info/doi/
                            doiURI = doiURI.replaceAll("%2F", "/");
                            IRI iri = IRI.builder().scheme("http")
                                    .host("xbib.info")
                                    .path("/doi/")
                                    .fragment(doiURI)
                                    .build();
                            r.setId(iri)
                                    .a(FABIO_ARTICLE)
                                    .add(PRISM_DOI, s);
                        } catch (Exception e) {
                            logger.warn("can't complete IRI from DOI " + v, e);
                        }
                        break;
                    }
                    case "rft.atitle": {
                        v = CharacterEntities.HTML40.unescape(v);
                        v = v.replaceAll("\\<[^>]*>","");
                        if (v.endsWith(".")) {
                            v = v.substring(0, v.length()-1);
                        }
                        r.add(DC_TITLE, v);
                        work = v;
                        break;
                    }
                    case "rft.jtitle": {
                        v = CharacterEntities.HTML40.unescape(v);
                        String cleanTitle = v.replaceAll("\\p{C}","")
                                .replaceAll("\\p{Space}","")
                                .replaceAll("\\p{Punct}","");
                        j = r.newResource(FRBR_PARTOF)
                                .a(FABIO_JOURNAL)
                                .add(PRISM_PUBLICATION_NAME, v);
                        Resource serial = serialsdb.getMap().get(cleanTitle);
                        if (serial != null) {
                            issns = serial.objects(PRISM_ISSN).iterator();
                            while (issns.hasNext()) {
                                Node issn  = issns.next();
                                j.add(PRISM_ISSN, issn.toString());
                            }
                            for (Node publisher : serial.objects(DC_PUBLISHER)) {
                                j.add(DC_PUBLISHER, publisher.toString());
                            }
                        } else {
                            missingserial = true;
                            if (missingSerialsWriter != null) {
                                try {
                                    lock.lock();
                                    try {
                                        missingSerialsWriter.write(v);
                                        missingSerialsWriter.write("\n");
                                    } catch (IOException e) {
                                        logger.error("can't write missing serial info", e);
                                    }
                                } finally {
                                    lock.unlock();
                                }
                            }
                        }
                        break;
                    }
                    case "rft.aulast": {
                        v = CharacterEntities.HTML40.unescape(v);
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
                                author.setLastName(v);
                            } else {
                                author.lastName = v;
                            }
                        } else {
                            author = new Author();
                            author.setLastName(v);
                        }
                        break;
                    }
                    case "rft.aufirst": {
                        v = CharacterEntities.HTML40.unescape(v);
                        if (author != null) {
                            if (author.foreName != null) {
                                authors.add(author);
                                r.newResource(DC_CREATOR)
                                        .a(FOAF_AGENT)
                                        .add(FOAF_FAMILYNAME, author.lastName)
                                        .add(FOAF_GIVENNAME, author.foreName);
                                author = new Author();
                                author.setForeName(v);
                            } else {
                                author.setForeName(v);
                            }
                        } else {
                            author = new Author();
                            author.setForeName(v);
                        }
                        break;
                    }
                    case "rft.au": {
                        // fix author strings
                        if ("&NA;".equals(v)) {
                            v = null;
                        } else {
                            v = CharacterEntities.HTML40.unescape(v);
                        }
                        // dubious author names contain question marks e.g. 10.1007/s101820500188
                        if (v != null && v.indexOf('?') >= 0) {
                            error = true;
                        }
                        author = new Author();
                        author.setLastName(v);
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
                        r.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PERIODICAL_VOLUME)
                                .add(PRISM_VOLUME, v);
                        break;
                    }
                    case "rft.issue": {
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
            URIBuilder.parseQueryString(coins.toURI(), StandardCharsets.UTF_8, listener);
        } catch (InvalidCharacterException | URISyntaxException e) {
            logger.warn("can't parse query string: " + coins, e);
        }
        listener.close();
        return listener.hasErrors() ? Result.ERROR :
                listener.missingSerial() ? Result.MISSINGSERIAL :
                        Result.OK;
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

    @Override
    @SuppressWarnings("unchecked")
    protected ForkJoinPipeline newPipeline() {
        return new BuilderPipeline();
    }

    private RdfContentBuilder rdfContentBuilder;
    private RdfContentBuilder errorRdfContentBuilder;
    private RdfContentBuilder missingRdfContentBuilder;
    private Writer missingSerialsWriter;

    private void setRdfContentBuilder(RdfContentBuilder rdfContentBuilder) {
        this.rdfContentBuilder = rdfContentBuilder;
    }

    private void setErrorRdfContentBuilder(RdfContentBuilder errorRdfContentBuilder) {
        this.errorRdfContentBuilder = errorRdfContentBuilder;
    }

    private void setMissingRdfContentBuilder(RdfContentBuilder missingRdfContentBuilder) {
        this.missingRdfContentBuilder = missingRdfContentBuilder;
    }

    private void setMissingSerialsWriter(Writer writer) {
        this.missingSerialsWriter = writer;
    }

    @Override
    public Feeder setPipeline(Pipeline<Converter,URIWorkerRequest> pipeline) {
        super.setPipeline(pipeline);
        if (pipeline instanceof BuilderPipeline) {
            BuilderPipeline builderPipeline = (BuilderPipeline) pipeline;
            setRdfContentBuilder(builderPipeline.getRdfContentBuilder());
            setErrorRdfContentBuilder(builderPipeline.getErrorRdfContentBuilder());
            setMissingRdfContentBuilder(builderPipeline.getMissingRdfContentBuilder());
            setMissingSerialsWriter(builderPipeline.getMissingSerialsWriter());
        }
        return this;
    }

    private class BuilderPipeline extends FeederPipeline {

        private RdfContentBuilder getRdfContentBuilder() {
            return rdfContentBuilder;
        }

        private RdfContentBuilder getErrorRdfContentBuilder() {
            return errorRdfContentBuilder;
        }

        private RdfContentBuilder getMissingRdfContentBuilder() {
            return missingRdfContentBuilder;
        }

        private Writer getMissingSerialsWriter() {
            return missingSerialsWriter;
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
