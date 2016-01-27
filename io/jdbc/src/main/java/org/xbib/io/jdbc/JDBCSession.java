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
package org.xbib.io.jdbc;

import org.xbib.io.ObjectPacket;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.jdbc.operator.Query;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * SQL Session
 *
 */
public class JDBCSession implements Session {

    /** default encoding */
    public static final String DEFAULT_ENCODING = System.getProperty("file.encoding");
    /** connection */
    private transient Connection connection = null;
    /** locale */
    private Locale locale = Locale.getDefault();
    /** date */
    private DateFormat dateformat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);;
    /** date time pattern */
    private DateFormat datetimeformat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);;
    /** encoding */
    private String encoding = DEFAULT_ENCODING;
    /** is open */
    private boolean isOpen;

    /**
     * Creates a new SQLSession object.
     */
    public JDBCSession() {
    }

    @Override
    public void open(Mode mode) throws IOException {
        this.isOpen = true; // :-(
    }

    @Override
    public void close() throws IOException {
        this.isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public Packet read() throws IOException {
        //return readOp.read(this);
        return null;
    }

    @Override
    public void write(Packet packet) throws IOException {
        //writeOp.write(this, packet);
    }

    @Override
    public Packet newPacket() {
        return new ObjectPacket();
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        this.datetimeformat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
        this.dateformat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
    }
    
    public Locale getLocale() {
        return locale;
    }

    public DateFormat getDateFormat() {
        return dateformat;
    }

    public DateFormat getDateTimeFormat() {
        return datetimeformat;
    }

    /**
     * Set connection
     *
     * @param connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * The underlying connection of this session
     *
     * @return the connection object
     */
    public Connection getConnection() {
        return connection;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    /**
     * Get encoding
     *
     * @return the encoding of this session
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Set current date in a prepared triple
     *
     * @param p
     * @param pos
     *
     * @throws java.sql.SQLException
     */
    public void setDate(PreparedStatement p, int pos) throws SQLException {
        setDate(p, pos, datetimeformat, null);
    }

    /**
     * Set a java.sql.Date in a prepared triple
     *
     * @param p
     * @param pos
     * @param value
     *
     * @throws java.sql.SQLException
     */
    public void setDate(PreparedStatement p, int pos, Date value) throws SQLException {
        p.setDate(pos, new java.sql.Date(value.getTime()));
    }

    /**
     * Set a java.sql.Date in a prepared triple
     *
     * @param p
     * @param pos
     * @param value
     *
     * @throws java.sql.SQLException
     */
    public void setDate(PreparedStatement p, int pos, String value) throws SQLException {
        setDate(p, pos, dateformat, value);
    }

    /**
     * Set a java.sql.Date in a prepared triple
     *
     * @param p
     * @param pos
     * @param format
     * @param value
     *
     * @throws java.sql.SQLException
     */
    public void setDate(PreparedStatement p, int pos, DateFormat format, String value) throws SQLException {
        p.setDate(pos, convertToDate(format, value));
    }

    /**
     * Set string
     *
     * @param p
     * @param pos
     * @param s
     * @param columnSizes
     * @param columnName
     *
     * @throws java.sql.SQLException
     */
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
     * Set a bind parameter of type java.lang.String in a
     * PreparedStatement
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
            //logger.log(Level.FINEST, "string too long: {0}", s);
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
    public void setString(PreparedStatement p, int pos, String s) throws SQLException {
        if (s == null) {
            p.setNull(pos, Types.VARCHAR);
            return;
        }
        if (DEFAULT_ENCODING.equalsIgnoreCase(encoding)) {
            p.setString(pos, s);
            return;
        }
        try {
            byte[] b = s.getBytes(getEncoding());
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
        setTimestamp(p, pos, datetimeformat, null);
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
        setTimestamp(p, pos, datetimeformat, value);
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
        p.setTimestamp(pos, t);
    }

    /**
     * Get a java.sql.Timestamp formatted string from bind variable in
     * a result set
     *
     * @param r
     * @param pos
     *
     * @return a timestamp as string
     *
     * @throws java.sql.SQLException
     */
    public String getTimestamp(ResultSet r, int pos) throws SQLException {
        return getTimestamp(r, pos, datetimeformat);
    }

    /**
     * Get a java.sql.Timestamp formatted string from bind variable in
     * a result set
     *
     * @param r
     * @param pos
     * @param format
     *
     * @return a timestamp as string
     *
     * @throws java.sql.SQLException
     */
    public String getTimestamp(ResultSet r, int pos, DateFormat format) throws SQLException {
        return format.format(r.getTimestamp(pos));
    }

    /**
     * Get a java.sql.Timestamp formatted string from bind variable in
     * a result set
     *
     * @param r
     * @param pos
     *
     * @return a date
     *
     * @throws java.sql.SQLException
     */
    public Date getTimestampAsDate(ResultSet r, int pos) throws SQLException {
        return (Date) r.getTimestamp(pos);
    }

    /**
     *
     *
     * @param format
     * @param value
     *
     * @return a date
     */
    private java.sql.Date convertToDate(DateFormat format, String value) {
        if (value == null) {
            return new java.sql.Date(new Date().getTime());
        }
        java.sql.Date d;
        try {
            d = new java.sql.Date(format.parse(value).getTime());
        } catch (ParseException e1) {
            //logger.log(Level.WARNING, "not a valid date value: {0} required format: {1}", new Object[]{value, format});
            return null; //new java.sql.Date(new Date().getTime());
        }
        return d;
    }

    /**
     * Get a java.sql.Timestamp from a string in a given format. If the
     * value is null, the current date will be used.
     *
     * @param format the date format
     * @param value the date value
     *
     * @return the timestamp
     */
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
                //logger.log(Level.WARNING, "not a valid timestamp value: {0} required format: {1}", new Object[]{value, format});
                return null;// new Timestamp(new Date().getTime());
            }
        }
        return t;
    }

    /**
     * Get primary key
     *
     * @param table
     *
     * @return the primary key
     *
     * @throws java.sql.SQLException
     */
    public String getPrimaryKey(String table) throws SQLException {
        Map<String,String> m = new HashMap<>();
        m.put("table", table);
        Query query = new Query("select c.table_name, c.column_name, c.position,  c.constraint_name, c.owner from all_cons_columns c, all_constraints k where k.constraint_type = 'P' and k.constraint_name = c.constraint_name and k.table_name = c.table_name and k.owner = c.owner and k.table_name = ?",
                new String[]{"table"}, m);
        SQLSingleResult r = new SQLSingleResult(Types.VARCHAR, 2);
        try {
            query.addListener(r);
            query.execute(this);
        } catch (IOException ex) {
            throw new SQLException(ex.getMessage());
        }
        return r.value().toLowerCase();
    }
}
