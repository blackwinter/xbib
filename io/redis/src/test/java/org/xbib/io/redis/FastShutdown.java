package org.xbib.io.redis;

import java.util.concurrent.TimeUnit;

import org.xbib.io.redis.resource.ClientResources;

public class FastShutdown {

    /**
     * Shut down a {@link AbstractRedisClient} with a timeout of 10ms.
     * 
     * @param redisClient client
     */
    public static void shutdown(AbstractRedisClient redisClient) {
        redisClient.shutdown(10, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * Shut down a {@link ClientResources} client with a timeout of 10ms.
     * 
     * @param clientResources client
     */
    public static void shutdown(ClientResources clientResources) {
        clientResources.shutdown(10, 10, TimeUnit.MILLISECONDS);
    }
}
