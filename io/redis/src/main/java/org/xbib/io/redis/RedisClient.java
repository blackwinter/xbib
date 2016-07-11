package org.xbib.io.redis;

import com.google.common.base.Supplier;
import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.codec.Utf8StringCodec;
import org.xbib.io.redis.protocol.CommandHandler;
import org.xbib.io.redis.protocol.RedisCommand;
import org.xbib.io.redis.pubsub.PubSubCommandHandler;
import org.xbib.io.redis.pubsub.RedisPubSubConnection;
import org.xbib.io.redis.pubsub.RedisPubSubConnectionImpl;
import org.xbib.io.redis.resource.ClientResources;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.xbib.io.redis.Strings.isEmpty;
import static org.xbib.io.redis.Strings.isNotEmpty;

/**
 * A scalable thread-safe <a href="http://redis.io/">Redis</a> client. Multiple threads may share one connection if they avoid
 * blocking and transactional operations such as BLPOP and MULTI/EXEC. {@link RedisClient} is an expensive resource. It holds a
 * set of netty's {@link io.netty.channel.EventLoopGroup}'s that consist of up to {@code Number of CPU's * 4} threads. Reuse
 * this instance as much as possible.
 */
public class RedisClient extends AbstractRedisClient {

    private final RedisURI redisURI;

    protected RedisClient(ClientResources clientResources, RedisURI redisURI) {
        super(clientResources);
        this.redisURI = redisURI;
    }

    /**
     * Creates a uri-less RedisClient. You can connect to different Redis servers but you must supply a {@link RedisURI} on
     * connecting. Methods without having a {@link RedisURI} will fail with a {@link IllegalStateException}.
     *
     * @deprecated Use the factory method {@link #create()}
     */
    @Deprecated
    public RedisClient() {
        super(null);
        redisURI = null;
        setDefaultTimeout(60, TimeUnit.MINUTES);
    }

    /**
     * Create a new client that connects to the supplied host on the default port.
     *
     * @param host Server hostname.
     * @deprecated Use the factory method {@link #create(String)}
     */
    @Deprecated
    public RedisClient(String host) {
        this(host, RedisURI.DEFAULT_REDIS_PORT);
    }

    /**
     * Create a new client that connects to the supplied host and port. Connection attempts and non-blocking commands will
     * {@link #setDefaultTimeout timeout} after 60 seconds.
     *
     * @param host Server hostname.
     * @param port Server port.
     * @deprecated Use the factory method {@link #create(RedisURI)}
     */
    @Deprecated
    public RedisClient(String host, int port) {
        this(RedisURI.Builder.redis(host, port).build());
    }

    /**
     * Create a new client that connects to the supplied host and port. Connection attempts and non-blocking commands will
     * {@link #setDefaultTimeout timeout} after 60 seconds.
     *
     * @param redisURI Redis URI.
     * @deprecated Use the factory method {@link #create(RedisURI)}
     */
    @Deprecated
    public RedisClient(RedisURI redisURI) {
        super();
        this.redisURI = redisURI;
        setDefaultTimeout(redisURI.getTimeout(), redisURI.getUnit());
    }

    /**
     * Creates a uri-less RedisClient with default {@link ClientResources}. You can connect to different Redis servers but you
     * must supply a {@link RedisURI} on connecting. Methods without having a {@link RedisURI} will fail with a
     * {@link IllegalStateException}.
     *
     * @return a new instance of {@link RedisClient}
     */
    public static RedisClient create() {
        return new RedisClient(null, null);
    }

    /**
     * Create a new client that connects to the supplied {@link RedisURI uri} with default {@link ClientResources}. You can
     * connect to different Redis servers but you must supply a {@link RedisURI} on connecting.
     *
     * @param redisURI the Redis URI, must not be {@literal null}
     * @return a new instance of {@link RedisClient}
     */
    public static RedisClient create(RedisURI redisURI) {
        assertNotNull(redisURI);
        return new RedisClient(null, redisURI);
    }

