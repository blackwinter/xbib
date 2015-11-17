package org.xbib.io.redis.models.role;

/**
 * Represents a redis instance according to the {@code ROLE} output.
 */
public interface RedisInstance {

    /**
     * @return Redis instance role, see {@link Role}
     */
    Role getRole();

    /**
     * Possible Redis instance roles.
     */
    public enum Role {
        MASTER, SLAVE, SENTINEL;
    }
}
