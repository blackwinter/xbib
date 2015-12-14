package org.xbib.io.http.client.handler.resumable;

import org.xbib.io.http.client.handler.resumable.ResumableAsyncHandler.ResumableProcessor;

import java.util.HashMap;
import java.util.Map;

public class MapResumableProcessor
        implements ResumableProcessor {

    Map<String, Long> map = new HashMap<>();

    public void put(String key, long transferredBytes) {
        map.put(key, transferredBytes);
    }

    public void remove(String key) {
        map.remove(key);
    }

    /**
     * NOOP
     */
    public void save(Map<String, Long> map) {

    }

    /**
     * NOOP
     */
    public Map<String, Long> load() {
        return map;
    }
}