    /**
     * Create a new client that connects to the supplied uri with default {@link ClientResources}. You can connect to different
     * Redis servers but you must supply a {@link RedisURI} on connecting.
     *
     * @param uri the Redis URI, must not be {@literal null}
     * @return a new instance of {@link RedisClient}
     */
    public static RedisClient create(String uri) {
        checkArgument(uri != null, "uri must not be null");
        return new RedisClient(null, RedisURI.create(uri));
    }

    /**
     * Creates a uri-less RedisClient with shared {@link ClientResources}. You need to shut down the {@link ClientResources}
     * upon shutting down your application. You can connect to different Redis servers but you must supply a {@link RedisURI} on
     * connecting. Methods without having a {@link RedisURI} will fail with a {@link IllegalStateException}.
     *
     * @param clientResources the client resources, must not be {@literal null}
     * @return a new instance of {@link RedisClient}
     */
    public static RedisClient create(ClientResources clientResources) {
        assertNotNull(clientResources);
        return new RedisClient(clientResources, null);
    }

    /**
     * Create a new client that connects to the supplied uri with shared {@link ClientResources}.You need to shut down the
     * {@link ClientResources} upon shutting down your application. You can connect to different Redis servers but you must
     * supply a {@link RedisURI} on connecting.
     *
     * @param clientResources the client resources, must not be {@literal null}
     * @param uri             the Redis URI, must not be {@literal null}
     * @return a new instance of {@link RedisClient}
     */
    public static RedisClient create(ClientResources clientResources, String uri) {
        assertNotNull(clientResources);
        checkArgument(uri != null, "uri must not be null");
        return create(clientResources, RedisURI.create(uri));
    }

    /**
     * Create a new client that connects to the supplied {@link RedisURI uri} with shared {@link ClientResources}. You need to
     * shut down the {@link ClientResources} upon shutting down your application.You can connect to different Redis servers but
     * you must supply a {@link RedisURI} on connecting.
     *
     * @param clientResources the client resources, must not be {@literal null}
     * @param redisURI        the Redis URI, must not be {@literal null}
     * @return a new instance of {@link RedisClient}
     */
    public static RedisClient create(ClientResources clientResources, RedisURI redisURI) {
        assertNotNull(clientResources);
        assertNotNull(redisURI);
        return new RedisClient(clientResources, redisURI);
    }

    private static <K, V> void assertNotNull(RedisCodec<K, V> codec) {
        checkArgument(codec != null, "RedisCodec must not be null");
    }

    private static void assertNotNull(RedisURI redisURI) {
        checkArgument(redisURI != null, "RedisURI must not be null");
    }

    private static void assertNotNull(ClientResources clientResources) {
        checkArgument(clientResources != null, "ClientResources must not be null");
    }

    protected long makeTimeout() {
        return TimeUnit.MILLISECONDS.convert(timeout, unit);
    }

    private void checkForRedisURI() {
        checkState(this.redisURI != null,
                "RedisURI is not available. Use RedisClient(Host), RedisClient(Host, Port) or RedisClient(RedisURI) to construct your client.");
    }

    /**
     * Open a new synchronous connection to a Redis server that treats keys and values as UTF-8 strings.
     *
     * @return A new connection
     */
    public RedisConnection<String, String> connect() {
        return connect(newStringStringCodec());
    }

    /**
     * Open a new synchronous connection to a Redis server. Use the supplied {@link RedisCodec codec} to encode/decode keys and
     * values.
     *
     * @param codec Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param <K>   Key type
     * @param <V>   Value type
     * @return A new connection
     */
    @SuppressWarnings("unchecked")
    public <K, V> RedisConnection<K, V> connect(RedisCodec<K, V> codec) {
        checkForRedisURI();
        return connect(codec, this.redisURI);
    }

