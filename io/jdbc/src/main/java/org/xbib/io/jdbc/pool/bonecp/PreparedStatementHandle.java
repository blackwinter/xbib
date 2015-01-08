/**
 *  Copyright 2010 Wallace Wadge
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.xbib.io.jdbc.pool.bonecp;

import org.xbib.io.jdbc.pool.bonecp.cache.StatementCache;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;


/**
 * Wrapper around JDBC PreparedStatement.
 *
 */
public class PreparedStatementHandle extends StatementHandle implements
        PreparedStatement {

    /**
     * Handle to the real prepared statement.
     */
    private PreparedStatement internalPreparedStatement;


    /**
     * PreparedStatement Wrapper constructor.
     *
     * @param internalPreparedStatement
     * @param sql                       sql statement
     * @param cache                     cache handle.
     * @param connectionHandle          Handle to the connection this is tied to.
     * @param cacheKey
     */
    public PreparedStatementHandle(PreparedStatement internalPreparedStatement,
                                   String sql, ConnectionHandle connectionHandle, String cacheKey, StatementCache cache) {
        super(internalPreparedStatement, sql, cache, connectionHandle, cacheKey);
        this.internalPreparedStatement = internalPreparedStatement;
        this.connectionHandle = connectionHandle;
        this.sql = sql;
        this.cache = cache;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.PreparedStatement#addBatch()
     */
    // @Override
    public void addBatch() throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.addBatch();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.PreparedStatement#clearParameters()
     */
    // @Override
    public void clearParameters() throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.clearParameters();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.PreparedStatement#execute()
     */
    // @Override
    public boolean execute() throws SQLException {
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, this.sql);
            }
            boolean result = this.internalPreparedStatement.execute();
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, this.sql);
            }
            return result;
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.PreparedStatement#executeQuery()
     */
    // @Override
    public ResultSet executeQuery() throws SQLException {
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, this.sql);
            }
            ResultSet result = this.internalPreparedStatement.executeQuery();
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, this.sql);
            }
            return result;
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public int executeUpdate() throws SQLException {
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, this.sql);
            }
            int result = this.internalPreparedStatement.executeUpdate();
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, this.sql);
            }
            return result;
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public ResultSetMetaData getMetaData() throws SQLException {
        checkClosed();
        try {
            return this.internalPreparedStatement.getMetaData();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        checkClosed();
        try {
            return this.internalPreparedStatement.getParameterMetaData();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setArray(int parameterIndex, Array x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setArray(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setBinaryStream(int parameterIndex, InputStream x)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setBinaryStream(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setBinaryStream(parameterIndex, x, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setBlob(int parameterIndex, InputStream inputStream)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setBlob(parameterIndex, inputStream);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setAsciiStream(parameterIndex, x, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setClob(parameterIndex, reader);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setRowId(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setSQLXML(parameterIndex, xmlObject);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setClob(int parameterIndex, Reader reader, long length)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setClob(parameterIndex, reader, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setNCharacterStream(int parameterIndex, Reader value)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setNCharacterStream(parameterIndex, value);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setNCharacterStream(int parameterIndex, Reader value,
                                    long length) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setNCharacterStream(parameterIndex, value, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setNClob(parameterIndex, value);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setNClob(parameterIndex, reader);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setNClob(int parameterIndex, Reader reader, long length)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setNClob(parameterIndex, reader, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setNString(int parameterIndex, String value)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setNString(parameterIndex, value);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setAsciiStream(int parameterIndex, InputStream x)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setAsciiStream(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader,
                                   long length) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setCharacterStream(parameterIndex, reader, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setBlob(parameterIndex, inputStream, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setCharacterStream(parameterIndex, reader);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setAsciiStream(parameterIndex, x, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setBigDecimal(int parameterIndex, BigDecimal x)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setBigDecimal(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    public void setBinaryStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setBinaryStream(parameterIndex, x, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setBlob(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }


    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setBoolean(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setByte(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setBytes(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setCharacterStream(parameterIndex,
                    reader, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setClob(int parameterIndex, Clob x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setClob(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setDate(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }
    public void setDate(int parameterIndex, Date x, Calendar cal)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setDate(parameterIndex, x, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setDouble(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setFloat(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setInt(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setLong(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setNull(parameterIndex, sqlType);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setNull(int parameterIndex, int sqlType, String typeName)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setNull(parameterIndex, sqlType, typeName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setObject(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setObject(parameterIndex, x, targetSqlType);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType,
                          int scaleOrLength) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setRef(int parameterIndex, Ref x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setRef(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setShort(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setString(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setTime(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setTime(int parameterIndex, Time x, Calendar cal)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setTime(parameterIndex, x, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setTimestamp(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setTimestamp(parameterIndex, x, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setURL(parameterIndex, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        checkClosed();
        try {
            this.internalPreparedStatement.setUnicodeStream(parameterIndex, x, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }


    /**
     * Returns the wrapped internal statement.
     *
     * @return the internalPreparedStatement that this wrapper is using.
     */
    public PreparedStatement getInternalPreparedStatement() {
        return this.internalPreparedStatement;
    }


    /**
     * Sets the internal statement that this wrapper wraps.
     *
     * @param internalPreparedStatement the internalPreparedStatement to set
     */
    public void setInternalPreparedStatement(PreparedStatement internalPreparedStatement) {
        this.internalPreparedStatement = internalPreparedStatement;
    }
}