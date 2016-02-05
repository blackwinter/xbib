package org.xbib.io.redis.cluster.models.partitions;

import org.junit.Test;
import org.xbib.io.redis.RedisURI;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisClusterNodeTest {
    @Test
    public void testEquality() throws Exception {
        RedisClusterNode node = new RedisClusterNode();

        assertThat(node).isEqualTo(new RedisClusterNode());
        assertThat(node.hashCode()).isEqualTo(new RedisClusterNode().hashCode());

        node.setUri(new RedisURI());
        assertThat(node.hashCode()).isNotEqualTo(new RedisClusterNode());
    }

    @Test
    public void testToString() throws Exception {
        RedisClusterNode node = new RedisClusterNode();

        assertThat(node.toString()).contains(RedisClusterNode.class.getSimpleName());
    }
}
