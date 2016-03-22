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
package org.xbib.analyzer.marc.zdb.hol;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;

import java.io.IOException;
import java.util.Map;

public class GeneralInformation extends MARCEntity {

    private final static GeneralInformation instance = new GeneralInformation();
    
    public static GeneralInformation getInstance() {
        return instance;
    }

    private Map<String,Object> codes;

    private Map<String,Object> continuingresource;

    @Override
    public MARCEntity setSettings(Map params) {
        super.setSettings(params);
        this.codes= (Map<String,Object>)params.get("codes");
        this.continuingresource= (Map<String,Object>)params.get("continuingresource");
        return this;
    }

    /**
     * Example "991118d19612006xx z||p|r ||| 0||||0ger c"
     */
    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker,
                          FieldList fields, String value) throws IOException {

        String date1 = value.length() > 11 ? value.substring(7,11) : "0000";
        worker.state().getResource().add("date1", check(date1));

        String date2 = value.length() > 15 ? value.substring(11,15) : "0000";
        worker.state().getResource().add("date2", check(date2));

        for (Field field: fields) {
            String data = field.data();
            if (data == null) {
                continue;
            }
            for (int i = 0; i < data.length(); i++) {
                String ch = data.substring(i, i+1);
                if ("|".equals(ch) || " ".equals(ch)) {
                    continue;
                }
                if (codes != null) {
                    Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                    if (q != null) {
                        String predicate = (String) q.get("_predicate");

                        String code = (String) q.get(ch);
                        if (code == null) {
                            logger.warn("unmapped code {} in field {} predicate {}", ch, field, predicate);
                        }
                        worker.state().getResource().add(predicate, code);
                    }
                }
                if (continuingresource != null) {
                    Map<String, Object> q = (Map<String, Object>) continuingresource.get(Integer.toString(i));
                    if (q != null) {
                        String predicate = (String) q.get("_predicate");
                        String code = (String) q.get(ch);
                        if (code == null) {
                            logger.warn("unmapped code {} in field {} predicate {}", ch, field, predicate);
                        }
                        worker.state().getResource().add(predicate, code);
                    }

                }
            }
        }
        return false;
    }

    // check for valid date, else return null
    private Integer check(String date) {
        try {
            int d = Integer.parseInt(date);
            if (d < 1450) {
                if (d > 0) {
                    logger.warn("very early date ignored: {}", d);
                }
                return null;
            }
            if (d == 9999) {
                return null;
            }
            return d;
        } catch (Exception e) {
            return null;
        }
    }
}
