package org.xbib.io.redis.output;

import org.xbib.io.redis.RedisCommandExecutionException;
import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link List} of command outputs, possibly deeply nested.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class NestedMultiOutput<K, V> extends CommandOutput<K, V, List<Object>> {
    private final Deque<List<Object>> stack;
    private int depth;

    public NestedMultiOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<Object>());
        stack = new LinkedList<List<Object>>();
        depth = 0;
    }

    @Override
    public void set(long integer) {
        output.add(integer);
    }

    @Override
    public void set(ByteBuffer bytes) {
        output.add(bytes == null ? null : codec.decodeValue(bytes));
    }

    @Override
    public void setError(ByteBuffer error) {
        output.add(new RedisCommandExecutionException(decodeAscii(error)));
    }

    @Override
    public void complete(int depth) {
        if (depth > 0 && depth < this.depth) {
            output = stack.pop();
            this.depth--;
        }
    }

    @Override
    public void multi(int count) {
        List<Object> a = new ArrayList<Object>(count);
        output.add(a);
        stack.push(output);
        output = a;
        this.depth++;
    }
}
