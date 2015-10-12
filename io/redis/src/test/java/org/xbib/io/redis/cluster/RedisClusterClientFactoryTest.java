package org.xbib.io.redis.cluster;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.xbib.io.redis.RedisURI;
import org.xbib.io.redis.TestClientResources;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import org.xbib.io.redis.FastShutdown;
import org.xbib.io.redis.TestSettings;
import org.xbib.io.redis.resource.ClientResources;

public class RedisClusterClientFactoryTest {

    private final static String URI = "redis://" + TestSettings.host() + ":" + TestSettings.port();
    private final static RedisURI REDIS_URI = RedisURI.create(URI);
    private static final List<RedisURI> REDIS_URIS = Lists.newArrayList(REDIS_URI);
    private static ClientResources DEFAULT_RESOURCES;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DEFAULT_RESOURCES = TestClientResources.create();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        DEFAULT_RESOURCES.shutdown(100, 100, TimeUnit.MILLISECONDS).get();
    }

    @Test
    public void withStringUri() throws Exception {
        FastShutdown.shutdown(RedisClusterClient.create(URI));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withStringUriNull() throws Exception {
        RedisClusterClient.create((String) null);
    }

    @Test
    public void withUri() throws Exception {
        FastShutdown.shutdown(RedisClusterClient.create(REDIS_URI));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withUriUri() throws Exception {
        RedisClusterClient.create((RedisURI) null);
    }

    @Test
    public void withUriIterable() throws Exception {
        FastShutdown.shutdown(RedisClusterClient.create(Lists.newArrayList(REDIS_URI)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withUriIterableNull() throws Exception {
        RedisClusterClient.create((Iterable<RedisURI>) null);
    }

    @Test
    public void clientResourcesWithStringUri() throws Exception {
        FastShutdown.shutdown(RedisClusterClient.create(DEFAULT_RESOURCES, URI));
    }

    @Test(expected = IllegalArgumentException.class)
    public void clientResourcesWithStringUriNull() throws Exception {
        RedisClusterClient.create(DEFAULT_RESOURCES, (String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void clientResourcesNullWithStringUri() throws Exception {
        RedisClusterClient.create(null, URI);
    }

    @Test
    public void clientResourcesWithUri() throws Exception {
        FastShutdown.shutdown(RedisClusterClient.create(DEFAULT_RESOURCES, REDIS_URI));
    }

    @Test(expected = IllegalArgumentException.class)
    public void clientResourcesWithUriNull() throws Exception {
        RedisClusterClient.create(DEFAULT_RESOURCES, (RedisURI) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void clientResourcesWithUriUri() throws Exception {
        RedisClusterClient.create(null, REDIS_URI);
    }

    @Test
    public void clientResourcesWithUriIterable() throws Exception {
        FastShutdown.shutdown(RedisClusterClient.create(DEFAULT_RESOURCES, Lists.newArrayList(REDIS_URI)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void clientResourcesWithUriIterableNull() throws Exception {
        RedisClusterClient.create(DEFAULT_RESOURCES, (Iterable<RedisURI>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void clientResourcesNullWithUriIterable() throws Exception {
        RedisClusterClient.create(null, REDIS_URIS);
    }
}
