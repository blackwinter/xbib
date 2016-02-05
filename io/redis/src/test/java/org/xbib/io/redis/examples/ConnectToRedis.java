package org.xbib.io.redis.examples;

import org.xbib.io.redis.RedisClient;
import org.xbib.io.redis.RedisConnection;
import org.xbib.io.redis.RedisURI;

public class ConnectToRedis {

    public static void main(String[] args) {
        // Syntax: redis://[password@]host[:port][/databaseNumber]
        RedisClient redisClient = new RedisClient(RedisURI.create("redis://password@localhost:6379/0"));
        RedisConnection<String, String> connection = redisClient.connect();

        System.out.println("Connected to Redis");

        connection.close();
        redisClient.shutdown();
    }
}
