package org.xbib.io.redis.models.command;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Parser for redis <a href="http://redis.io/commands/command">COMMAND</a>/<a
 * href="http://redis.io/commands/command-info">COMMAND INFO</a>command output.
 */
public class CommandDetailParser {

    /**
     * Number of array elements for a specific command.
     */
    public static final int COMMAND_INFO_SIZE = 6;

    protected static final Map<String, CommandDetail.Flag> FLAG_MAPPING;

    static {
        ImmutableMap.Builder<String, CommandDetail.Flag> builder = ImmutableMap.builder();
        builder.put("admin", CommandDetail.Flag.ADMIN);
        builder.put("asking", CommandDetail.Flag.ASKING);
        builder.put("denyoom", CommandDetail.Flag.DENYOOM);
        builder.put("fast", CommandDetail.Flag.FAST);
        builder.put("loading", CommandDetail.Flag.LOADING);
        builder.put("noscript", CommandDetail.Flag.NOSCRIPT);
        builder.put("movablekeys", CommandDetail.Flag.MOVABLEKEYS);
        builder.put("pubsub", CommandDetail.Flag.PUBSUB);
        builder.put("random", CommandDetail.Flag.RANDOM);
        builder.put("readonly", CommandDetail.Flag.READONLY);
        builder.put("skip_monitor", CommandDetail.Flag.SKIP_MONITOR);
        builder.put("sort_for_script", CommandDetail.Flag.SORT_FOR_SCRIPT);
        builder.put("stale", CommandDetail.Flag.STALE);
        builder.put("write", CommandDetail.Flag.WRITE);
        FLAG_MAPPING = builder.build();
    }

    private CommandDetailParser() {
    }

    /**
     * Parse the output of the redis COMMAND/COMMAND INFO command and convert to a list of {@link CommandDetail}.
     *
     * @param commandOutput the command output, must not be {@literal null}
     * @return RedisInstance
     */
    public static List<CommandDetail> parse(List<?> commandOutput) {
        checkArgument(commandOutput != null, "CommandOutput must not be null");

        List<CommandDetail> result = Lists.newArrayList();

        for (Object o : commandOutput) {
            if (!(o instanceof Collection<?>)) {
                continue;
            }

            Collection<?> collection = (Collection<?>) o;
            if (collection.size() != COMMAND_INFO_SIZE) {
                continue;
            }

            CommandDetail commandDetail = parseCommandDetail(collection);
            result.add(commandDetail);
        }

        return Collections.unmodifiableList(result);
    }

    private static CommandDetail parseCommandDetail(Collection<?> collection) {
        Iterator<?> iterator = collection.iterator();
        String name = (String) iterator.next();
        int arity = Ints.checkedCast(getLongFromIterator(iterator, 0));
        Object flags = iterator.next();
        int firstKey = Ints.checkedCast(getLongFromIterator(iterator, 0));
        int lastKey = Ints.checkedCast(getLongFromIterator(iterator, 0));
        int keyStepCount = Ints.checkedCast(getLongFromIterator(iterator, 0));

        Set<CommandDetail.Flag> parsedFlags = parseFlags(flags);

        return new CommandDetail(name, arity, parsedFlags, firstKey, lastKey, keyStepCount);
    }

    private static Set<CommandDetail.Flag> parseFlags(Object flags) {
        Set<CommandDetail.Flag> result = Sets.newHashSet();

        if (flags instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) flags;
            for (Object o : collection) {
                CommandDetail.Flag flag = FLAG_MAPPING.get(o);
                if (flag != null) {
                    result.add(flag);
                }
            }
        }

        return Collections.unmodifiableSet(result);
    }

    private static long getLongFromIterator(Iterator<?> iterator, long defaultValue) {
        if (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof String) {
                return Long.parseLong((String) object);
            }

            if (object instanceof Number) {
                return ((Number) object).longValue();
            }
        }
        return defaultValue;
    }

}
