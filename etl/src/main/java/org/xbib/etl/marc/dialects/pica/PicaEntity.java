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
package org.xbib.etl.marc.dialects.pica;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.Entity;
import org.xbib.marc.FieldList;

import java.io.IOException;
import java.util.Map;

public class PicaEntity implements Entity {

    protected static final Logger logger = LogManager.getLogger(PicaEntity.class.getName());

    private  Map<String,Object> params;
    
    public PicaEntity setSettings(Map<String,Object> params) {
        this.params = params;
        return this;
    }

    public Map<String,Object> getSettings() {
        return params;
    }

    /**
     * Process mapped element. Empty by default.
     *
     * @param worker the worker
     * @param fields the fields
     * @param value the value
     */
    public void fields(PicaEntityQueue.PicaKeyValueWorker worker,
                       FieldList fields, String value) throws IOException {
        // should be overridden
    }

}
