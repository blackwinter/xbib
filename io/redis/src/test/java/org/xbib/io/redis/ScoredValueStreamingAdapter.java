package org.xbib.io.redis;

import org.xbib.io.redis.output.ScoredValueStreamingChannel;

import java.util.ArrayList;
import java.util.List;

public class ScoredValueStreamingAdapter<T> implements ScoredValueStreamingChannel<T> {
    private List<ScoredValue<T>> list = new ArrayList<>();

    @Override
    public void onValue(ScoredValue<T> value) {
        list.add(value);
    }

    public List<ScoredValue<T>> getList() {
        return list;
    }
}
