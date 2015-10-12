package org.xbib.io.redis;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.output.IntegerOutput;
import org.xbib.io.redis.output.ListOfMapsOutput;
import org.xbib.io.redis.output.MapOutput;
import org.xbib.io.redis.output.StatusOutput;
import org.xbib.io.redis.output.ValueListOutput;
import org.xbib.io.redis.protocol.Command;
import org.xbib.io.redis.protocol.CommandArgs;
import org.xbib.io.redis.protocol.CommandKeyword;

import java.util.List;
import java.util.Map;

import static org.xbib.io.redis.protocol.CommandKeyword.FAILOVER;
import static org.xbib.io.redis.protocol.CommandKeyword.RESET;
import static org.xbib.io.redis.protocol.CommandKeyword.SLAVES;
import static org.xbib.io.redis.protocol.CommandType.MONITOR;
import static org.xbib.io.redis.protocol.CommandType.PING;
import static org.xbib.io.redis.protocol.CommandType.SENTINEL;
import static org.xbib.io.redis.protocol.CommandType.SET;

/**
 */
class SentinelCommandBuilder<K, V> extends BaseRedisCommandBuilder<K, V> {
    public SentinelCommandBuilder(RedisCodec<K, V> codec) {
        super(codec);
    }

    public Command<K, V, List<V>> getMasterAddrByKey(K key) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add("get-master-addr-by-name").addKey(key);
        return createCommand(SENTINEL, new ValueListOutput<K, V>(codec), args);
    }

    public Command<K, V, List<Map<K, V>>> masters() {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add("masters");
        return createCommand(SENTINEL, new ListOfMapsOutput<K, V>(codec), args);
    }

    public Command<K, V, Map<K, V>> master(K key) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add("master").addKey(key);
        return createCommand(SENTINEL, new MapOutput<K, V>(codec), args);
    }

    public Command<K, V, List<Map<K, V>>> slaves(K key) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add(SLAVES).addKey(key);
        return createCommand(SENTINEL, new ListOfMapsOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> reset(K key) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add(RESET).addKey(key);
        return createCommand(SENTINEL, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, String> failover(K key) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add(FAILOVER).addKey(key);
        return createCommand(SENTINEL, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> monitor(K key, String ip, int port, int quorum) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add(MONITOR).addKey(key).add(ip).add(port).add(quorum);
        return createCommand(SENTINEL, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> set(K key, String option, V value) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add(SET).addKey(key).add(option).addValue(value);
        return createCommand(SENTINEL, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> ping() {
        return createCommand(PING, new StatusOutput<K, V>(codec));
    }

    public Command<K, V, String> remove(K key) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add(CommandKeyword.REMOVE).addKey(key);
        return createCommand(SENTINEL, new StatusOutput<K, V>(codec), args);
    }
}
