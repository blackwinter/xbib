package org.xbib.io.redis.cluster;

import com.google.common.base.Splitter;
import com.google.common.net.HostAndPort;
import io.netty.channel.Channel;
import org.xbib.io.redis.LettuceStrings;
import org.xbib.io.redis.ReadFrom;
import org.xbib.io.redis.RedisAsyncConnectionImpl;
import org.xbib.io.redis.RedisChannelHandler;
import org.xbib.io.redis.RedisChannelWriter;
import org.xbib.io.redis.cluster.models.partitions.Partitions;
import org.xbib.io.redis.protocol.Command;
import org.xbib.io.redis.protocol.CommandArgs;
import org.xbib.io.redis.protocol.CommandKeyword;
import org.xbib.io.redis.protocol.ProtocolKeyword;
import org.xbib.io.redis.protocol.RedisCommand;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Channel writer for cluster operation. This writer looks up the right partition by hash/slot for the operation.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
class ClusterDistributionChannelWriter<K, V> implements RedisChannelWriter<K, V> {

    private RedisChannelWriter<K, V> defaultWriter;
    private ClusterConnectionProvider clusterConnectionProvider;
    private boolean closed = false;
    private int executionLimit = 5;

    public ClusterDistributionChannelWriter(RedisChannelWriter<K, V> defaultWriter) {
        this.defaultWriter = defaultWriter;
    }

    @Override
    public <T> RedisCommand<K, V, T> write(RedisCommand<K, V, T> command) {

        checkArgument(command != null, "command must not be null");

        RedisCommand<K, V, T> commandToSend = command;
        CommandArgs<K, V> args = command.getArgs();
        RedisChannelWriter<K, V> channelWriter = null;

        if (command instanceof Command) {
            Command<K, V, T> singleCommand = (Command<K, V, T>) command;
            if (!singleCommand.isMulti()) {
                commandToSend = new ClusterCommand<K, V, T>(singleCommand, this, executionLimit);
            }
        }

        if (commandToSend instanceof ClusterCommand) {
            ClusterCommand<K, V, T> clusterCommand = (ClusterCommand<K, V, T>) commandToSend;
            if (!clusterCommand.isDone()) {
                if (clusterCommand.isMoved()) {
                    HostAndPort moveTarget = getMoveTarget(clusterCommand.getError());
                    commandToSend.getOutput().setError((String) null);
                    RedisAsyncConnectionImpl<K, V> connection = clusterConnectionProvider.getConnection(
                            ClusterConnectionProvider.Intent.WRITE, moveTarget.getHostText(), moveTarget.getPort());
                    channelWriter = connection.getChannelWriter();
                }

                if (clusterCommand.isAsk()) {
                    HostAndPort askTarget = getAskTarget(clusterCommand.getError());
                    commandToSend.getOutput().setError((String) null);
                    RedisAsyncConnectionImpl<K, V> connection = clusterConnectionProvider.getConnection(
                            ClusterConnectionProvider.Intent.WRITE, askTarget.getHostText(), askTarget.getPort());
                    channelWriter = connection.getChannelWriter();

                    // set asking bit
                    connection.asking();
                }
            }
        }

        if (channelWriter == null && args != null && args.getEncodedKey() != null) {
            int hash = getHash(args.getEncodedKey());

            ClusterConnectionProvider.Intent intent = getIntent(command.getType());

            RedisAsyncConnectionImpl<K, V> connection = clusterConnectionProvider.getConnection(intent, hash);
            channelWriter = connection.getChannelWriter();
        }

        if (channelWriter instanceof ClusterDistributionChannelWriter) {
            ClusterDistributionChannelWriter<K, V> writer = (ClusterDistributionChannelWriter<K, V>) channelWriter;
            channelWriter = writer.defaultWriter;
        }

        if (channelWriter != null && channelWriter != this && channelWriter != defaultWriter) {
            return channelWriter.write(commandToSend);
        }

        return defaultWriter.write(commandToSend);
    }

    private ClusterConnectionProvider.Intent getIntent(ProtocolKeyword type) {
        for (ProtocolKeyword readOnlyCommand : ReadOnlyCommands.READ_ONLY_COMMANDS) {
            if (readOnlyCommand == type) {
                return ClusterConnectionProvider.Intent.READ;
            }
        }

        return ClusterConnectionProvider.Intent.WRITE;
    }

    private HostAndPort getMoveTarget(String errorMessage) {

        checkArgument(LettuceStrings.isNotEmpty(errorMessage), "errorMessage must not be empty");
        checkArgument(errorMessage.startsWith(CommandKeyword.MOVED.name()), "errorMessage must start with "
                + CommandKeyword.MOVED);

        List<String> movedMessageParts = Splitter.on(' ').splitToList(errorMessage);
        checkArgument(movedMessageParts.size() >= 3, "errorMessage must consist of 3 tokens (" + movedMessageParts + ")");

        return HostAndPort.fromString(movedMessageParts.get(2));
    }

    private HostAndPort getAskTarget(String errorMessage) {

        checkArgument(LettuceStrings.isNotEmpty(errorMessage), "errorMessage must not be empty");
        checkArgument(errorMessage.startsWith(CommandKeyword.ASK.name()), "errorMessage must start with " + CommandKeyword.ASK);

        List<String> movedMessageParts = Splitter.on(' ').splitToList(errorMessage);
        checkArgument(movedMessageParts.size() >= 3, "errorMessage must consist of 3 tokens (" + movedMessageParts + ")");

        return HostAndPort.fromString(movedMessageParts.get(2));
    }

    protected int getHash(byte[] encodedKey) {
        return SlotHash.getSlot(encodedKey);
    }

    @Override
    public void close() {

        if (closed) {
            return;
        }

        closed = true;

        if (defaultWriter != null) {
            defaultWriter.close();
            defaultWriter = null;
        }

        if (clusterConnectionProvider != null) {
            clusterConnectionProvider.close();
            clusterConnectionProvider = null;
        }

    }

    @Override
    public void setRedisChannelHandler(RedisChannelHandler<K, V> redisChannelHandler) {
        defaultWriter.setRedisChannelHandler(redisChannelHandler);
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        getClusterConnectionProvider().setAutoFlushCommands(autoFlush);
    }

    @Override
    public void flushCommands() {
        getClusterConnectionProvider().flushCommands();
    }

    @Override
    public Channel getChannel() {
        return defaultWriter.getChannel();
    }

    public ClusterConnectionProvider getClusterConnectionProvider() {
        return clusterConnectionProvider;
    }

    public void setClusterConnectionProvider(ClusterConnectionProvider clusterConnectionProvider) {
        this.clusterConnectionProvider = clusterConnectionProvider;
    }

    @Override
    public void reset() {
        defaultWriter.reset();
        clusterConnectionProvider.reset();
    }

    public void setPartitions(Partitions partitions) {
        clusterConnectionProvider.setPartitions(partitions);
    }

    /**
     * Gets the {@link ReadFrom} setting for this connection. Defaults to {@link ReadFrom#MASTER} if not set.
     *
     * @return the read from setting
     */
    public ReadFrom getReadFrom() {
        return clusterConnectionProvider.getReadFrom();
    }

    /**
     * Set from which nodes data is read. The setting is used as default for read operations on this connection. See the
     * documentation for {@link ReadFrom} for more information.
     *
     * @param readFrom the read from setting, must not be {@literal null}
     */
    public void setReadFrom(ReadFrom readFrom) {
        clusterConnectionProvider.setReadFrom(readFrom);
    }
}
