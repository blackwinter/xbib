package org.xbib.io.redis;

import com.google.common.collect.Lists;
import org.xbib.io.redis.models.role.RedisInstance;
import org.xbib.io.redis.models.role.RedisNodeDescription;

import java.util.Collections;
import java.util.List;

/**
 * Collection of common read setting implementations.
 */
class ReadFromImpl {

    /**
     * Read from master only.
     */
    static final class ReadFromMaster extends ReadFrom {
        @Override
        public List<RedisNodeDescription> select(Nodes nodes) {
            for (RedisNodeDescription node : nodes) {
                if (node.getRole() == RedisInstance.Role.MASTER) {
                    return Lists.newArrayList(node);
                }
            }
            return Collections.emptyList();
        }
    }

    /**
     * Read preffered from master. If the master is not available, read from a slave.
     */
    static final class ReadFromMasterPreferred extends ReadFrom {
        @Override
        public List<RedisNodeDescription> select(Nodes nodes) {
            List<RedisNodeDescription> result = Lists.newArrayList();

            for (RedisNodeDescription node : nodes) {
                if (node.getRole() == RedisInstance.Role.MASTER) {
                    result.add(node);
                }
            }

            for (RedisNodeDescription node : nodes) {
                if (node.getRole() == RedisInstance.Role.SLAVE) {
                    result.add(node);
                }
            }
            return result;
        }
    }

    /**
     * Read from slave only.
     */
    static final class ReadFromSlave extends ReadFrom {
        @Override
        public List<RedisNodeDescription> select(Nodes nodes) {
            List<RedisNodeDescription> result = Lists.newArrayList();
            for (RedisNodeDescription node : nodes) {
                if (node.getRole() == RedisInstance.Role.SLAVE) {
                    result.add(node);
                }
            }
            return result;
        }
    }

    /**
     * Read from nearest node.
     */
    static final class ReadFromNearest extends ReadFrom {
        @Override
        public List<RedisNodeDescription> select(Nodes nodes) {
            return nodes.getNodes();
        }
    }
}
