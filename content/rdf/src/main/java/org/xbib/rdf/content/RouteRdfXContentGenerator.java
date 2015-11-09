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

import org.xbib.iri.IRI;
import org.xbib.rdf.Node;

import java.io.IOException;
import java.io.OutputStream;

public class RouteRdfXContentGenerator<R extends RouteRdfXContentParams> extends RdfXContentGenerator<R> {

    private boolean flushed;

    RouteRdfXContentGenerator(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    public RdfXContentGenerator startStream() {
        super.startStream();
        flushed = false;
        return this;
    }

    @Override
    public RdfXContentGenerator receive(IRI identifier) throws IOException {
        super.receive(identifier);
        flushed = false;
        return this;
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        if (flushed) {
            return;
        }
        flushed = true;
        RouteRdfXContent.RouteHandler handler = getParams().getHandler();
        if (handler != null) {
            String s = getParams().getGenerator().get();
            if (s != null && !s.isEmpty()) {
                if (resource.id() != null) {
                    getParams().setId(resource.id().toString());
                }
                handler.complete(s, getParams());
            }
        }
    }

    @Override
    public void filter(IRI predicate, Node object) {
        String indexPredicate = getParams().getIndexPredicate();
        if (indexPredicate != null && indexPredicate.equals(predicate.toString())) {
            getParams().setIndex(object.toString());
        }
        String typePredicate = getParams().getIdPredicate();
        if (typePredicate != null && typePredicate.equals(predicate.toString())) {
            getParams().setType(object.toString());
        }
        String idPredicate = getParams().getIdPredicate();
        if (idPredicate != null && idPredicate.equals(predicate.toString())) {
            getParams().setId(object.toString());
        }
    }

}
