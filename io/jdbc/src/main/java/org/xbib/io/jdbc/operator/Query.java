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

import org.xbib.io.jdbc.ResultSetListener;
import org.xbib.io.jdbc.JDBCSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Query operator for SQL sessions
 *
 */
public class Query {

    protected JDBCSession session;
    /**
     * the request
     */
    private Map request;
    /**
     * lower bound
     */
    private String from;
    /**
     * the key
     */
    private String key;
    /**
     * primary key
     */
    private String primaryKey;
    /**
     * request parameter
     */
    private String[] requestParams;
    /**
     * SQL triple
     */
    private String sql;
    /**
     * table name
     */
    private String table;
    /**
     * upper bound
     */
    private String to;
    /**
     * the where clause
     */
    private String where;
    /**
     * column names
     */
    private String[] cols;
    private PreparedStatement pstmt;
    private ResultSet results;
    private LinkedList<ResultSetListener> listeners = new LinkedList();

    public Query(String sql) {
        this.sql = sql;
    }

    public Query(String table, Map request, String[] cols) {
        this.table = table;
        this.request = request;
        this.cols = cols;
    }

    public Query(String sql, String[] requestParams, Map request) {
        this.sql = sql;
        this.requestParams = requestParams;
        this.request = request;
    }

    public void close() {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            if (results != null) {
                results.close();
            }
        } catch (SQLException e) {

        }
    }

    public void setColumns(String[] cols) {
        this.cols = cols;
    }

    public String[] getColumns() {
        return cols;
    }

    public String getFrom() {
        return from;
    }

    public String getKey() {
        return key;
    }

    public void setPrimaryKey(String key) {
        this.primaryKey = key;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public Map getRequest() {
        return request;
    }

    public String[] getRequestParams() {
        return requestParams;
    }

    public void setSQL(String sql) {
        this.sql = sql;
    }

    public String getSQL() {
        return sql;
    }

    public String getTable() {
        return table;
    }

    public String getTo() {
        return to;
    }

    public String getWhere() {
        return where;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setRequest(Map request) {
        this.request = request;
    }

    public void setRequestParams(String[] requestParams) {
        this.requestParams = requestParams;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public void addListener(ResultSetListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ResultSetListener listener) {
        listeners.remove(listener);
    }

    public void execute(JDBCSession session) throws IOException {
        this.pstmt = null;
        this.results = null;
        try {
            this.session = session;
            pstmt = prepareStatement(session);
            results = pstmt.executeQuery();
            for (ResultSetListener listener : listeners) {
                listener.received(results);
            }
        } catch (SQLException ex) {
            throw new IOException(ex.getMessage(), ex);
        } finally {
            try {
                for (ResultSetListener listener : listeners) {
                    listener.close(results);
                }
            } catch (SQLException ex) {
                throw new IOException(ex.getMessage());
            }
        }
    }

    /**
     * Create a prepared triple
     *
     * @param session the session
     * @return a prepared triple
     * @throws java.sql.SQLException
     */
    protected PreparedStatement prepareStatement(JDBCSession session) throws SQLException {
        StringBuilder sb = new StringBuilder();
        if (getSQL() != null) {
            sb.append(getSQL());
        } else {
            String[] columns = getColumns() != null ? getColumns() : new String[]{
                        "*"
                    };
            sb.append("select ");
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(columns[i]);
            }
            sb.append(" from ").append(getTable());
            StringBuilder whereStr = new StringBuilder();
            if ((getKey() != null) && (getFrom() != null) && (getTo() != null)) {
                sb.append(" where ").append(getKey()).append(" between ? and ?");
                Map<String, Object> m = new LinkedHashMap<String, Object>();
                m.put("from", getFrom());
                m.put("to", getTo());
                setRequest(m);
                // find primary key from DB
                setPrimaryKey(session.getPrimaryKey(getTable().toUpperCase()));
            } else if ((getFrom() != null) && (getTo() != null)) {
                LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
                m.put("from", getFrom());
                m.put("to", getTo());
                setRequest(m);
                // find primary key from DB
                setPrimaryKey(session.getPrimaryKey(getTable().toUpperCase()));
                sb.append(" where ").append(getPrimaryKey()).append(" between ? and ?");
            } else if (getWhere() != null) {
                // a where clause for the primary key
                sb.append(" where ").append(getWhere());
                // find primary key from DB
                setPrimaryKey(session.getPrimaryKey(getTable().toUpperCase()));
            } else {
                // add column where clauses
                Map<String, Object> m = getRequest();
                if (m != null) {
                    for (Map.Entry<String, Object> me : m.entrySet()) {
                        if (whereStr.length() > 0) {
                            whereStr.append(" and ");
                        }
                        whereStr.append(me.getKey()).append(" = ? ");
                    }
                    sb.append((whereStr.length() > 0) ? (" where " + whereStr) : "");
                }
            }
        }
        //logger.log(Level.FINEST, sb.toString());
        //pstmt = (pstmt != null) ? pstmt : ((query instanceof PageableQuery) ? preparePageableStatement(sb.toString()) : prepareStatement(sb.toString()));
        this.pstmt = prepareStatement(session, sb.toString());
        if (getRequestParams() != null) {
            bind(pstmt, getRequest(), getRequestParams());
        } else if (getRequest() != null) {
            bind(pstmt, getRequest(), getRequest().keySet().toArray());
        }
        return pstmt;
    }

    protected PreparedStatement prepareStatement(JDBCSession session, String sql) throws SQLException {
        return session.getConnection().prepareStatement(sql);
    }

    protected void bind(PreparedStatement pstmt, Map m, Object[] keys) throws SQLException {
        for (int i = 1; i <= keys.length; i++) {
            Object value = m.get(keys[i - 1]);
            if (keys[i - 1] != null) {
                bind(pstmt, i, value);
            }
        }
    }

    protected void bind(PreparedStatement pstmt, int i, Object value) throws SQLException {
        if (value == null) {
            pstmt.setNull(i, Types.VARCHAR);
        } else if (value instanceof String) {
            setString(pstmt, i, (String) value);
        } else if (value instanceof Integer) {
            pstmt.setInt(i, ((Integer) value).intValue());
        } else if (value instanceof BigDecimal) {
            pstmt.setBigDecimal(i, (BigDecimal) value);
        } else if (value instanceof Timestamp) {
            setTimestamp(pstmt, i, (Timestamp) value);
        } else if (value instanceof Double) {
            pstmt.setDouble(i, ((Double) value).doubleValue());
        } else {
            pstmt.setObject(i, value);
        }
    }

    public void setString(PreparedStatement p, int pos, String s, Map columnSizes, String columnName) throws SQLException {
        if (columnSizes == null) {
            throw new SQLException("null map of column sizes");
        }
        Integer v = (Integer) columnSizes.get(columnName);
        if (v == null) {
            throw new SQLException("column not found: " + columnName + ", map = " + columnSizes);
        }
        setString(p, pos, s, v.intValue());
    }

    /**
     * Set a bind parameter of type java.lang.String in a PreparedStatement
     *
     * @param p the prepared triple
     * @param pos the position of the bind variable
     * @param s the string
     * @param len the string length
     *
     * @throws java.sql.SQLException if string can't be set
     */
    public void setString(PreparedStatement p, int pos, String s, int len) throws SQLException {
        if ((s == null) || (len < 0)) {
            return;
        }

        if (len == 0) {
            p.setString(pos, "");

            return;
        }
        while (s.length() > len) {
            //logger.log(Level.FINEST, "string too long: " + s);
            int ext = s.length() - len;
            s = s.substring(0, s.length() - ext);
        }
        setString(p, pos, s);
    }

    /**
     * Set a Java string to binary representation for databases with
     * character-set problems.
     *
     * @param p the prepared triple
     * @param pos the position in the triple
     * @param s the string
     *
     * @throws java.sql.SQLException if SQL fails
     */
    private void setString(PreparedStatement p, int pos, String s) throws SQLException {
        if (s == null) {
            p.setNull(pos, Types.VARCHAR);
            return;
        }
        if (JDBCSession.DEFAULT_ENCODING.equalsIgnoreCase(session.getEncoding())) {
            p.setString(pos, s);
            return;
        }
        try {
            byte[] b = s.getBytes(session.getEncoding());
            // Oracle driver supports "setAsciiStream" only on "varchar2" columns
            p.setAsciiStream(pos, new ByteArrayInputStream(b), b.length);
        } catch (UnsupportedEncodingException e) {
            //logger.log(Level.SEVERE, e.getMessage(), e);
            p.setString(pos, s);
        }
    }

    /**
     * Set current date in a prepared triple
     *
     * @param p
     * @param pos
     *
     * @throws java.sql.SQLException
     */
    public void setTimestamp(PreparedStatement p, int pos) throws SQLException {
        // current date
        setTimestamp(p, pos, session.getDateTimeFormat(), null);
    }

    /**
     * Set a java.sql.Timestamp in a prepared triple
     *
     * @param p
     * @param pos
     * @param value
     *
     * @throws java.sql.SQLException
     */
    public void setTimestamp(PreparedStatement p, int pos, Date value) throws SQLException {
        p.setTimestamp(pos, new Timestamp(value.getTime()));
    }

    /**
     * Set a java.sql.Timestamp in a prepared triple
     *
     * @param p
     * @param pos
     * @param value
     *
     * @throws java.sql.SQLException
     */
    public void setTimestamp(PreparedStatement p, int pos, String value) throws SQLException {
        setTimestamp(p, pos, session.getDateTimeFormat(), value);
    }

    /**
     * Set a java.sql.Timestamp in a prepared triple
     *
     * @param p
     * @param pos
     * @param format
     * @param value
     *
     * @throws java.sql.SQLException
     */
    public void setTimestamp(PreparedStatement p, int pos, DateFormat format, String value) throws SQLException {
        Timestamp t = convertToTimestamp(format, value);
        //logger.log(Level.FINEST, "converted timestamp = " + t);
        p.setTimestamp(pos, t);
    }

    private java.sql.Date convertToDate(String format, String value) {
        if (value == null) {
            return new java.sql.Date(new Date().getTime());
        }
        java.sql.Date d;
        try {
            d = new java.sql.Date(new SimpleDateFormat(format).parse(value).getTime());
        } catch (ParseException e1) {
            //logger.log(Level.WARNING, "not a valid date value: " + value + " required format: " + format);
            return new java.sql.Date(new Date().getTime());
        }
        return d;
    }

    private Timestamp convertToTimestamp(DateFormat format, String value) {
        if (value == null) {
            return new Timestamp(new Date().getTime());
        }
        Timestamp t;
        try {
            // full date / time
            t = new Timestamp(format.parse(value).getTime());
        } catch (ParseException e1) {
            // date
            try {
                // This is a kludge. Append midnight to the value and parse again.
                t = new Timestamp(format.parse(value + " 00:00:00").getTime());
            } catch (ParseException e2) {
                //logger.log(Level.WARNING, "not a valid timestamp value: " + value + " required format: " + format);

                return new Timestamp(new Date().getTime());
            }
        }

        return t;
    }
}
