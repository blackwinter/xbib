package org.xbib.io.redis;

import org.xbib.io.redis.protocol.CommandArgs;

import static org.xbib.io.redis.protocol.CommandKeyword.ADDR;
import static org.xbib.io.redis.protocol.CommandKeyword.ID;
import static org.xbib.io.redis.protocol.CommandKeyword.SKIPME;
import static org.xbib.io.redis.protocol.CommandType.TYPE;

/**
 * Argument list builder for the redis <a href="http://redis.io/commands/client-kill">CLIENT KILL</a> command. Static import the
 * methods from {@link Builder} and chain the method calls: {@code id(1).skipme()}.
 */
public class KillArgs {

    private Boolean skipme;
    private String addr;
    private Long id;
    private Type type;

    public KillArgs skipme() {
        return this.skipme(true);
    }

    public KillArgs skipme(boolean state) {
        this.skipme = state;
        return this;
    }

    public KillArgs addr(String addr) {
        this.addr = addr;
        return this;
    }

    public KillArgs id(long id) {
        this.id = id;
        return this;
    }

    public KillArgs type(Type type) {
        this.type = type;
        return this;
    }

    <K, V> void build(CommandArgs<K, V> args) {

        if (skipme != null) {
            args.add(SKIPME).add(skipme ? "yes" : "no");
        }

        if (id != null) {
            args.add(ID).add(id);
        }

        if (addr != null) {
            args.add(ADDR).add(addr);
        }

        if (type != null) {
            args.add(TYPE).add(type.name().toLowerCase());
        }

    }

    private static enum Type {
        NORMAL, SLAVE, PUBSUB
    }

    /**
     * Static builder methods.
     */
    public static class Builder {

        /**
         * Utility constructor.
         */
        private Builder() {

        }

        public static KillArgs skipme() {
            return new KillArgs().skipme();
        }

        public static KillArgs addr(String addr) {
            return new KillArgs().addr(addr);
        }

        public static KillArgs id(long id) {
            return new KillArgs().id(id);
        }

        public static KillArgs typePubsub() {
            return new KillArgs().type(Type.PUBSUB);
        }

        public static KillArgs typeNormal() {
            return new KillArgs().type(Type.NORMAL);
        }

        public static KillArgs typeSlave() {
            return new KillArgs().type(Type.SLAVE);
        }

    }
}
