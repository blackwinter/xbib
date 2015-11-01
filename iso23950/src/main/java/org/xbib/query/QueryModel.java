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
package org.xbib.query;

/**
 * A query model is an interface for obtaining several important
 * string representations of query languages ("query forms")
 * from query parsers (such as the Contextual Query
 * Language).
 *
 * The query model interface provides methods for processing
 * the query forms in search engine middleware.
 *
 */
public interface QueryModel {

    /**
     * Set the name space context of this query model.
     * Only terms with the defined name spaces are recognized.
     * Other name space must be dropped silently.
     *
     * @param context
     */
    //void setNamespaceContext(QueryNamespaceContext context);

    /**
     * Generate a normalized ("canonical") from of the query.
     * The normalized form is useful for hashing (query IDs, session management)
     *
     * @return a normalized query string
     */
    String writeNormalizedForm();

    /**
     * By suggesting new terms instead of user-erraneous input,
     * queries should be reformulated. Instead of delegating this tedious task
     * to the user, the query generator can re-write a query, mostly be
     * replacing all occurences of an old term by the new one.
     *
     * @param oldTerm the old term in the existing query
     * @param newTerm the new term, e.g. from a spell checker
     * @return re-written query
     */
    String writeSubstitutedForm(String oldTerm, String newTerm);

    /**
     * Write a full representation of the query, including all
     * bread crumbs in the breadcrumbs.
     *
     * @return the query representation as a string
     */
    String writeWithBreadcrumbs();

    /**
     * Write the query without any breadcrumbs.
     * @return
     */
    String writeWithoutBreadcrumbs();
}
