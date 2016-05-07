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
import org.xbib.grouping.bibliographic.endeavor.WorkAuthorKey;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Title extends NlzEntity {

    public Title(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String data(NlzEntityQueue.NlzWorker worker,
                       String resourcePredicate, Resource resource, String property, String value) throws IOException {
        Resource r = worker.getWorkerState().getResource();
        IRI type = null;
        if ("titleMain".equals(property)) {
            String s = value;
            if (s.endsWith(".")) {
                s = s.substring(0, s.length()-1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length()-1);
            }
            if (s.endsWith(" / ")) {
                s = s.substring(0, s.length()-3);
            }
            if (s.endsWith(" /")) {
                s = s.substring(0, s.length()-2);
            }
            s = s.trim();
            if (s.endsWith("(Book Review)")) {
                s = s.substring(0, s.length() - 13).trim();
                type = FABIO_REVIEW;
            } else {
                if (s.startsWith("Article Review: ")) {
                    s = s.substring(16);
                } else {
                    type = FABIO_ARTICLE;
                }
            }
            String cleanTitle = value.replaceAll("\\p{C}","")
                    .replaceAll("\\p{Space}","")
                    .replaceAll("\\p{Punct}","");
            WorkAuthorKey workAuthorKey = worker.getWorkerState().getWorkAuthorKey();
            if (!workAuthorKey.isBlacklisted(value)) {
                workAuthorKey.workName(cleanTitle);
                r.a(type);
                r.add(DC_TITLE, s);
            } else {
                logger.warn("blacklisted title: {}", cleanTitle);
            }
        }
        return value;
    }

    private final static IRI DC_TITLE = IRI.create("dc:title");

    private final static IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    private final static IRI FABIO_REVIEW = IRI.create("fabio:Review");

    private final static Set<String> blacklistes = new HashSet<>(Arrays.asList(
            "",
            ""
    ));

}
