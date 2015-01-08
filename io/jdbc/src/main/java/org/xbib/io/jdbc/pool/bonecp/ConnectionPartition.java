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

import com.google.common.base.Objects;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Connection Partition structure
 *
 */
public class ConnectionPartition {

    /**
     * Connections available to be taken
     */
    private BlockingQueue<ConnectionHandle> freeConnections;
    /**
     * When connections start running out, add these number of new connections.
     */
    private final int acquireIncrement;
    /**
     * Minimum number of connections to start off with.
     */
    private final int minConnections;
    /**
     * Maximum number of connections that will ever be created.
     */
    private final int maxConnections;
    /**
     * Statistics lock.
     */
    protected ReentrantReadWriteLock statsLock = new ReentrantReadWriteLock();
    /**
     * Number of connections that have been created.
     */
    private int createdConnections = 0;
    /**
     * DB details.
     */
    private final String url;
    /**
     * DB details.
     */
    private final String username;
    /**
     * DB details.
     */
    private final String password;
    /**
     * If set to true, don't bother calling method to attempt to create
     * more connections because we've hit our limit.
     */
    private volatile boolean unableToCreateMoreTransactions = false;
    /**
     * Signal trigger to pool watch thread. Making it a queue means our signal is persistent.
     */
    private BlockingQueue<Object> poolWatchThreadSignalQueue = new ArrayBlockingQueue<Object>(1);
    /**
     * Store the unit translation here to avoid recalculating it in statement handles.
     */
    private long queryExecuteTimeLimitInNanoSeconds;
    /**
     * Cached copy of the config-specified pool name.
     */
    private String poolName;
    /**
     * Handle to the pool.
     */
    protected Pool pool;


    /**
     * Returns a handle to the poolWatchThreadSignalQueue
     *
     * @return the poolWatchThreadSignal
     */
    protected BlockingQueue<Object> getPoolWatchThreadSignalQueue() {
        return this.poolWatchThreadSignalQueue;
    }

    /**
     * Updates leased connections statistics
     *
     * @param increment value to add/subtract
     */
    protected void updateCreatedConnections(int increment) {

        try {
            this.statsLock.writeLock().lock();
            this.createdConnections += increment;
            //		assert this.createdConnections >= 0 : "Created connections < 0!";

        } finally {
            this.statsLock.writeLock().unlock();
        }
    }

    /**
     * Adds a free connection.
     *
     * @param connectionHandle
     * @throws java.sql.SQLException on error
     */
    protected void addFreeConnection(ConnectionHandle connectionHandle) throws SQLException {
        connectionHandle.setOriginatingPartition(this);
        // assume success to avoid racing where we insert an item in a queue and having that item immediately
        // taken and closed off thus decrementing the created connection count.
        updateCreatedConnections(1);

        // the instant the following line is executed, consumers can start making use of this
        // connection.
        if (!this.freeConnections.offer(connectionHandle)) {
            // we failed. rollback.
            updateCreatedConnections(-1); // compensate our createdConnection count.

            // terminate the internal handle.
            connectionHandle.internalClose();
        }
    }

    /**
     * @return the freeConnections
     */
    public BlockingQueue<ConnectionHandle> getFreeConnections() {
        return this.freeConnections;
    }

    /**
     * @param freeConnections the freeConnections to set
     */
    public void setFreeConnections(BlockingQueue<ConnectionHandle> freeConnections) {
        this.freeConnections = freeConnections;
    }


    /**
     * Partition constructor
     *
     * @param pool handle to connection pool
     */
    public ConnectionPartition(Pool pool) {
        PoolConfig config = pool.getConfig();
        this.minConnections = config.getMinConnectionsPerPartition();
        this.maxConnections = config.getMaxConnectionsPerPartition();
        this.acquireIncrement = config.getAcquireIncrement();
        this.url = config.getJdbcUrl();
        this.username = config.getUsername();
        this.password = config.getPassword();
        this.poolName = config.getPoolName() != null ? "(in pool '" + config.getPoolName() + "') " : "";
        this.pool = pool;

        this.queryExecuteTimeLimitInNanoSeconds = TimeUnit.NANOSECONDS.convert(config.getQueryExecuteTimeLimitInMs(), TimeUnit.MILLISECONDS);
    }

    /**
     * @return the acquireIncrement
     */
    public int getAcquireIncrement() {
        return this.acquireIncrement;
    }

    /**
     * @return the minConnections
     */
    public int getMinConnections() {
        return this.minConnections;
    }


    /**
     * @return the maxConnections
     */
    public int getMaxConnections() {
        return this.maxConnections;
    }

    /**
     * @return the leasedConnections
     */
    public int getCreatedConnections() {
        try {
            this.statsLock.readLock().lock();
            return this.createdConnections;
        } finally {
            this.statsLock.readLock().unlock();
        }
    }

    /**
     * @return the url
     */
    protected String getUrl() {
        return this.url;
    }


    /**
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }


    /**
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }


    /**
     * Returns true if we have created all the connections we can
     *
     * @return true if we have created all the connections we can
     */
    public boolean isUnableToCreateMoreTransactions() {
        return this.unableToCreateMoreTransactions;
    }


    /**
     * Sets connection creation possible status
     *
     * @param unableToCreateMoreTransactions t/f
     */
    public void setUnableToCreateMoreTransactions(boolean unableToCreateMoreTransactions) {
        this.unableToCreateMoreTransactions = unableToCreateMoreTransactions;
    }


    /**
     * Returns the number of avail connections
     *
     * @return avail connections.
     */
    public int getAvailableConnections() {
        return this.freeConnections.size();
    }

    /**
     * Returns no of free slots.
     *
     * @return remaining capacity.
     */
    public int getRemainingCapacity() {
        return this.freeConnections.remainingCapacity();
    }

    /**
     * Store the unit translation here to avoid recalculating it in the constructor of StatementHandle.
     *
     * @return value
     */
    public long getQueryExecuteTimeLimitinNanoSeconds() {
        return this.queryExecuteTimeLimitInNanoSeconds;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("url", this.pool.getConfig().getJdbcUrl())
                .add("user", this.pool.getConfig().getUsername())
                .add("minConnections", this.getMinConnections())
                .add("maxConnections", this.getMaxConnections())
                .add("acquireIncrement", this.acquireIncrement)
                .add("createdConnections", this.createdConnections)
                .add("freeConnections", this.getFreeConnections())
                .toString();
    }
}