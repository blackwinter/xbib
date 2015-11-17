package org.xbib.io.redis;

import java.util.concurrent.TimeUnit;

import org.xbib.io.redis.resource.ClientResources;
import org.xbib.io.redis.resource.DefaultClientResources;

/**
 * Client-Resources suitable for testing. Uses {@link TestEventLoopGroupProvider} to preserve the event
 * loop groups between tests. Every time a new {@link TestClientResources} instance is created, shutdown hook is added
 * {@link Runtime#addShutdownHook(Thread)}.
 * 
 */
public class TestClientResources {

    public static ClientResources create() {
        final DefaultClientResources resources = new DefaultClientResources.Builder().eventLoopGroupProvider(
                new TestEventLoopGroupProvider()).build();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    resources.shutdown(100, 100, TimeUnit.MILLISECONDS).get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return resources;
    }
}
