package org.xbib.io.redis.cluster;

import org.xbib.io.redis.ReadFrom;
import org.xbib.io.redis.RedisAsyncConnectionImpl;
import org.xbib.io.redis.RedisException;
import org.xbib.io.redis.cluster.models.partitions.Partitions;

import java.io.Closeable;

/**
 * Connection provider for cluster operations.
 */
interface ClusterConnectionProvider extends Closeable {
    /**
     * Provide a connection for the intent and cluster slot. The underlying connection is bound to the nodeId. If the slot
     * responsibility changes, the connection will not point to the updated nodeId.
     *
     * @param intent Connection intent {@literal READ} or {@literal WRITE}
     * @param slot   the slot-hash of the key, see {@link SlotHash}
     * @return a valid connection which handles the slot.
     * @throws RedisException if no host is handling the slot
     */
    <K, V> RedisAsyncConnectionImpl<K, V> getConnection(Intent intent, int slot);

    /**
     * Provide a connection for the intent and host/port. The connection can survive cluster topology updates. The connection *
     * will be closed if the node identified by {@code host} and {@code port} is no longer part of the cluster.
     *
     * @param intent Connection intent {@literal READ} or {@literal WRITE}
     * @param host   the host
     * @param port   the port
     * @return a valid connection to the given host.
     * @throws RedisException if the host is not part of the cluster
     */
    <K, V> RedisAsyncConnectionImpl<K, V> getConnection(Intent intent, String host, int port);

    /**
     * Provide a connection for the intent and nodeId. The connection can survive cluster topology updates. The connection will
     * be closed if the node identified by {@code nodeId} is no longer part of the cluster.
     *
     * @param intent Connection intent {@literal READ} or {@literal WRITE}
     * @param nodeId the nodeId of the cluster node
     * @return a valid connection to the given nodeId.
     * @throws RedisException if the {@code nodeId} is not part of the cluster
     */
    <K, V> RedisAsyncConnectionImpl<K, V> getConnection(Intent intent, String nodeId);

    /**
     * Close the connections and free all resources.
     */
    @Override
    void close();

    /**
     * Reset the writer state. Queued commands will be canceled and the internal state will be reset. This is useful when the
     * internal state machine gets out of sync with the connection.
     */
    void reset();

    /**
     * Close connections that are not in use anymore/not part of the cluster.
     */
    void closeStaleConnections();

    /**
     * Update partitions.
     *
     * @param partitions
     */
    void setPartitions(Partitions partitions);

    /**
     * Disable or enable auto-flush behavior. Default is {@literal true}. If autoFlushCommands is disabled, multiple commands
     * can be issued without writing them actually to the transport. Commands are buffered until a {@link #flushCommands()} is
     * issued. After calling {@link #flushCommands()} commands are sent to the transport and executed by Redis.
     *
     * @param autoFlush state of autoFlush.
     */
    void setAutoFlushCommands(boolean autoFlush);

    /**
     * Flush pending commands. This commands forces a flush on the channel and can be used to buffer ("pipeline") commands to
     * achieve batching. No-op if channel is not connected.
     */
    void flushCommands();

    /**
     * Gets the {@link ReadFrom} setting for this connection. Defaults to {@link ReadFrom#MASTER} if not set.
     *
     * @return the read from setting
     */
    ReadFrom getReadFrom();

    /**
     * Set from which nodes data is read. The setting is used as default for read operations on this connection. See the
     * documentation for {@link ReadFrom} for more information.
     *
     * @param readFrom the read from setting, must not be {@literal null}
     */
    void setReadFrom(ReadFrom readFrom);

    public static enum Intent {
        READ, WRITE;
    }
}
