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
package org.xbib.elements.marc;

import org.xbib.analyzer.dublincore.DublinCoreContext;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.util.DateUtil;

import java.util.Date;

/**
 * A MARC builder builds Elements from MARC field collections. It uses a MARC context.
 */
public class MARCContext extends DublinCoreContext {

    private String label;

    private String recordNumber;

    private String resourceType;

    @Override
    public Resource newResource() {
        return new SimpleResource();
    }

    public MARCContext setLabel(String label) {
        this.label = label;
        return this;
    }

    public MARCContext setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
        return this;
    }

    public MARCContext setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getResourceType() {
        return resourceType;
    }

    /**
     * Prepare the output of the MARC resource.
     * Add the current timestamp, the MARC record label, and the MARC record ID
     * to the resource.
     * Set the ID fragment to the record number if exists.
     * This can be used later for addressing the record to a target document ID.
     *
     * @return this context
     */
    @Override
    public ResourceContext<Resource> beforeOutput() {
        if (getResource() == null) {
            return this;
        }
        getResource().add("xbib:timestamp", DateUtil.formatDateISO(new Date()));
        getResource().add("xbib:label", label);
        if (recordNumber != null) {
            getResource().add("xbib:id", recordNumber);
            getResource().id(IRI.builder().fragment(recordNumber).build());
        }
        return this;
    }

}
