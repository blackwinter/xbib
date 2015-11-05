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
package org.xbib.io.iso23950.client;

import org.xbib.io.iso23950.ZSession;
import org.xbib.io.iso23950.searchretrieve.CQLSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.PQFSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Default Z client
 *
 */
public class DefaultZClient implements ZClient {

    private final ZSession session;

    public DefaultZClient(ZSession session) {
        this.session = session;
    }

    public ZSession getSession() {
        return session;
    }

    @Override
    public PQFSearchRetrieveRequest newPQFSearchRetrieveRequest() {
        return new PQFSearchRetrieveRequestHelper(this.getSession());
    }

    class PQFSearchRetrieveRequestHelper extends PQFSearchRetrieveRequest {
        PQFSearchRetrieveRequestHelper(ZSession session) {
            super(session);
            setDatabase(getDatabases());
            setResultSetName(getResultSetName());
            setPreferredRecordSyntax(getPreferredRecordSyntax());
            setElementSetName(getElementSetName());
        }
    }

    @Override
    public CQLSearchRetrieveRequest newCQLSearchRetrieveRequest() {
        return new CQLSearchRetrieveRequestHelper(session);
    }

    class CQLSearchRetrieveRequestHelper extends CQLSearchRetrieveRequest {
        CQLSearchRetrieveRequestHelper(ZSession session) {
            super(session);
            setDatabase(getDatabases());
            setResultSetName(getResultSetName());
            setPreferredRecordSyntax(getPreferredRecordSyntax());
            setElementSetName(getElementSetName());
        }
    }

    public void close() throws IOException {
        if (session.isOpen()) {
            session.close();
        }
    }

    @Override
    public ZSearchRetrieveResponse execute(ZSearchRetrieveRequest request) throws IOException {
        return request.execute();
    }

    @Override
    public List<String> getDatabases() {
        return Arrays.asList("");
    }

    @Override
    public String getPreferredRecordSyntax() {
        return "marc21";
    }

    @Override
    public String getResultSetName() {
        return "default";
    }

    @Override
    public String getElementSetName() {
        return "F";
    }

    @Override
    public String getEncoding() {
        return "ANSEL";
    }

    @Override
    public String getFormat() {
        return "MARC21";
    }

    @Override
    public String getType() {
        return "Bibliographic";
    }

}
