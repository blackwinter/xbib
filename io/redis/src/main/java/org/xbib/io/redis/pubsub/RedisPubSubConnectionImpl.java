package org.xbib.io.redis.pubsub;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.xbib.io.redis.RedisAsyncConnectionImpl;
import org.xbib.io.redis.RedisChannelWriter;
import org.xbib.io.redis.RedisFuture;
import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandArgs;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.xbib.io.redis.protocol.CommandType.PSUBSCRIBE;
import static org.xbib.io.redis.protocol.CommandType.PUNSUBSCRIBE;
import static org.xbib.io.redis.protocol.CommandType.SUBSCRIBE;
import static org.xbib.io.redis.protocol.CommandType.UNSUBSCRIBE;

/**
 * An asynchronous thread-safe pub/sub connection to a redis server. After one or more channels are subscribed to only pub/sub
 * related commands or {@link #quit} may be called.
 * <p>
 * Incoming messages and results of the {@link #subscribe}/{@link #unsubscribe} calls will be passed to all registered
 * {@link RedisPubSubListener}s.
 * <p>
 * A {@link org.xbib.io.redis.protocol.ConnectionWatchdog} monitors each connection and reconnects automatically until
 * {@link #close} is called. Channel and pattern subscriptions are renewed after reconnecting.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class RedisPubSubConnectionImpl<K, V> extends RedisAsyncConnectionImpl<K, V> implements RedisPubSubConnection<K, V> {

    protected final List<RedisPubSubListener<K, V>> listeners;
    protected final Set<K> channels;
    protected final Set<K> patterns;

    /**
     * Initialize a new connection.
     *
     * @param writer  the channel writer
     * @param codec   Codec used to encode/decode keys and values.
     * @param timeout Maximum time to wait for a responses.
     * @param unit    Unit of time for the timeout.
     */
    public RedisPubSubConnectionImpl(RedisChannelWriter<K, V> writer, RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        super(writer, codec, timeout, unit);
        listeners = Lists.newCopyOnWriteArrayList();
        channels = Sets.newConcurrentHashSet();
        patterns = Sets.newConcurrentHashSet();
    }

    /**
     * Add a new listener.
     *
     * @param listener Listener.
     */
    @Override
    public void addListener(RedisPubSubListener<K, V> listener) {
        listeners.add(listener);
    }

    /**
     * Remove an existing listener.
     *
     * @param listener Listener.
     */
    @Override
    public void removeListener(RedisPubSubListener<K, V> listener) {
        listeners.remove(listener);
    }

    @Override
    public RedisFuture<Void> psubscribe(K... patterns) {
        return new VoidFuture(dispatch(PSUBSCRIBE, new PubSubOutput<K, V, K>(codec), args(patterns)));
    }

    @Override
    public RedisFuture<Void> punsubscribe(K... patterns) {
        return new VoidFuture(dispatch(PUNSUBSCRIBE, new PubSubOutput<K, V, K>(codec), args(patterns)));
    }

    @Override
    public RedisFuture<Void> subscribe(K... channels) {
        return new VoidFuture(dispatch(SUBSCRIBE, new PubSubOutput<K, V, K>(codec), args(channels)));
    }

    @Override
    public RedisFuture<Void> unsubscribe(K... channels) {
        return new VoidFuture(dispatch(UNSUBSCRIBE, new PubSubOutput<K, V, K>(codec), args(channels)));
    }

    @Override
    public void activated() {
        super.activated();
        resubscribe();
    }

    /**
     * Re-subscribe to all previously subscribed channels and patterns.
     *
     * @return list of the futures of the {@literal subscribe} and {@literal psubscribe} commands.
     */
    protected List<RedisFuture<Void>> resubscribe() {

        List<RedisFuture<Void>> result = Lists.newArrayList();

        if (!channels.isEmpty()) {
            result.add(subscribe(toArray(channels)));
        }

        if (!patterns.isEmpty()) {
            result.add(psubscribe(toArray(patterns)));
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(Object msg) {
        PubSubOutput<K, V, V> output = (PubSubOutput<K, V, V>) msg;

        // drop empty messages
        if (output.type() == null || (output.pattern() == null && output.channel() == null && output.get() == null)) {
            return;
        }

        updateState(output);
        notifyListeners(output);
    }

    private void updateState(PubSubOutput<K, V, V> output) {
        switch (output.type()) {
            case psubscribe:
                patterns.add(output.pattern());
                break;
            case punsubscribe:
                patterns.remove(output.pattern());
                break;
            case subscribe:
                channels.add(output.channel());
                break;
            case unsubscribe:
                channels.remove(output.channel());
                break;
            default:
                break;
        }
    }

    private void notifyListeners(PubSubOutput<K, V, V> output) {
        // update listeners
        for (RedisPubSubListener<K, V> listener : listeners) {
            switch (output.type()) {
                case message:
                    listener.message(output.channel(), output.get());
                    break;
                case pmessage:
                    listener.message(output.pattern(), output.channel(), output.get());
                    break;
                case psubscribe:
                    listener.psubscribed(output.pattern(), output.count());
                    break;
                case punsubscribe:
                    listener.punsubscribed(output.pattern(), output.count());
                    break;
                case subscribe:
                    listener.subscribed(output.channel(), output.count());
                    break;
                case unsubscribe:
                    listener.unsubscribed(output.channel(), output.count());
                    break;
                default:
                    throw new UnsupportedOperationException("Operation " + output.type() + " not supported");
            }
        }
    }

    private CommandArgs<K, V> args(K... keys) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec);
        args.addKeys(keys);
        return args;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] toArray(Collection<T> c) {
        Class<T> cls = (Class<T>) c.iterator().next().getClass();
        T[] array = (T[]) Array.newInstance(cls, c.size());
        return c.toArray(array);
    }

}
