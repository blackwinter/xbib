package org.xbib.io.redis;

import org.xbib.io.redis.protocol.RedisCommand;
import io.netty.channel.Channel;
import java.io.Closeable;

/**
 * Writer for a channel. Writers push commands on to the communication channel and maintain a state for the commands.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface RedisChannelWriter<K, V> extends Closeable {

    /**
     * Write a command on the channel. The command may be changed/wrapped during write and the written instance is returned
     * after the call.
     *
     * @param command the redis command
     * @param <T>     result type
     * @return the written redis command
     */
    <T> RedisCommand<K, V, T> write(RedisCommand<K, V, T> command);

    @Override
    void close();

    /**
     * Reset the writer state. Queued commands will be canceled and the internal state will be reset. This is useful when the
     * internal state machine gets out of sync with the connection.
     */
    void reset();

    /**
     * Set the corresponding connection instance in order to notify it about channel active/inactive state.
     *
     * @param redisChannelHandler the channel handler (external connection object)
     */
    void setRedisChannelHandler(RedisChannelHandler<K, V> redisChannelHandler);

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

    Channel getChannel();
}
