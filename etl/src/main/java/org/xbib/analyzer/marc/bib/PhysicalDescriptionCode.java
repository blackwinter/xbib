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
package org.xbib.analyzer.marc.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;

import java.io.IOException;
import java.util.Map;

public class PhysicalDescriptionCode extends MARCEntity {
    private final static PhysicalDescriptionCode instance = new PhysicalDescriptionCode();

    public static MARCEntity getInstance() {
        return instance;
    }
    
    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker,
                          FieldList fields, String value) throws IOException {
        Map<String,Object> codes = (Map<String,Object>)getSettings().get("codes");
        if (codes != null) {
            // position 0 is the selector
            codes = (Map<String,Object>)codes.get("0");
        }
        for (Field field: fields) {
            String data = field.data();
            if (data == null) {
                continue;
            }
            Map<String,Object> m = (Map<String,Object>)codes.get(data.substring(0,1));
            if (m == null) {
                continue;
            }
            // transform all codes except position 0
            String predicate = (String)m.get("_predicate");
            for (int i = 1; i < data.length(); i++) {
                String ch = data.substring(i,i+1);
                Map<String,Object> q = (Map<String,Object>)m.get(Integer.toString(i));
                if (q != null) {
                    String code = (String)q.get(ch);
                    worker.state().getResource().add(predicate, code);
                }
            }
        }
        return false;
    }
    
}
