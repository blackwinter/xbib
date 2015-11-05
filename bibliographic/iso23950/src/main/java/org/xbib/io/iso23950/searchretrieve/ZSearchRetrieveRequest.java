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
package org.xbib.io.iso23950.searchretrieve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.Session;
import org.xbib.io.iso23950.ErrorRecord;
import org.xbib.io.iso23950.InitOperation;
import org.xbib.io.iso23950.PresentOperation;
import org.xbib.io.iso23950.Record;
import org.xbib.io.iso23950.RecordHandler;
import org.xbib.io.iso23950.ZSession;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;

/**
 * Init / Search / Present operation with result processing
 *
 */
public abstract class ZSearchRetrieveRequest extends SearchRetrieveRequest {

    private final static Logger logger = LogManager.getLogger(ZSearchRetrieveRequest.class.getName());

    private static final ResourceBundle recordSyntaxBundle =
            ResourceBundle.getBundle("org.xbib.io.iso23950.recordsyntax");

    private final ZSession session;

    private String user;

    private String password;

    private List<String> databases;

    private String query;

    private int offset;

    private int length;

    private String resultSetName;

    private String elementSetName;

    private String preferredRecordSyntax;

    private long presentMillis;

    private final ByteArrayOutputStream records = new ByteArrayOutputStream();

    private final ByteArrayOutputStream errors = new ByteArrayOutputStream();

    public ZSearchRetrieveRequest(ZSession session) {
        this.session = session;
    }

    public ZSearchRetrieveRequest setUser(String user) {
        this.user = user;
        return this;
    }

    public ZSearchRetrieveRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public ZSearchRetrieveRequest setQuery(String query) {
        this.query = query;
        return this;
    }

    public ZSearchRetrieveRequest setFrom(int offset) {
        this.offset = offset;
        return this;
    }

    public ZSearchRetrieveRequest setSize(int length) {
        this.length = length;
        return this;
    }

    public ZSearchRetrieveRequest setDatabase(List<String> databases) {
        this.databases = databases;
        return this;
    }

    public ZSearchRetrieveRequest setResultSetName(String resultSetName) {
        this.resultSetName = resultSetName;
        return this;
    }

    public ZSearchRetrieveRequest setElementSetName(String elementSetName) {
        this.elementSetName = elementSetName;
        return this;
    }

    public ZSearchRetrieveRequest setPreferredRecordSyntax(String preferredRecordSyntax) {
        this.preferredRecordSyntax = recordSyntaxBundle.containsKey(preferredRecordSyntax)
                ? recordSyntaxBundle.getString(preferredRecordSyntax.toLowerCase()) : preferredRecordSyntax;
        return this;
    }

    public ZSearchRetrieveResponse execute() throws IOException {
        if (query == null) {
            throw new IOException("no query");
        }
        if (offset < 1) {
            // Z39.50 bails out when offset = 0
            this.offset = 1;
        }
        long t0 = System.currentTimeMillis();
        if (!session.isOpen()) {
            session.open(Session.Mode.READ);
            if (!session.isOpen()) {
                logger.error("{} unable to open session [{}]", databases, query);
                throw new IOException("session not open");
            }
            if (!session.isAuthenticated()) {
                InitOperation init = new InitOperation(user, password, null);
                init.execute(session);
                session.setAuthenticated(!init.rejected());
                if (!session.isAuthenticated()) {
                    logger.error("{} session not authenticated [{}]", databases, query);
                    throw new IOException("could not authenticate");
                }
            }
        }
        AbstractSearchOperation search = getSearchOperation(databases, resultSetName);
        search.setTimeout(session.getConnection().getTimeout() * 1000);
        search.query(session, query);
        if (!search.isSuccess()) {
            logger.warn("{} search was not a success [{}]", databases, query);
        } else {
            PresentOperation present = new PresentOperation(
                    resultSetName, elementSetName, preferredRecordSyntax,
                    offset, length);
            RecordHandler handler  = new RecordHandler() {
                @Override
                public void receivedRecord(Record record) {
                    try {
                        if (record instanceof ErrorRecord) {
                            errors.write(record.getContent());
                        } else {
                            records.write(record.getContent());
                        }
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            };
            present.execute(session, handler);
            this.presentMillis = present.getMillis();
        }
        long t1 = System.currentTimeMillis();
        logger.info("{} [{}ms] [{}ms] [{}ms] [{}] [{}]",
                databases, t1 - t0, search.getMillis(), presentMillis, search.getResultCount(), query);
        if (!search.isSuccess()) {
            throw new IOException("search was not a success");
        }
        return new ZSearchRetrieveResponse(this)
                .setResultCount(search.getResultCount())
                .setSession(session)
                .setRecords(records.toByteArray())
                .setErrors(errors.toByteArray());
    }

    protected abstract AbstractSearchOperation getSearchOperation(List<String> database, String resultSetName);
}
