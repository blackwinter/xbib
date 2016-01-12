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
package org.xbib.tools.convert.xref;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.tools.convert.articles.SerialsDB;
import org.xbib.util.InputService;
import org.xbib.io.archive.file.Finder;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.io.turtle.TurtleContentParams;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.text.InvalidCharacterException;
import org.xbib.tools.convert.Converter;
import org.xbib.util.Entities;
import org.xbib.util.URIUtil;
import org.xbib.util.concurrent.WorkerProvider;
import org.xbib.xml.XMLUtil;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

/**
 * Convert article DB
 */
public class JsonCoins extends Converter {

    private final static Logger logger = LogManager.getLogger(JsonCoins.class.getSimpleName());

    private final static Charset UTF8 = Charset.forName("UTF-8");

    protected final static JsonFactory jsonFactory = new JsonFactory();

    private static RdfContentBuilder articleBuilder;

    private static RdfContentBuilder errorArticleBuilder;

    private static RdfContentBuilder missingArticleBuilder;

    private static GZIPOutputStream gzout;

    private static GZIPOutputStream errorgzout;

    private static GZIPOutputStream noserialgzout;

    private static FileWriter missingserials;

    private final static SerialsDB serialsdb = new SerialsDB();

    private final static IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    static {
        namespaceContext.add(new HashMap<String, String>() {{
            put(RdfConstants.NS_PREFIX, RdfConstants.NS_URI);
            put("dc", "http://purl.org/dc/elements/1.1/");
            put("dcterms", "http://purl.org/dc/terms/");
            put("foaf", "http://xmlns.com/foaf/0.1/");
            put("frbr", "http://purl.org/vocab/frbr/core#");
            put("fabio", "http://purl.org/spar/fabio/");
            put("prism", "http://prismstandard.org/namespaces/basic/2.1/");
        }});
    }

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new JsonCoins().setPipeline(p);
    }

    @Override
    public void prepareSource() throws IOException {
        logger.info("parsing initial set of serials...");

        try {
            Queue<URI> input = new Finder(settings.get("serials"))
                    .find(settings.get("path"))
                    .getURIs();
            serialsdb.process(settings, input.poll());
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("serials done, getSize = {}", serialsdb.getMap().size());

        if (serialsdb.getMap().isEmpty()) {
            throw new IllegalArgumentException("serials are empty?");
        }
        // now parse our files
        super.prepareSource();
    }

    @Override
    public void prepareSink() throws IOException {
        String outputFilename = settings.get("output");
        FileOutputStream fout = new FileOutputStream(outputFilename + ".ttl.gz");
        gzout = new GZIPOutputStream(fout) {
            {
                def.setLevel(Deflater.BEST_COMPRESSION);
            }
        };
        TurtleContentParams params = new TurtleContentParams(namespaceContext, true);
        articleBuilder = turtleBuilder(gzout, params);

        FileOutputStream errorfout = new FileOutputStream(outputFilename + "-errors.ttl.gz");
        errorgzout = new GZIPOutputStream(errorfout) {
            {
                def.setLevel(Deflater.BEST_COMPRESSION);
            }
        };
        errorArticleBuilder = turtleBuilder(errorgzout, params);

        FileOutputStream noserialfout = new FileOutputStream(outputFilename + "-without-serial.ttl.gz");
        noserialgzout = new GZIPOutputStream(noserialfout) {
            {
                def.setLevel(Deflater.BEST_COMPRESSION);
            }
        };
        missingArticleBuilder = turtleBuilder(noserialgzout, params);

        // extra text file for missing serials
        missingserials = new FileWriter("missingserials.txt");
    }

    @Override
    public JsonCoins cleanup() {
        try {
            gzout.close();
            noserialgzout.close();
            errorgzout.close();
        } catch (Exception e) {
            // skip
        }
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            JsonParser parser = jsonFactory.createParser(reader);
            JsonToken token = parser.nextToken();
            Resource resource = null;
            String key = null;
            String value;
            Result result = Result.OK;
            while (token != null) {
                switch (token) {
                    case START_OBJECT: {
                        resource = new MemoryResource();
                        break;
                    }
                    case END_OBJECT: {
                        switch (result) {
                            case OK:
                                synchronized (articleBuilder) {
                                    articleBuilder.receive(resource);
                                }
                                break;
                            case MISSINGSERIAL:
                                synchronized (missingArticleBuilder) {
                                    missingArticleBuilder.receive(resource);
                                }
                                break;
                            case ERROR:
                                synchronized (errorArticleBuilder) {
                                    errorArticleBuilder.receive(resource);
                                }
                                break;

                        }
                        resource = null;
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
    }

    protected interface URIListener extends URIUtil.ParameterListener {
        void close();

        boolean hasErrors();

        boolean missingSerial();
    }

    private final IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    private final IRI FABIO_JOURNAL = IRI.create("fabio:Journal");

    private final IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");

    private final IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");

    private final IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");

    private final IRI FOAF_MAKER = IRI.create("foaf:maker");

    private final IRI FRBR_PARTOF = IRI.create("frbr:partOf");

    private final IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");

    protected enum Result {
        OK, ERROR, MISSINGSERIAL
    }

    private final static Pattern[] patterns = new Pattern[]{
            Pattern.compile("^info:doi/"),
            Pattern.compile("^http://dx.doi.org/"),
            Pattern.compile("^http://doi.org/")
    };

    protected Result parseCoinsInto(Resource resource, final String value) {
        final IRI coins = IRI.builder()
                .scheme("http")
                .host("localhost")
                .query(XMLUtil.unescape(value)).build();
        final Resource r = resource;
        URIListener listener = new URIListener() {
            boolean error = false;
            boolean missingserial = false;

            String aufirst = null;
            String aulast = null;

            String spage = null;
            String epage = null;

            String author = null;
            String work = null;
            Iterator<Node> issns = null;

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
                        for (Pattern pattern : patterns) {
                            s = pattern.matcher(s).replaceAll("");
                        }
                        try {
                            String doiURI = URIUtil.encode(s, UTF8);
                            // encode as URI, but "info URI" RFC wants slash as unencoded character
                            // anyway we use xbib.info/doi/
                            doiURI = doiURI.replaceAll("%2F", "/");
                            IRI iri = IRI.builder()
                                    .scheme("http")
                                    .host("xbib.info")
                                    .path("/doi/")
                                    .fragment(doiURI)
                                    .build();
                            r.id(iri)
                                    .a(FABIO_ARTICLE)
                                    .add("prism:doi", s);
                        } catch (Exception e) {
                            logger.warn("can't complete IRI from DOI " + v, e);
                        }
                        break;
                    }
                    case "rft.atitle": {
                        v = Entities.HTML40.unescape(v);
                        r.add("dcterms:title", v);
                        work = v;
                        break;
                    }
                    case "rft.jtitle": {
                        v = Entities.HTML40.unescape(v);
                        String cleanTitle = v.replaceAll("\\p{C}","")
                                .replaceAll("\\p{Space}","")
                                .replaceAll("\\p{Punct}","");
                        Resource j = r.newResource(FRBR_PARTOF)
                                .a(FABIO_JOURNAL)
                                .add("prism:publicationName", v);
                        if (serialsdb.getMap().containsKey(cleanTitle)) {
                            Resource serial = serialsdb.getMap().get(cleanTitle);
                            issns = serial.objects("prism:issn");
                            while (issns.hasNext()) {
                                Node issn  = issns.next();
                                j.add("prism:issn", issn.toString());
                            }
                            Iterator<Node> publishers = serial.objects("dc:publisher");
                            while (publishers.hasNext()) {
                                Node publisher = publishers.next();
                                j.add("dc:publisher", publisher.toString());
                            }
                        } else {
                            missingserial = true;
                            synchronized (missingserials) {
                                try {
                                    missingserials.write(v);
                                    missingserials.write("\n");
                                } catch (IOException e) {
                                    logger.error("can't write missing serial info", e);
                                }
                            }
                        }
                        break;
                    }
                    case "rft.aulast": {
                        v = Entities.HTML40.unescape(v);
                        if (aulast != null) {
                            r.newResource(FOAF_MAKER)
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst);
                            aulast = null;
                            aufirst = null;
                        } else {
                            aulast = v;
                        }
                        break;
                    }
                    case "rft.aufirst": {
                        v = Entities.HTML40.unescape(v);
                        if (aufirst != null) {
                            r.newResource(FOAF_MAKER)
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst);
                            aulast = null;
                            aufirst = null;
                        } else {
                            aufirst = v;
                        }
                        break;
                    }
                    case "rft.au": {
                        // fix author
                        if ("&NA;".equals(v)) {
                            v = null;
                        } else {
                            v = Entities.HTML40.unescape(v);
                        }
                        r.add("dc:creator", v);
                        if (author == null) {
                            author = v;
                        }
                        break;
                    }
                    case "rft.date": {
                        Literal l = new MemoryLiteral(v).type(Literal.GYEAR);
                        r.add("prism:publicationDate", l);
                        break;
                    }
                    case "rft.volume": {
                        r.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PERIODICAL_VOLUME)
                                .add("prism:volume", v);
                        break;
                    }
                    case "rft.issue": {
                        r.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PERIODICAL_ISSUE)
                                .add("prism:number", v);
                        break;
                    }
                    case "rft.spage": {
                        if (spage != null) {
                            r.newResource(FRBR_EMBODIMENT)
                                    .a(FABIO_PRINT_OBJECT)
                                    .add("prism:startingPage", spage)
                                    .add("prism:endingPage", epage);
                            spage = null;
                            epage = null;
                        } else {
                            // pagination: skip leading "p" or "P"
                            if (v.startsWith("p") || v.startsWith("P")) {
                                v = v.substring(1);
                            }
                            spage = v;
                        }
                        break;
                    }
                    case "rft.epage": {
                        if (epage != null) {
                            r.newResource(FRBR_EMBODIMENT)
                                    .a(FABIO_PRINT_OBJECT)
                                    .add("prism:startingPage", spage)
                                    .add("prism:endingPage", epage);
                            spage = null;
                            epage = null;
                        } else {
                            // pagination: skip leading "p" or "P"
                            if (v.startsWith("p") || v.startsWith("P")) {
                                v = v.substring(1);
                            }
                            epage = v;
                        }
                        break;
                    }
                    case "rft_val_fmt":
                    case "rft.genre":
                    case "ctx_ver":
                    case "rfr_id":
                        break;
                    default: {
                        logger.info("unknown element: {}", k);
                        break;
                    }
                }
            }

            public void close() {
                // pending fields...
                if (aufirst != null || aulast != null) {
                    r.newResource(FOAF_MAKER)
                            .add("foaf:familyName", aulast)
                            .add("foaf:givenName", aufirst);
                }
                if (spage != null || epage != null) {
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PRINT_OBJECT)
                            .add("prism:startingPage", spage)
                            .add("prism:endingPage", epage);
                }
                // create bibliographic key
                String key = new WorkAuthor()
                        .authorName(author)
                        .workName(work)
                        .createIdentifier();
                // move aulast to author if no author
                if (author == null && aulast != null) {
                    author = aulast;
                }
                if (author != null && work != null && key != null) {
                    r.add("xbib:key", key);
                    /*if (articles.containsKey(key)) {
                        dupCounter.incrementAndGet();
                    } else {
                        //logger.info("new articles key {}", key);
                        articles.put(key, coins);
                        counter.incrementAndGet();
                    }*/
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

}
