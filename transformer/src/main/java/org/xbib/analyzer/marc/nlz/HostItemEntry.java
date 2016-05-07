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
package org.xbib.analyzer.marc.nlz;

import org.xbib.etl.marc.dialects.nlz.NlzEntity;
import org.xbib.etl.marc.dialects.nlz.NlzEntityQueue;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryLiteral;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HostItemEntry extends NlzEntity {

    // 84:3 (1987:Sept.) 39
    private final static Pattern partPattern1 = Pattern.compile("^(.*?)\\:(.*?)\\s*\\((\\d{4,}).*?\\)(.*?)$");

    // 22 (1924/1925) 115
    private final static Pattern partPattern2 = Pattern.compile("^(.*?)\\s*\\((\\d{4,}).*?\\)(.*?)$");

    private final static Pattern titlePattern = Pattern.compile("^(.*?)\\.\\.\\s\\-\\s(.*?)\\s\\:\\s(.*?)$");

    public HostItemEntry(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String data(NlzEntityQueue.NlzWorker worker,
                       String resourcePredicate, Resource resource, String property, String value) throws IOException {
        Resource r = worker.getWorkerState().getResource();
        switch (property) {
            case "relatedParts": {
                String volume = null;
                String issue = null;
                String date = null;
                String page = null;
                Matcher matcher = partPattern1.matcher(value);
                if (matcher.matches()) {
                    volume = matcher.group(1);
                    issue = matcher.group(2);
                    int pos = issue.indexOf(':');
                    if (pos > 0) {
                        volume = issue.substring(0, pos);
                        issue = issue.substring(pos + 1);
                    }
                    date = matcher.group(3);
                    page = matcher.group(4);
                } else {
                    matcher = partPattern2.matcher(value);
                    if (matcher.matches()) {
                        volume = matcher.group(1);
                        date = matcher.group(2);
                        page = matcher.group(3);
                    } else {
                        logger.warn("unmatched: {}", value);
                    }
                }
                r.newResource(FRBR_EMBODIMENT)
                        .a(FABIO_PRINT_OBJECT)
                        .add(PRISM_STARTING_PAGE, page);
                r.newResource(FRBR_EMBODIMENT)
                        .a(FABIO_PERIODICAL_VOLUME)
                        .add(PRISM_VOLUME, volume);
                r.newResource(FRBR_EMBODIMENT)
                        .a(FABIO_PERIODICAL_ISSUE)
                        .add(PRISM_NUMBER, issue);
                if (date != null) {
                    r.add(DC_DATE, new MemoryLiteral(date.substring(0, 4)).type(Literal.GYEAR));
                    r.add(PRISM_PUBLICATION_DATE, date);
                    worker.getWorkerState().getWorkAuthorKey()
                            .chronology(date.substring(0, 4));
                }
                worker.getWorkerState().getWorkAuthorKey()
                        .chronology(volume)
                        .chronology(issue);
                break;
            }
            case "title": {
                Matcher matcher = titlePattern.matcher(value);
                if (matcher.matches()) {
                    String journalTitle = matcher.group(1).trim();
                    if (journals.containsKey(journalTitle)) {
                        journalTitle = journals.get(journalTitle);
                    }
                    String cleanTitle = journalTitle
                            .replaceAll("\\p{C}", "")
                            .replaceAll("\\p{Space}", "")
                            .replaceAll("\\p{Punct}", "");
                    String publishingPlace = matcher.group(2).trim();
                    String publisherName = matcher.group(3).trim();
                    if (publisherName.endsWith(".")) {
                        publisherName = publisherName.substring(0, publisherName.length()-1);
                    }
                    Resource serial = worker.getWorkerState().getSerialsMap().get(cleanTitle.toLowerCase());
                    if (serial == null && journalTitle.startsWith("The")) {
                        journalTitle = journalTitle.substring(4);
                        cleanTitle = journalTitle
                                .replaceAll("\\p{C}", "")
                                .replaceAll("\\p{Space}", "")
                                .replaceAll("\\p{Punct}", "");
                        serial = worker.getWorkerState().getSerialsMap().get(cleanTitle.toLowerCase());
                    }
                    Resource j = r.newResource(FRBR_PARTOF)
                            .a(FABIO_JOURNAL)
                            .add(PRISM_PUBLICATION_NAME, journalTitle)
                            .add(PRISM_LOCATION, publishingPlace)
                            .add(DC_PUBLISHER, publisherName);
                    if (serial != null) {
                        for (Node issn : serial.objects(PRISM_ISSN)) {
                            j.add(PRISM_ISSN, issn.toString());
                        }
                    } else {
                        worker.getWorkerState().getMissingSerialsMap().put(journalTitle, true);
                    }
                }
                break;
            }
            default:
                break;
        }
        return value;
    }

    private final static Map<String,String> journals = new HashMap<String,String>() {{
        put("Zeitschrift fur Celtische Philologie", "Zeitschrift für celtische Philologie");
        put("Zeitschrift für Celtische Philologie; The Revenue Celtique", "Zeitschrift für celtische Philologie");
        put("Zeitschrift fur Orthographie", "Zeitschrift für Orthographie");
        put("Political quarterly", "The Political Quarterly");
        put("Zeitschrift fur Offentliches Recht Reappears", "Zeitschrift für Öffentliches Recht");
        put("Zeitschrift der deutschen morgenlandischen Gesellschaft", "Zeitschrift der deutschen morgenländischen Gesellschaft");
        put("Zeitschrift für Deutscher Verein für Kunstwissenschaft", "Zeitschrift für Kunstwissenschaft");
        put("Canadian journal of economics and political science/Revue canadienne d'économique et de science politique", "Canadian journal of economics and political science");
        put("Canadian modern language review/Revue canadienne des langues vivantes", "Canadian Modern Language Review");
    }};

    private final static IRI DC_DATE = IRI.create("dc:date");

    private final static IRI DC_PUBLISHER = IRI.create("dc:publisher");

    private final static IRI FABIO_JOURNAL = IRI.create("fabio:Journal");

    private final static IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");

    private final static IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");

    private final static IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");

    private final static IRI FRBR_PARTOF = IRI.create("frbr:partOf");

    private final static IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");

    private final static IRI PRISM_PUBLICATION_DATE = IRI.create("prism:publicationDate");

    private final static IRI PRISM_PUBLICATION_NAME = IRI.create("prism:publicationName");

    private final static IRI PRISM_LOCATION = IRI.create("prism:location");

    private final static IRI PRISM_ISSN = IRI.create("prism:issn");

    private final static IRI PRISM_VOLUME = IRI.create("prism:volume");

    private final static IRI PRISM_NUMBER = IRI.create("prism:number");

    private final static IRI PRISM_STARTING_PAGE = IRI.create("prism:startingPage");

}
