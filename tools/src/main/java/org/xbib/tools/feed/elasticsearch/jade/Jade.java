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
package org.xbib.tools.feed.elasticsearch.jade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.rdf.memory.BlankMemoryResource;
import org.xbib.tools.convert.Converter;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.Literal;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RdfXContentParams;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.ArticleVocabulary;
import org.xbib.util.CharacterEntities;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;

public class Jade extends Feeder implements ArticleVocabulary {

    private final static Logger logger = LogManager.getLogger(Jade.class);

    private final static Pattern datepattern = Pattern.compile("^\\d\\d\\d\\d.*");

    private final static Pattern volissuepattern1 = Pattern.compile("^(\\d\\d\\d\\d), Vol\\.\\s*(.*?), Nr\\.\\s*(.*?), S\\.\\s*(.*)$");

    private final static Pattern volissuepattern2 = Pattern.compile("^(\\d\\d\\d\\d), Vol\\.\\s*(.*?), T\\.\\s*(.*?), S\\.\\s*(.*)$");

    private final static Pattern volissuepattern3 = Pattern.compile("^(\\d\\d\\d\\d), Vol\\.\\s*(.*?), Vol\\.\\s*(.*?), S\\.\\s*(.*)$");

    private final static Pattern volissuepattern4 = Pattern.compile("^(\\d\\d\\d\\d), Vol\\.\\s*(.*?), S\\.\\s*(.*)$");

    private final static Pattern volissuepattern5 = Pattern.compile("^(\\d\\d\\d\\d), T\\.\\s*(.*?), S\\.\\s*(.*)$");

    private final static Pattern volissuepattern6 = Pattern.compile("^(\\d\\d\\d\\d), Nr\\.\\s*(.*?), S\\.\\s*(.*)$");

    private final static Pattern volissuepattern7 = Pattern.compile("^(\\d\\d\\d\\d), \\s*(.*?), S\\.\\s*(.*)$");

    private final static Pattern partpattern = Pattern.compile(",\"Part(.*?)\"");

    private final static AtomicInteger counter = new AtomicInteger();

    private final IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    private Set<Author> authors = new LinkedHashSet<>();

    private String title;

    private String year;

    private String journal;

    private String issn;

    private String volume;

    private String issue;

    private String pagination;

