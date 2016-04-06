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
package org.xbib.etl.marc.dialects.mab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.DefaultEntity;
import org.xbib.etl.faceting.Facet;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public abstract class MABEntity extends DefaultEntity {

    protected static final Logger logger = LogManager.getLogger(MABEntity.class.getName());

    public MABEntity(Map<String,Object> params) {
        super(params);
    }

    /**
     * Process mapped element with fields. Empty by default.
     *
     * @param worker the worker
     * @param fields  fields
     */
    public boolean fields(MABEntityQueue.MABWorker worker, FieldList fields) throws IOException {
        // overridden
        return false;
    }

    /**
     * Transform field data
     *
     * @param value value
     * @return transformed value
     */
    public String data(MABEntityQueue.MABWorker worker,
                       String resourcePredicate, Resource resource, String property, String value) {
        // nothing
        return value;
    }

    public MABEntity facetize(MABEntityQueue.MABWorker worker, Field field) {
        return this;
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> getCodes() {
        return (Map<String, Object>) getParams().get("codes");
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> getFacetCodes() {
        return (Map<String, Object>) getParams().get("facetcodes");
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> getRegexes() {
        return (Map<String, Object>) getParams().get("regexes");
    }

    public Resource getResource(MABEntityQueue.MABWorker worker) throws IOException {
        return worker.state().getResource();
    }

    public Facet getDefaultFacet() {
        return null;
    }
}