    /**
     * Open a new synchronous connection to a Redis server using the supplied {@link RedisURI} that treats keys and values as
     * UTF-8 strings.
     *
     * @param redisURI the Redis server to connect to, must not be {@literal null}
     * @return A new connection
     */
    @SuppressWarnings("unchecked")
    public RedisConnection<String, String> connect(RedisURI redisURI) {
        checkValidRedisURI(redisURI);
        return connect(newStringStringCodec(), redisURI);
    }

    /**
     * Open a new synchronous connection to a Redis server using the supplied {@link RedisURI} and the supplied
     * {@link RedisCodec codec} to encode/decode keys.
     *
     * @param codec    Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param redisURI the Redis server to connect to, must not be {@literal null}
     * @param <K>      Key type
     * @param <V>      Value type
     * @return A new connection
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <K, V> RedisConnection connect(RedisCodec<K, V> codec, RedisURI redisURI) {
        return (RedisConnection<K, V>) syncHandler((RedisChannelHandler<K, V>) connectAsync(codec, redisURI),
                RedisConnection.class, RedisClusterConnection.class);
    }

    /**
     * Open a new asynchronous connection to a Redis server that treats keys and values as UTF-8 strings.
     *
     * @return A new connection
     */
    public RedisAsyncConnection<String, String> connectAsync() {
        return connectAsync(newStringStringCodec());
    }

    /**
     * Open a new asynchronous connection to a Redis server. Use the supplied {@link RedisCodec codec} to encode/decode keys and
     * values.
     *
     * @param codec Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param <K>   Key type
     * @param <V>   Value type
     * @return A new connection
     */
    public <K, V> RedisAsyncConnection<K, V> connectAsync(RedisCodec<K, V> codec) {
        checkForRedisURI();
        return connectAsync(codec, redisURI);
    }

    /**
     * Open a new asynchronous connection to a Redis server using the supplied {@link RedisURI} that treats keys and values as
     * UTF-8 strings.
     *
     * @param redisURI the Redis server to connect to, must not be {@literal null}
     * @return A new connection
     */
    public RedisAsyncConnection<String, String> connectAsync(RedisURI redisURI) {
        checkValidRedisURI(redisURI);
        return connectAsync(newStringStringCodec(), redisURI);
    }

    /**
     * Open a new asynchronous connection to a Redis server using the supplied {@link RedisURI} and the supplied
     * {@link RedisCodec codec} to encode/decode keys.
     *
     * @param codec    Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param redisURI the Redis server to connect to, must not be {@literal null}
     * @param <K>      Key type
     * @param <V>      Value type
     * @return A new connection
     */
    public <K, V> RedisAsyncConnection<K, V> connectAsync(RedisCodec<K, V> codec, RedisURI redisURI) {
        checkArgument(codec != null, "RedisCodec must not be null");
        assertNotNull(redisURI);

        Queue<RedisCommand<K, V, ?>> queue = new ArrayDeque<RedisCommand<K, V, ?>>();

        CommandHandler<K, V> handler = new CommandHandler<K, V>(clientOptions, clientResources, queue);
        RedisAsyncConnectionImpl<K, V> connection = newRedisAsyncConnectionImpl(handler, codec, timeout, unit);

        connectAsync(handler, connection, redisURI);

        return connection;
    }

    private <K, V> void connectAsync(CommandHandler<K, V> handler, RedisAsyncConnectionImpl<K, V> connection, RedisURI redisURI) {

        ConnectionBuilder connectionBuilder;
        if (redisURI.isSsl()) {
            SslConnectionBuilder sslConnectionBuilder = SslConnectionBuilder.sslConnectionBuilder();
            sslConnectionBuilder.ssl(redisURI);
            connectionBuilder = sslConnectionBuilder;
        } else {
            connectionBuilder = ConnectionBuilder.connectionBuilder();
        }

        connectionBuilder.clientOptions(clientOptions);
        connectionBuilder.clientResources(clientResources);
        connectionBuilder(handler, connection, getSocketAddressSupplier(redisURI), connectionBuilder, redisURI);
        channelType(connectionBuilder, redisURI);
        initializeChannel(connectionBuilder);

        if (redisURI.getPassword() != null && redisURI.getPassword().length != 0) {
            connection.auth(new String(redisURI.getPassword()));
        }

        if (redisURI.getDatabase() != 0) {
            connection.select(redisURI.getDatabase());
        }
    }

