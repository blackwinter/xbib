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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * A connection factory for JDBC DataSources
 *
 */
public final class SQLConnectionFactory implements ConnectionFactory<SQLSession> {

    private static final Logger logger = LogManager.getLogger(SQLConnectionFactory.class.getName());

    private String jndiName;

    @Override
    public String getName() {
        return "jdbc";
    }

    @Override
    public Connection<SQLSession> getConnection(final URI uri) throws IOException {
        Context context = null;
        DataSource ds = null;
        for (String name : new String[]{
                    "jdbc/" + uri.getHost(),
                    "java:comp/env/" + uri.getScheme() + ":" + uri.getHost() + ":" + uri.getPort()
                }) {
            this.jndiName = name;
            try {
                context = new InitialContext();
                Object o = context.lookup(jndiName);
                if (o instanceof DataSource) {
                    logger.info( "DataSource ''{}'' found in naming context", jndiName);
                    ds = (DataSource) o;
                    break;
                } else {
                    logger.warn("JNDI object {} not a DataSource class: {} - ignored", jndiName, o.getClass());
                }
            } catch (NameNotFoundException e) {
                logger.warn("DataSource ''{}'' not found in context", jndiName);
            } catch (NamingException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        try {
            if (context != null && ds == null) {
                BasicDataSource bsource = new BasicDataSource();
                //bsource.setDriverClassName(properties.getProperty("driverClassName"));
                String url = uri.toString();
                        //properties.getProperty("jdbcScheme") + properties.getProperty("host") + ":" + properties.getProperty("port") + ("jdbc:oracle:thin:@".equals(properties.getProperty("jdbcScheme")) ? ":" : "/") + properties.getProperty("cluster");
                bsource.setUrl(url);
                String auth = uri.getAuthority();
                int pos = auth != null ? auth.indexOf(':') : 0;
                String username = pos > 0 ? auth.substring(0, pos) : null;
                String password = pos > 0 ? auth.substring(pos + 1) : auth;
                if (username != null) {
                    bsource.setUsername(username);
                }
                if (password != null) {
                    bsource.setPassword(password);
                }
                /*if (properties.containsKey("n")) {
                    bsource.setInitialSize(Integer.parseInt(properties.getProperty("n")));
                    bsource.setMaxActive(Integer.parseInt(properties.getProperty("n")));
                }*/
                // Other BasicDataSource settings, not used yet:
                //  setAccessToUnderlyingConnectionAllowed(boolean allow)
                //  setDefaultAutoCommit(boolean defaultAutoCommit)
                //  setDefaultCatalog(String defaultCatalog)
                //  setDefaultReadOnly(boolean defaultReadOnly)
                //  setDefaultTransactionIsolation(int defaultTransactionIsolation)
                //  setLogAbandoned(boolean logAbandoned)
                //  setLoginTimeout(int loginTimeout)
                //  setLogWriter(PrintWriter logWriter)
                //  setMaxIdle(int maxIdle)
                //  setMaxOpenPreparedStatements(int maxOpenStatements)
                //  setMaxWait(long maxWait)
                //  setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis)
                //  setMinIdle(int minIdle)
                //  setNumTestsPerEvictionRun(int numTestsPerEvictionRun)
                //  setPoolPreparedStatements(boolean poolingStatements)
                //  setTestOnBorrow(boolean testOnBorrow)
                //  setTestOnReturn(boolean testOnReturn)
                //  setTestWhileIdle(boolean testWhileIdle)
                //  setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis)
                //  setValidationQuery(String validationQuery)
                context.bind(jndiName, bsource);
                ds = (DataSource) bsource;
            }
        } catch (NamingException e) {
            throw new IOException(e.getMessage());
        }
        /*try {
            ds.getConnection().setAutoCommit("false".equals(properties.getProperty("autoCommit")) ? false : true);
        } catch (SQLException e) {
            throw new IOException(e.getMessage());
        }*/
        return new SQLConnection(ds);
    }

    @Override
    public InputStream open(URI uri) throws IOException {
        return uri.toURL().openStream();
    }

    @Override
    public boolean canOpen(URI uri) {
        return uri.getScheme().startsWith("jdbc");
    }
}
