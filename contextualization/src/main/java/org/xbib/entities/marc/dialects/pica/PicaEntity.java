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
package org.xbib.entities.marc.dialects.pica;

import org.xbib.entities.Entity;
import org.xbib.entities.EntityBuilder;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;

import java.util.Map;

public class PicaEntity implements Entity<FieldList, String, PicaEntityBuilder> {

    protected static final Logger logger = LoggerFactory.getLogger(PicaEntity.class.getName());

    protected Map<String,Object> params;
    
    @Override
    public PicaEntity setSettings(Map params) {
        this.params = params;
        return this;
    }

    @Override
    public Map<String,Object> getSettings() {
        return params;
    }

    @Override
    public PicaEntity build(PicaEntityBuilder builder, FieldList key, String value) {
        return this;
    }

    /**
     * Process mapped element. Empty by default.
     *
     * @param worker
     * @param fields
     * @param value
     */
    public void fields(PicaEntityQueue.PicaKeyValueWorker worker,
                       FieldList fields, String value) {
        // should be overridden
    }

    /**
     * Process mapped element, with subfield mappings. Empty by default.
     *
     * @param builder
     * @param field
     * @param subfieldType
     */
    public void field(EntityBuilder<PicaEntityBuilderState, PicaEntity, FieldList, String> builder,
                      Field field, String subfieldType) {
        // should be overridden
    }

}