    private String citation;

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new Jade().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            namespaceContext.add(new HashMap<String, String>() {{
                put(RdfConstants.NS_PREFIX, RdfConstants.NS_URI);
                put("dc", "http://purl.org/dc/elements/1.1/");
                put("dcterms", "http://purl.org/dc/terms/");
                put("foaf", "http://xmlns.com/foaf/0.1/");
                put("frbr", "http://purl.org/vocab/frbr/core#");
                put("fabio", "http://purl.org/spar/fabio/");
                put("prism", "http://prismstandard.org/namespaces/basic/3.0/");
            }});
            LinkedList<String> lines = new LinkedList<>();
            Resource resource = new BlankMemoryResource();
            Reader r = new InputStreamReader(in, StandardCharsets.ISO_8859_1);
            BufferedReader reader = new BufferedReader(r);
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("*** BRS DOCUMENT BOUNDARY ***")) {
                    complete(resource);
                    resource = new BlankMemoryResource();
                    authors.clear();
                    title = null;
                    year = null;
                    journal = null;
                    issn = null;
                    volume = null;
                    issue = null;
                    pagination = null;
                    citation = null;
                    line = reader.readLine();
                } else {
                    lines.add(line);
                    line = reader.readLine();
                    while (!isKey(line) && !isBoundary(line)) {
                        lines.add(line.trim());
                        line = reader.readLine();
                    }
                    process(uri, resource, lines);
                    lines.clear();
                }
            }
            reader.close();
            complete(resource);
        }
        logger.info("end of {}, counter = {}", uri, counter.get());
    }

    private void process(URI uri, Resource resource, LinkedList<String> lines) throws IOException {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = lines.iterator();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(" ").append(it.next());
        }
        String line = sb.toString();
        int pos = line.indexOf(":");
        String key = pos > 0 ? line.substring(0, pos) : line;
        String value = pos > 0 ? line.substring(pos+1) : line;
        process(uri, resource, key, value);
    }

    private void process(URI uri, Resource resource, String key, String value) throws IOException {
        switch (key) {
            case "..IDNR":
            case "..Document-Number": {
                // ignore
                break;
            }
            case "..AUTR": {
                value = clean(value);
                Author author = new Author(value);
                // forename?
                authors.add(author);
                break;
            }
            case "..TITL": {
                if (value.endsWith(".")) {
                    value = value.substring(0, value.length()-1);
                }
                value = clean(value);
                if (value != null && value.endsWith("(Book Review)")) {
                    value = value.substring(0, value.length()-14);
                    resource.a(FABIO_REVIEW);
                } else {
                    resource.a(FABIO_ARTICLE);
                }
                resource.add(DC_TITLE, value);
                title = value;
                break;
            }
            case "..ISSN": {
                if (!value.equals("NONE-XXXX")) {
                    issn = value;
                }
                break;
            }
            case "..AZIT": {
                citation = value;
                resource.add(DCTERMS_BIBLIOGRAPHIC_CITATION, value);
                parseCitation(value);
                if (volume != null) {
                    // there still can be clutter which troubles match key building
                    volume = volume.replaceAll("\"","").replaceAll("/","");
                    resource.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PERIODICAL_VOLUME)
                            .add(PRISM_VOLUME, volume);
                }
                if (issue != null) {
                    // fix clutter
                    Matcher matcher = partpattern.matcher(issue);
                    StringBuffer sb = new StringBuffer();
                    if (matcher.find()) {
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1)));
                        issue = sb.toString();
                    } else if (",\"Complete\"".equalsIgnoreCase(issue)) {
                        issue = "Complete";
                    } else if (",\"Supplement\"".equalsIgnoreCase(issue) || ",\"Supl.1\"".equalsIgnoreCase(issue)) {
                        issue = "Supplement";
                    } else if (",\"Index\"".equalsIgnoreCase(issue)) {
                        issue = "Index";
                    }
                    // there still can be clutter which troubles match key building
                    issue = issue.replaceAll("\"","").replaceAll("/","");
                    resource.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PERIODICAL_ISSUE)
                            .add(PRISM_NUMBER, issue);
                }
                if (pagination != null) {
                    int pos = pagination.indexOf('-');
                    if (pos > 0) {
                        String spage = pagination.substring(0, pos).trim();
                        String epage = pagination.substring(pos + 1).trim();
                        int l = spage.length() - epage.length();
                        if (l > 0) {
                            epage = spage.substring(0, l) + epage;
                        }
                        if (spage.equals(epage)) {
                            epage = null; // do not set starting page to end page
                        }
                        resource.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PRINT_OBJECT)
                                .add(PRISM_STARTING_PAGE, spage)
                                .add(PRISM_ENDING_PAGE, epage);
                    } else {
                        resource.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PRINT_OBJECT)
                                .add(PRISM_PAGERANGE, value);
                    }
                }
                break;
            }
            case "..AZST" : {
                journal = value;
                break;
            }
            case "..JAHR": {
                resource.add(PRISM_PUBLICATIONDATE, value);
                Matcher matcher = datepattern.matcher(value);
                if (matcher.matches()) {
                    try {
                        resource.add(DC_DATE, new MemoryLiteral(value.substring(0,4)).type(Literal.GYEAR));
                        year = value.substring(0,4);
                    } catch (NumberFormatException e) {
                        logger.error(e.getMessage());
                    }
                }
                break;
            }
            case "..QUEL": {
                // ignore
                break;
            }
            case "..ABST": {
                value = clean(value);
                resource.add(DCTERMS_ABSTRACT, value);
                break;
            }
            default : {
                logger.warn("{}: unknown key: {}", uri, key);
                break;
            }
        }
    }

    private void complete(Resource resource) {
        if (journal != null || issn != null) {
            resource.newResource(FRBR_PARTOF)
                    .a(FABIO_JOURNAL)
                    .add(PRISM_PUBLICATIONNAME, journal)
                    .add(PRISM_ISSN, issn);
        }
        WorkAuthor wa = new WorkAuthor()
                .workName(title)
                .authorName(authors.stream().map(Author::name).collect(Collectors.toList()))
                .chronology(year)
                .chronology(volume)
                .chronology(issue);
        for (Author author : authors) {
            resource.newResource(DC_CREATOR)
                    .a(FOAF_AGENT)
                    .add(FOAF_NAME, author.name);
        }
        String key = wa.createIdentifier();
        if (key != null) {
            resource.add(XBIB_KEY, key);
            try {
                resource.setId(IRI.create("http://xbib.info/key/" + key));
            } catch (Exception e) {
                logger.error("wrong key: '{}' citation={}", key, citation);
            }
            flush(resource);
        }
    }

    private void flush(Resource resource) {
        counter.incrementAndGet();
        try {
            RdfXContentParams params = new RdfXContentParams(namespaceContext);
            RdfContentBuilder builder = rdfXContentBuilder(params);
            builder.receive(resource);
            if (settings.getAsBoolean("mock", false)) {
                if (resource.id() != null) {
                    logger.info("{} {}", resource.id(), params.getGenerator().get());
                }
            } else if (!resource.isEmpty()) {
                if (resource.id() != null) {
                    ingest.index(
                            indexDefinitionMap.get("bib").getConcreteIndex(),
                            indexDefinitionMap.get("bib").getType(),
                            resource.id().toString(), params.getGenerator().get());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean isKey(String line) {
        if (line == null) {
            return false;
        }
        boolean b = false;
        if (line.length() > 2) {
            b = line.charAt(0) == '.' && line.charAt(1) == '.' && line.charAt(2) != '.';
        }
        return b;
    }

    private boolean isBoundary(String line) {
        if (line == null) {
            return false;
        }
        boolean b = false;
        if (line.length() > 2) {
            b = line.charAt(0) == '*' && line.charAt(1) == '*' && line.charAt(2) == '*';
        }
        return b;
    }

    private String clean(String v) {
        v = CharacterEntities.HTML40.unescape(v); // HTML entities
        v = v.replaceAll("\\<[^>]*>",""); // XML tags
        v = v.replaceAll("amp;", "").replaceAll("lt;", "").replaceAll("gt;", "");
        v = v.replaceAll("\\[non\\-\\s*Roman script word\\]", "");
        v = v.replaceAll("\\(non\\-\\s*Roman script word\\)", "");
        v = v.replaceAll("\\s{2,}", " ");
        v = v.replaceAll("Ã„", "Ä");
        v = v.replaceAll("Ã¤", "ä");
        v = v.replaceAll("Ã–", "Ö");
        v = v.replaceAll("Ã¶", "ö");
        v = v.replaceAll("Ãœ", "Ü");
        v = v.replaceAll("Ã¼", "ü");
        v = v.replaceAll("ÃŸ", "ß");
        return v;
    }

    private void parseCitation(String value) {
        Matcher matcher = volissuepattern1.matcher(value);
        if (matcher.matches()) {
            volume = matcher.group(2) != null ? removeBlanks(matcher.group(2)) : null;
            issue = matcher.group(3) != null ? removeBlanks(matcher.group(3)) : null;
            pagination = matcher.group(4) != null ? removeBlanks(matcher.group(4)) : null;
            return;
        }
        matcher = volissuepattern2.matcher(value);
        if (matcher.matches()) {
            volume = matcher.group(2) != null ? removeBlanks(matcher.group(2)) : null;
            issue = matcher.group(3) != null ? removeBlanks(matcher.group(3)) : null;
            pagination = matcher.group(4) != null ? removeBlanks(matcher.group(4)) : null;
            return;
        }
        matcher = volissuepattern3.matcher(value);
        if (matcher.matches()) {
            volume = matcher.group(2) != null ? removeBlanks(matcher.group(2)) : null;
            issue = matcher.group(3) != null ? removeBlanks(matcher.group(3)) : null;
            pagination = matcher.group(4) != null ? removeBlanks(matcher.group(4)) : null;
            return;
        }
        matcher = volissuepattern4.matcher(value);
        if (matcher.matches()) {
            volume = matcher.group(2) != null ? removeBlanks(matcher.group(2)) : null;
            pagination = matcher.group(3) != null ? removeBlanks(matcher.group(3)) : null;
            return;
        }
        matcher = volissuepattern5.matcher(value);
        if (matcher.matches()) {
            issue = matcher.group(2) != null ? removeBlanks(matcher.group(2)) : null;
            pagination = matcher.group(3) != null ? removeBlanks(matcher.group(3)) : null;
            return;
        }
        matcher = volissuepattern6.matcher(value);
        if (matcher.matches()) {
            issue = matcher.group(2) != null ? removeBlanks(matcher.group(2)) : null;
            pagination = matcher.group(3) != null ? removeBlanks(matcher.group(3)) : null;
            return;
        }
        matcher = volissuepattern7.matcher(value);
        if (matcher.matches()) {
            pagination = matcher.group(3) != null ? removeBlanks(matcher.group(3)) : null;
        }
    }

    private String removeBlanks(String s) {
        return s.replaceAll("\\s+", "");
    }

    static class Author implements Comparable<Author> {
        private String name;

        Author(String name) {
            this.name = name;
        }

        String name() {
            return name;
        }

        @Override
        public int compareTo(Author o) {
            return name().compareTo(o.name());
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Author && name.equals(((Author)o).name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
