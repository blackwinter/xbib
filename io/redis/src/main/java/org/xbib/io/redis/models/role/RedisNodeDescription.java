package org.xbib.io.redis.models.role;

import org.xbib.io.redis.RedisURI;

/**
 * Description of a single Redis Node.
 */
public interface RedisNodeDescription extends RedisInstance {

    /**
     * @return the URI of the node
     */
    RedisURI getUri();
}
