package org.xbib.io.redis;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.Command;
import io.netty.channel.ChannelHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @param <K> Key type.
 * @param <V> Value type.
 */
@ChannelHandler.Sharable
class RedisSentinelAsyncConnectionImpl<K, V> extends RedisChannelHandler<K, V> implements RedisSentinelAsyncConnection<K, V> {

    private final SentinelCommandBuilder<K, V> commandBuilder;

    public RedisSentinelAsyncConnectionImpl(RedisChannelWriter<K, V> writer, RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        super(writer, timeout, unit);
        commandBuilder = new SentinelCommandBuilder<K, V>(codec);
    }

    @Override
    public Future<SocketAddress> getMasterAddrByName(K key) {

        Command<K, V, List<V>> cmd = commandBuilder.getMasterAddrByKey(key);
        final Future<List<V>> future = dispatch(cmd);

        Future<SocketAddress> result = Futures.lazyTransform(future, new Function<List<V>, SocketAddress>() {
            @Override
            public SocketAddress apply(List<V> input) {
                if (input.isEmpty()) {
                    return null;
                }

                checkArgument(input.size() == 2, "List must contain exact 2 entries (Hostname, Port)");
                String hostname = (String) input.get(0);
                String port = (String) input.get(1);
                return new InetSocketAddress(hostname, Integer.parseInt(port));
            }
        });

        return result;
    }

    @Override
    public RedisFuture<List<Map<K, V>>> masters() {

        return dispatch(commandBuilder.masters());
    }

    @Override
    public RedisFuture<Map<K, V>> master(K key) {

        return dispatch(commandBuilder.master(key));
    }

    @Override
    public RedisFuture<List<Map<K, V>>> slaves(K key) {

        return dispatch(commandBuilder.slaves(key));
    }

    @Override
    public RedisFuture<Long> reset(K key) {

        return dispatch(commandBuilder.reset(key));
    }

    @Override
    public RedisFuture<String> failover(K key) {

        return dispatch(commandBuilder.failover(key));
    }

    @Override
    public RedisFuture<String> monitor(K key, String ip, int port, int quorum) {

        return dispatch(commandBuilder.monitor(key, ip, port, quorum));
    }

    @Override
    public RedisFuture<String> set(K key, String option, V value) {

        return dispatch(commandBuilder.set(key, option, value));
    }

    @Override
    public RedisFuture<String> remove(K key) {
        return dispatch(commandBuilder.remove(key));
    }

    @Override
    public RedisFuture<String> ping() {
        return dispatch(commandBuilder.ping());
    }
}