    /**
     * Open a new pub/sub connection to a Redis server that treats keys and values as UTF-8 strings.
     *
     * @return A new pub/sub connection
     */
    public RedisPubSubConnection<String, String> connectPubSub() {
        return connectPubSub(newStringStringCodec());
    }

    /**
     * Open a new pub/sub connection to a Redis server using the supplied {@link RedisURI} that treats keys and values as UTF-8
     * strings.
     *
     * @param redisURI the Redis server to connect to, must not be {@literal null}
     * @return A new pub/sub connection
     */
    public RedisPubSubConnection<String, String> connectPubSub(RedisURI redisURI) {
        checkValidRedisURI(redisURI);
        return connectPubSub(newStringStringCodec(), redisURI);
    }

    /**
     * Open a new pub/sub connection to the Redis server using the supplied {@link RedisURI} and use the supplied
     * {@link RedisCodec codec} to encode/decode keys and values.
     *
     * @param codec Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param <K>   Key type
     * @param <V>   Value type
     * @return A new pub/sub connection
     */
    public <K, V> RedisPubSubConnection<K, V> connectPubSub(RedisCodec<K, V> codec) {
        checkForRedisURI();
        return connectPubSub(codec, redisURI);
    }

    /**
     * Open a new pub/sub connection to the Redis server using the supplied {@link RedisURI} and use the supplied
     * {@link RedisCodec codec} to encode/decode keys and values.
     *
     * @param codec    Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param redisURI the redis server to connect to, must not be {@literal null}
     * @param <K>      Key type
     * @param <V>      Value type
     * @return A new connection
     */
    public <K, V> RedisPubSubConnection<K, V> connectPubSub(RedisCodec<K, V> codec, RedisURI redisURI) {
        checkArgument(codec != null, "RedisCodec must not be null");
        assertNotNull(redisURI);

        Queue<RedisCommand<K, V, ?>> queue = new ArrayDeque<RedisCommand<K, V, ?>>();
        PubSubCommandHandler<K, V> handler = new PubSubCommandHandler<K, V>(clientOptions, clientResources, queue, codec);
        RedisPubSubConnectionImpl<K, V> connection = newRedisPubSubConnectionImpl(handler, codec, timeout, unit);

        connectAsync(handler, connection, redisURI);

        return connection;
    }

    /**
     * Open a new asynchronous connection to a Redis Sentinel that treats keys and values as UTF-8 strings. You must supply a
     * valid RedisURI containing one or more sentinels.
     *
     * @return a new connection
     */
    public RedisSentinelAsyncConnection<String, String> connectSentinelAsync() {
        return connectSentinelAsync(newStringStringCodec());
    }

    /**
     * Open a new asynchronous connection to a Redis Sentinela nd use the supplied {@link RedisCodec codec} to encode/decode
     * keys and values. You must supply a valid RedisURI containing one or more sentinels.
     *
     * @param codec Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param <K>   Key type
     * @param <V>   Value type
     * @return a new connection
     */
    public <K, V> RedisSentinelAsyncConnection<K, V> connectSentinelAsync(RedisCodec<K, V> codec) {
        checkForRedisURI();
        return connectSentinelAsyncImpl(codec, redisURI);
    }

    /**
     * Open a new asynchronous connection to a Redis Sentinel using the supplied {@link RedisURI} that treats keys and values as
     * UTF-8 strings. You must supply a valid RedisURI containing a redis host or one or more sentinels.
     *
     * @param redisURI the Redis server to connect to, must not be {@literal null}
     * @return A new connection
     */
    public RedisSentinelAsyncConnection<String, String> connectSentinelAsync(RedisURI redisURI) {
        return connectSentinelAsyncImpl(newStringStringCodec(), redisURI);
    }

