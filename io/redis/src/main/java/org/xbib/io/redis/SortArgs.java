package org.xbib.io.redis;

import org.xbib.io.redis.protocol.CommandArgs;
import org.xbib.io.redis.protocol.CommandKeyword;

import java.util.ArrayList;
import java.util.List;

import static org.xbib.io.redis.protocol.CommandKeyword.ALPHA;
import static org.xbib.io.redis.protocol.CommandKeyword.ASC;
import static org.xbib.io.redis.protocol.CommandKeyword.BY;
import static org.xbib.io.redis.protocol.CommandKeyword.DESC;
import static org.xbib.io.redis.protocol.CommandKeyword.LIMIT;
import static org.xbib.io.redis.protocol.CommandKeyword.STORE;
import static org.xbib.io.redis.protocol.CommandType.GET;

/**
 * Argument list builder for the redis <a href="http://redis.io/commands/sort">SORT</a> command. Static import the methods from
 * {@link Builder} and chain the method calls: {@code by("weight_*").desc().limit(0, 2)}.
 */
public class SortArgs {
    private String by;
    private Long offset, count;
    private List<String> get;
    private CommandKeyword order;
    private boolean alpha;

    public SortArgs by(String pattern) {
        by = pattern;
        return this;
    }

    public SortArgs limit(long offset, long count) {
        this.offset = offset;
        this.count = count;
        return this;
    }

    public SortArgs get(String pattern) {
        if (get == null) {
            get = new ArrayList<String>();
        }
        get.add(pattern);
        return this;
    }

    public SortArgs asc() {
        order = ASC;
        return this;
    }

    public SortArgs desc() {
        order = DESC;
        return this;
    }

    public SortArgs alpha() {
        alpha = true;
        return this;
    }

    <K, V> void build(CommandArgs<K, V> args, K store) {

        if (by != null) {
            args.add(BY);
            args.add(by);
        }

        if (get != null) {
            for (String pattern : get) {
                args.add(GET);
                args.add(pattern);
            }
        }

        if (offset != null) {
            args.add(LIMIT);
            args.add(offset);
            args.add(count);
        }

        if (order != null) {
            args.add(order);
        }

        if (alpha) {
            args.add(ALPHA);
        }

        if (store != null) {
            args.add(STORE);
            args.addKey(store);
        }
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

        public static SortArgs by(String pattern) {
            return new SortArgs().by(pattern);
        }

        public static SortArgs limit(long offset, long count) {
            return new SortArgs().limit(offset, count);
        }

        public static SortArgs get(String pattern) {
            return new SortArgs().get(pattern);
        }

        public static SortArgs asc() {
            return new SortArgs().asc();
        }

        public static SortArgs desc() {
            return new SortArgs().desc();
        }

        public static SortArgs alpha() {
            return new SortArgs().alpha();
        }
    }
}
