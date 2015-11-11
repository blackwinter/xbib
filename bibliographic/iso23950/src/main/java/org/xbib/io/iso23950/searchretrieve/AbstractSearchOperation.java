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

import asn1.ASN1Any;
import asn1.ASN1Boolean;
import asn1.ASN1Exception;
import asn1.ASN1GeneralString;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.iso23950.ZSession;
import z3950.v3.DatabaseName;
import z3950.v3.InternationalString;
import z3950.v3.OtherInformation1;
import z3950.v3.PDU;
import z3950.v3.Query;
import z3950.v3.RPNQuery;
import z3950.v3.SearchRequest;
import z3950.v3.SearchResponse;

/**
 *  Base class for Z39.50 Search operation
 *
 */
public abstract class AbstractSearchOperation {

    private final static Logger logger = LogManager.getLogger(AbstractSearchOperation.class.getName());

    private long timeout;

    private long millis;

    private boolean status;

    private int[] results;

    private List<String> databases;

    private String resultSetName;

    private int count;

    public AbstractSearchOperation() {
        this.status = false;
        this.count = 0;
    }

    public AbstractSearchOperation setDatabases(List<String> databases) {
        this.databases = databases;
        return this;
    }

    public AbstractSearchOperation setResultSetName(String resultSetName) {
        this.resultSetName = resultSetName;
        return this;
    }

    public void query(ZSession session, String query) throws IOException {
        long t0 = System.currentTimeMillis();
        long t1;
        try {
            RPNQuery rpn = getQuery(query);
            logger.debug("rpn query={} databases={} resultSetName={}", rpn, databases, resultSetName);
            SearchRequest search = new SearchRequest();
            search.s_query = new Query();
            search.s_query.c_type_1 = rpn;
            search.s_smallSetUpperBound = new ASN1Integer(0);
            search.s_largeSetLowerBound = new ASN1Integer(1);
            search.s_mediumSetPresentNumber = new ASN1Integer(0);
            search.s_replaceIndicator = new ASN1Boolean(true);
            search.s_resultSetName = new InternationalString();
            search.s_resultSetName.value = new ASN1GeneralString(resultSetName);
            DatabaseName dbs[] = new DatabaseName[databases.size()];
            for (int n = 0; n < databases.size(); n++) {
                dbs[n] = new DatabaseName();
                dbs[n].value = new InternationalString();
                dbs[n].value.value = new ASN1GeneralString(databases.get(n));
            }
            search.s_databaseNames = dbs;
            PDU pduRequest = new PDU();
            pduRequest.c_searchRequest = search;
            session.getConnection().setTimeout(getTimeout());
            session.getConnection().writePDU(pduRequest);
            PDU pduResponse = session.getConnection().readPDU();
            t1 = System.currentTimeMillis();
            setMillis(t1 -t0);
            SearchResponse response = pduResponse.c_searchResponse;
            this.count = response.s_resultCount.get();
            if (response.s_searchStatus != null) {
                this.status = response.s_searchStatus.get();
                if (status == false) {
                    String message = "no message";
                    if (response.s_records != null && response.s_records.c_nonSurrogateDiagnostic != null) {
                        try {
                            message = "ASN error, non-surrogate diagnostics: " + response.s_records.c_nonSurrogateDiagnostic.ber_encode();
                        } catch (ASN1Exception e) {
                        }
                    }
                    throw new IOException(session.getConnection().getURI().getHost() + ": " + message);
                }
            }
            if (response.s_additionalSearchInfo != null && response.s_additionalSearchInfo.value[0] != null) {
                OtherInformation1 info = response.s_additionalSearchInfo.value[0];
                ASN1Sequence targetSeq = (ASN1Sequence) info.s_information.c_externallyDefinedInfo.c_singleASN1type;
                ASN1Any[] targets = targetSeq.get();
                this.results = new int[targets.length];
                DatabaseName dbName;
                for (int i = 0; i < targets.length; i++) {
                    try {
                        ASN1Sequence target = (ASN1Sequence) targets[i];
                        ASN1Any[] details = target.get();
                        dbName = new DatabaseName(details[0].ber_encode(), false);
                        if (!dbName.value.value.get().equalsIgnoreCase(databases.get(i))) {
                            String message = "database name listed in additional search info doesn't match database name in names set.";
                            throw new IOException(session.getConnection().getURI().getHost() + ": " + message);
                        }
                        ASN1Integer res = (ASN1Integer) details[1];
                        results[i] = res.get();
                    } catch (ASN1Exception ex) {
                        // non-fatal String message = "Error in accessing additional search info.";
                        results[i] = -1;
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            t1 = System.currentTimeMillis();
            setMillis(t1 - t0);
            throw new IOException(session.getConnection().getURI().getHost() + ": timeout (" 
                    + getMillis() + " millis passed, max "
                    + session.getConnection().getTimeout() + " millis)" , e);
        }
    }

    public boolean isSuccess() {
        return status;
    }

    public int getResultCount() {
        return count;
    }
    
    public void setMillis(long millis) {
        this.millis = millis;
    }
    
    public long getMillis() {
        return millis;
    }

    public void setTimeout(long millis) {
        this.timeout = millis;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public abstract RPNQuery getQuery(String query) throws IOException;

}
