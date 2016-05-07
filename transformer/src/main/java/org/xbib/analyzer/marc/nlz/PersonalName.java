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

import org.xbib.common.Strings;
import org.xbib.etl.marc.dialects.nlz.NlzEntity;
import org.xbib.etl.marc.dialects.nlz.NlzEntityQueue;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Map;

public class PersonalName extends NlzEntity {

    public PersonalName(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String data(NlzEntityQueue.NlzWorker worker,
                       String resourcePredicate, Resource resource, String property, String value) throws IOException {
        Resource r = worker.getWorkerState().getResource();
        if ("personalName".equals(property)) {
            String name = capitalize(value.toLowerCase().substring(0, value.length()-1), " ");
            r.newResource(DC_CREATOR)
                    .a(FOAF_AGENT)
                    .add(FOAF_NAME, name);
            worker.getWorkerState().getWorkAuthorKey().authorName(name);
        }
        return value;
    }

    private String capitalize(final String str, String delimiters) {
        final int delimLen = delimiters == null ? -1 : delimiters.length();
        if (Strings.isEmpty(str) || delimLen == 0) {
            return str;
        }
        final char[] buffer = str.toCharArray();
        boolean b = true;
        for (int i = 0; i < buffer.length; i++) {
            final char ch = buffer[i];
            if (delimiters != null && delimiters.indexOf(ch) >= 0) {
                b = true;
            } else if (b) {
                buffer[i] = Character.toTitleCase(ch);
                b = false;
            }
        }
        return new String(buffer);
    }


    private final static IRI FOAF_AGENT = IRI.create("foaf:agent");

    private final static IRI FOAF_NAME = IRI.create("foaf:name");

    private final static IRI DC_CREATOR = IRI.create("dc:creator");

}
