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
package org.xbib.rdf.content;

import org.xbib.iri.namespace.IRINamespaceContext;

public class RouteRdfXContentParams extends RdfXContentParams {

    private String index;

    private String type;

    private String id;

    private String indexPredicate;

    private String typePredicate;

    private String idPredicate;

    private RouteRdfXContent.RouteHandler handler;

    public RouteRdfXContentParams() {
        super();
    }

    public RouteRdfXContentParams(String index, String type) {
        super();
        this.index = index;
        this.type = type;
    }

    public RouteRdfXContentParams(IRINamespaceContext namespaceContext) {
        super(namespaceContext);
    }

    public RouteRdfXContentParams(IRINamespaceContext namespaceContext, String index, String type) {
        super(namespaceContext);
        this.index = index;
        this.type = type;
    }

    public RouteRdfXContentParams setIndex(String index) {
        this.index = index;
        return this;
    }

    public String getIndex() {
        return index;
    }

    public RouteRdfXContentParams setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    public RouteRdfXContentParams setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public RouteRdfXContentParams setIndexPredicate(String indexPredicate) {
        this.indexPredicate = indexPredicate;
        return this;
    }

    public String getIndexPredicate() {
        return indexPredicate;
    }

    public RouteRdfXContentParams setTypePredicate(String typePredicate) {
        this.typePredicate = typePredicate;
        return this;
    }

    public String getTypePredicate() {
        return typePredicate;
    }

    public RouteRdfXContentParams setIdPredicate(String idPredicate) {
        this.idPredicate = idPredicate;
        return this;
    }

    public String getIdPredicate() {
        return idPredicate;
    }

    public RouteRdfXContentParams setHandler(RouteRdfXContent.RouteHandler handler) {
        this.handler = handler;
        return this;
    }

    public RouteRdfXContent.RouteHandler getHandler() {
        return handler;
    }
}
