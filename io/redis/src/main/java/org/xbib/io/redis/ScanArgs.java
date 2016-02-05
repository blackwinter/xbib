package org.xbib.io.redis;

import org.xbib.io.redis.protocol.CommandArgs;

import static org.xbib.io.redis.protocol.CommandKeyword.COUNT;
import static org.xbib.io.redis.protocol.CommandKeyword.MATCH;

/**
 * Argument list builder for the redis scan commans (scan, hscan, sscan, zscan) . Static import the methods from {@link Builder}
 * and chain the method calls: {@code matches("weight_*").limit(0, 2)}.
 */
public class ScanArgs {

    private Long count;
    private String match;

    /**
     * Match filter
     *
     * @param match the filter
     * @return the current instance of {@link ScanArgs}
     */
    public ScanArgs match(String match) {
        this.match = match;
        return this;
    }

    /**
     * Limit the scan by count
     *
     * @param count number of elements to scan
     * @return the current instance of {@link ScanArgs}
     */
    public ScanArgs limit(long count) {
        this.count = count;
        return this;
    }

    <K, V> void build(CommandArgs<K, V> args) {

        if (match != null) {
            args.add(MATCH).add(match);
        }

        if (count != null) {
            args.add(COUNT).add(count);
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

        /**
         * Create a new instance of {@link ScanArgs} with limit.
         *
         * @param count number of elements to scan
         * @return a new instance of {@link ScanArgs}
         */
        public static ScanArgs limit(long count) {
            return new ScanArgs().limit(count);
        }

        /**
         * Create a new instance of {@link ScanArgs} with match filter.
         *
         * @param matches the filter
         * @return a new instance of {@link ScanArgs}
         */
        public static ScanArgs matches(String matches) {
            return new ScanArgs().match(matches);
        }
    }

}
