package org.xbib.io.redis.examples;

import org.xbib.io.redis.RedisClient;
import org.xbib.io.redis.RedisConnection;
import org.xbib.io.redis.RedisURI;

public class ConnectToRedisUsingRedisSentinel {

    public static void main(String[] args) {
        // Syntax: redis-sentinel://[password@]host[:port][,host2[:port2]][/databaseNumber]#sentinelMasterId
        RedisClient redisClient = new RedisClient(RedisURI.create("redis-sentinel://localhost:26379,localhost:26380/0#mymaster"));
        RedisConnection<String, String> connection = redisClient.connect();

        System.out.println("Connected to Redis using Redis Sentinel");

        connection.close();
        redisClient.shutdown();
    }
}
