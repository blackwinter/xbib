package org.xbib.io.redis;

import org.xbib.io.redis.protocol.CommandArgs;

import java.util.ArrayList;
import java.util.List;

import static org.xbib.io.redis.protocol.CommandKeyword.AGGREGATE;
import static org.xbib.io.redis.protocol.CommandKeyword.MAX;
import static org.xbib.io.redis.protocol.CommandKeyword.MIN;
import static org.xbib.io.redis.protocol.CommandKeyword.SUM;
import static org.xbib.io.redis.protocol.CommandKeyword.WEIGHTS;

/**
 * Argument list builder for the redis <a href="http://redis.io/commands/zunionstore">ZUNIONSTORE</a> and <a
 * href="http://redis.io/commands/zinterstore">ZINTERSTORE</a> commands. Static import the methods from {@link Builder} and
 * chain the method calls: {@code weights(1, 2).max()}.
 */
public class ZStoreArgs {
    private List<Long> weights;
    private Aggregate aggregate;

    public ZStoreArgs weights(long... weights) {
        this.weights = new ArrayList<>(weights.length);
        for (long weight : weights) {
            this.weights.add(weight);
        }
        return this;
    }

    public ZStoreArgs sum() {
        aggregate = Aggregate.SUM;
        return this;
    }

    public ZStoreArgs min() {
        aggregate = Aggregate.MIN;
        return this;
    }

    public ZStoreArgs max() {
        aggregate = Aggregate.MAX;
        return this;
    }

    <K, V> void build(CommandArgs<K, V> args) {
        if (weights != null) {
            args.add(WEIGHTS);
            for (long weight : weights) {
                args.add(weight);
            }
        }

        if (aggregate != null) {
            args.add(AGGREGATE);
            switch (aggregate) {
                case SUM:
                    args.add(SUM);
                    break;
                case MIN:
                    args.add(MIN);
                    break;
                case MAX:
                    args.add(MAX);
                    break;
                default:
                    throw new IllegalArgumentException("Aggregation " + aggregate + " not supported");
            }
        }
    }

    private static enum Aggregate {
        SUM, MIN, MAX
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

        public static ZStoreArgs weights(long... weights) {
            return new ZStoreArgs().weights(weights);
        }

        public static ZStoreArgs sum() {
            return new ZStoreArgs().sum();
        }

        public static ZStoreArgs min() {
            return new ZStoreArgs().min();
        }

        public static ZStoreArgs max() {
            return new ZStoreArgs().max();
        }
    }
}
