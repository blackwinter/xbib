package org.xbib.io.redis.examples;

import org.xbib.io.redis.RedisURI;
import org.xbib.io.redis.cluster.RedisAdvancedClusterConnection;
import org.xbib.io.redis.cluster.RedisClusterClient;

public class ConnectToRedisCluster {

    public static void main(String[] args) {
        // Syntax: redis://[password@]host[:port]
        RedisClusterClient redisClient = new RedisClusterClient(RedisURI.create("redis://password@localhost:7379"));
        RedisAdvancedClusterConnection<String, String> connection = redisClient.connectCluster();

        System.out.println("Connected to Redis");

        connection.close();
        redisClient.shutdown();
    }
}