    /**
     * Open a new asynchronous connection to a Redis Sentinel using the supplied {@link RedisURI} and use the supplied
     * {@link RedisCodec codec} to encode/decode keys and values. You must supply a valid RedisURI containing a redis host or
     * one or more sentinels.
     *
     * @param codec    Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param redisURI the Redis server to connect to, must not be {@literal null}
     * @param <K>      Key type
     * @param <V>      Value type
     * @return A new connection
     */
    public <K, V> RedisSentinelAsyncConnection<K, V> connectSentinelAsync(RedisCodec<K, V> codec, RedisURI redisURI) {
        return connectSentinelAsyncImpl(codec, redisURI);
    }

    private <K, V> RedisSentinelAsyncConnection<K, V> connectSentinelAsyncImpl(RedisCodec<K, V> codec, RedisURI redisURI) {

        assertNotNull(codec);
        assertNotNull(redisURI);

        Queue<RedisCommand<K, V, ?>> queue = new ArrayDeque<RedisCommand<K, V, ?>>();

        ConnectionBuilder connectionBuilder = ConnectionBuilder.connectionBuilder();
        connectionBuilder.clientOptions(ClientOptions.copyOf(getOptions()));
        connectionBuilder.clientResources(clientResources);

        final CommandHandler<K, V> commandHandler = new CommandHandler<K, V>(clientOptions, clientResources, queue);
        final RedisSentinelAsyncConnectionImpl<K, V> connection = newRedisSentinelAsyncConnectionImpl(commandHandler, codec,
                timeout, unit);

        logger.debug("Trying to get a Sentinel connection for one of: " + redisURI.getSentinels());

        connectionBuilder(commandHandler, connection, getSocketAddressSupplier(redisURI), connectionBuilder, redisURI);

        if (redisURI.getSentinels().isEmpty() && (isNotEmpty(redisURI.getHost()) || !isEmpty(redisURI.getSocket()))) {
            channelType(connectionBuilder, redisURI);
            initializeChannel(connectionBuilder);
        } else {
            boolean connected = false;
            boolean first = true;
            Exception causingException = null;
            validateUrisAreOfSameConnectionType(redisURI.getSentinels());
            for (RedisURI uri : redisURI.getSentinels()) {
                if (first) {
                    channelType(connectionBuilder, uri);
                    first = false;
                }
                connectionBuilder.socketAddressSupplier(getSocketAddressSupplier(uri));
                logger.debug("Connecting to Sentinel, address: " + uri.getResolvedAddress());
                try {
                    initializeChannel(connectionBuilder);
                    connected = true;
                    break;
                } catch (Exception e) {
                    logger.warn("Cannot connect sentinel at " + uri + ": " + e.toString());
                    causingException = e;
                    if (e instanceof ConnectException) {
                        continue;
                    }
                }
            }
            if (!connected) {
                throw new RedisConnectionException("Cannot connect to a sentinel: " + redisURI.getSentinels(), causingException);
            }
        }

        return connection;
    }

    private void validateUrisAreOfSameConnectionType(List<RedisURI> redisUris) {
        boolean unixDomainSocket = false;
        boolean inetSocket = false;
        for (RedisURI sentinel : redisUris) {
            if (sentinel.getSocket() != null) {
                unixDomainSocket = true;
            }
            if (sentinel.getHost() != null) {
                inetSocket = true;
            }
        }

        if (unixDomainSocket && inetSocket) {
            throw new RedisConnectionException("You cannot mix unix domain socket and IP socket URI's");
        }
    }

    /**
     * Construct a new {@link RedisAsyncConnectionImpl}. Can be overridden in order to construct a subclass of
     * {@link RedisAsyncConnectionImpl}
     *
     * @param channelWriter the channel writer
     * @param codec         the codec to use
     * @param timeout       Timeout value
     * @param unit          Timeout unit
     * @param <K>           Key type.
     * @param <V>           Value type.
     * @return RedisAsyncConnectionImpl&lt;K, V&gt; instance
     */
    protected <K, V> RedisAsyncConnectionImpl<K, V> newRedisAsyncConnectionImpl(RedisChannelWriter<K, V> channelWriter,
                                                                                RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        return new RedisAsyncConnectionImpl<K, V>(channelWriter, codec, timeout, unit);
    }

