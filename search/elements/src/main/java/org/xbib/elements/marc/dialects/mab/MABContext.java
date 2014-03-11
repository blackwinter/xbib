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
package org.xbib.elements.marc.dialects.mab;

import org.xbib.analyzer.dublincore.DublinCoreProperties;
import org.xbib.elements.items.LiaContext;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.rdf.xcontent.DefaultContentBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;


public class MABContext extends LiaContext implements DublinCoreProperties {

    private final ContentBuilder contentBuilder = new DefaultContentBuilder();

    private String identifier;

    private String format;

    private String label;

    @Override
    public Resource newResource() {
        return new SimpleResource();
    }

    public ResourceContext<Resource> identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public ResourceContext<Resource> format(String format) {
        this.format = format;
        return this;
    }

    public ResourceContext<Resource> label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public ResourceContext<Resource> prepareForOutput() {
        if (resource() == null) {
            return this;
        }
        IRI id = IRI.builder().fragment(identifier).build();
        resource().id(id);

        resource().add("xbib:format", format);
        resource().add("xbib:label", label);

        contentBuilder.timestamp(new Date());
        contentBuilder.message(id.toString());
        contentBuilder.source(this.getClass().getName());
        try {
            contentBuilder.sourceHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            contentBuilder.sourceHost("unknown");
        }

        return this;
    }

    @Override
    public ContentBuilder contentBuilder() {
        return contentBuilder;
    }
}
