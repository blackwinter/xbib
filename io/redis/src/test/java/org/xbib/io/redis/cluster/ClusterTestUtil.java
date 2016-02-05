package org.xbib.io.redis.cluster;

import org.xbib.io.redis.RedisClusterConnection;
import org.xbib.io.redis.cluster.models.partitions.ClusterPartitionParser;
import org.xbib.io.redis.cluster.models.partitions.Partitions;
import org.xbib.io.redis.cluster.models.partitions.RedisClusterNode;

public class ClusterTestUtil {

    public static String getNodeId(RedisClusterConnection<String, String> connection) {
        RedisClusterNode ownPartition = getOwnPartition(connection);
        if (ownPartition != null) {
            return ownPartition.getNodeId();
        }

        return null;
    }

    public static RedisClusterNode getOwnPartition(RedisClusterConnection<String, String> connection) {
        Partitions partitions = ClusterPartitionParser.parse(connection.clusterNodes());

        for (RedisClusterNode partition : partitions) {
            if (partition.getFlags().contains(RedisClusterNode.NodeFlag.MYSELF)) {
                return partition;
            }
        }
        return null;
    }
}