    /**
     * Construct a new {@link RedisSentinelAsyncConnectionImpl}. Can be overridden in order to construct a subclass of
     * {@link RedisSentinelAsyncConnectionImpl}
     *
     * @param channelWriter the channel writer
     * @param codec         the codec to use
     * @param timeout       Timeout value
     * @param unit          Timeout unit
     * @param <K>           Key type.
     * @param <V>           Value type.
     * @return RedisSentinelAsyncConnectionImpl&lt;K, V&gt; instance
     */
    protected <K, V> RedisSentinelAsyncConnectionImpl<K, V> newRedisSentinelAsyncConnectionImpl(
            RedisChannelWriter<K, V> channelWriter, RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        return new RedisSentinelAsyncConnectionImpl<K, V>(channelWriter, codec, timeout, unit);
    }

    /**
     * Construct a new {@link RedisPubSubConnectionImpl}. Can be overridden in order to construct a subclass of
     * {@link RedisPubSubConnectionImpl}
     *
     * @param channelWriter the channel writer
     * @param codec         the codec to use
     * @param timeout       Timeout value
     * @param unit          Timeout unit
     * @param <K>           Key type.
     * @param <V>           Value type.
     * @return RedisPubSubConnectionImpl&lt;K, V&gt; instance
     */
    protected <K, V> RedisPubSubConnectionImpl<K, V> newRedisPubSubConnectionImpl(RedisChannelWriter<K, V> channelWriter,
                                                                                  RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        return new RedisPubSubConnectionImpl<K, V>(channelWriter, codec, timeout, unit);
    }

    private Supplier<SocketAddress> getSocketAddressSupplier(final RedisURI redisURI) {
        return new Supplier<SocketAddress>() {
            @Override
            public SocketAddress get() {
                try {
                    return getSocketAddress(redisURI);
                } catch (InterruptedException e) {
                    throw new RedisException(e);
                } catch (TimeoutException e) {
                    throw new RedisException(e);
                } catch (ExecutionException e) {
                    throw new RedisException(e);
                }
            }
        };
    }

    /**
     * Returns the {@link ClientResources} which are used with that client.
     *
     * @return the {@link ClientResources} for this client
     */
    public ClientResources getResources() {
        return clientResources;
    }

    protected SocketAddress getSocketAddress(RedisURI redisURI) throws InterruptedException, TimeoutException,
            ExecutionException {
        SocketAddress redisAddress;

        if (redisURI.getSentinelMasterId() != null && !redisURI.getSentinels().isEmpty()) {
            logger.debug("Connecting to Redis using Sentinels " + redisURI.getSentinels() + ", MasterId "
                    + redisURI.getSentinelMasterId());
            redisAddress = lookupRedis(redisURI.getSentinelMasterId());

            if (redisAddress == null) {
                throw new RedisConnectionException("Cannot provide redisAddress using sentinel for masterId "
                        + redisURI.getSentinelMasterId());
            }

        } else {
            redisAddress = redisURI.getResolvedAddress();
        }
        return redisAddress;
    }

    private SocketAddress lookupRedis(String sentinelMasterId) throws InterruptedException, TimeoutException,
            ExecutionException {
        RedisSentinelAsyncConnection<String, String> connection = connectSentinelAsync();
        try {
            return connection.getMasterAddrByName(sentinelMasterId).get(timeout, unit);
        } finally {
            connection.close();
        }
    }

    private void checkValidRedisURI(RedisURI redisURI) {
        checkArgument(redisURI != null && isNotEmpty(redisURI.getHost()), "A valid RedisURI with a host is needed");
    }

    protected Utf8StringCodec newStringStringCodec() {
        return new Utf8StringCodec();
    }
}
