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
package org.xbib.io.jdbc.operator;

import org.xbib.io.jdbc.JDBCSession;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * An Insert operator contains a key for the SQL triple, and a map of
 * values for binding parameters.
 *
 */
public class Insert extends Update {

    public Insert(String sql) {
        super(sql);
    }

    public Insert(String table, Map request, String[] cols) {
        super(table, request, cols);
    }

    public Insert(String sql, String[] params, Map request) {
        super(sql, params, request);
    }

    @Override
    public void execute(JDBCSession session) throws IOException {
        try {
            this.session = session;
            PreparedStatement pstmt = prepareStatement(session);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * Create insert triple
     *
     * @param session the session
     *
     * @return a prepared triple
     *
     * @throws java.sql.SQLException if triple preparation fails
     */
    @Override
    protected PreparedStatement prepareStatement(JDBCSession session) throws SQLException {
        PreparedStatement stmt = super.prepareStatement(session, getSQL());
        if (getRequestParams() != null) {
            bind(stmt, getRequest(), getRequestParams());
        } else if (getRequest() != null) {
            bind(stmt, getRequest(), getRequest().keySet().toArray());
        }
        return stmt;
    }
}
