package org.xbib.io.redis;

import org.xbib.io.redis.output.KeyStreamingChannel;
import org.xbib.io.redis.output.ScoredValueStreamingChannel;
import org.xbib.io.redis.output.ValueStreamingChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * Streaming adapter which stores every key or/and value in a list. This adapter can be used in KeyStreamingChannels and
 * ValueStreamingChannels.
 * 
 * @param <T> Value Type
 */
public class ListStreamingAdapter<T> implements KeyStreamingChannel<T>, ValueStreamingChannel<T>,
        ScoredValueStreamingChannel<T> {
    private final List<T> list = new ArrayList<T>();

    @Override
    public void onKey(T key) {
        list.add(key);

    }

    @Override
    public void onValue(T value) {
        list.add(value);
    }

    public List<T> getList() {
        return list;
    }

    @Override
    public void onValue(ScoredValue<T> value) {
        list.add(value.value);
    }
}
