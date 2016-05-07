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
import org.xbib.marc.FieldList;
import org.xbib.rdf.Literal;
import org.xbib.rdf.memory.MemoryLiteral;

import java.io.IOException;
import java.util.Map;

public class GeneralInformation extends NlzEntity {

    public GeneralInformation(Map<String,Object> params) {
        super(params);
    }

    /**
     * Example "991118d19612006xx z||p|r ||| 0||||0ger c"
     */
    @Override
    public boolean fields(NlzEntityQueue.NlzWorker worker, FieldList fields) throws IOException {
        String value = fields.getLast().data();
        if (value.length() != 40) {
            logger.warn("broken GeneralInformation field, length is not 40");
        }
        String date1 = value.length() > 11 ? value.substring(7,11) : "0000";
        Integer date = check(date1);
        worker.getWorkerState().getResource().add(DC_DATE, new MemoryLiteral(date).type(Literal.GYEAR));
        return false;
    }

    // check for valid date, else return null
    private Integer check(String date) {
        try {
            int d = Integer.parseInt(date);
            if (d == 9999) {
                return null;
            }
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    private final static IRI DC_DATE = IRI.create("dc:date");

}
