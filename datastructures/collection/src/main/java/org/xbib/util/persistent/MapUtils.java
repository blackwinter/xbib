package org.xbib.util.persistent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class MapUtils {

    @SuppressWarnings("rawtypes")
    private static final Function TO_MAP_ENTRY = input -> (Entry) input;

    @SuppressWarnings("rawtypes")
    private static final Function GET_KEY = input -> ((Entry) input).getKey();

    @SuppressWarnings("rawtypes")
    private static final Function GET_VALUE = input -> ((Entry) input).getValue();

    @SuppressWarnings("unchecked")
    public static <K, V> Function<? super Map.Entry<K, V>, Map.Entry<K, V>> mapEntryFunction() {
        return TO_MAP_ENTRY;
    }

    @SuppressWarnings("unchecked")
    public static <K> Function<Map.Entry<K, ?>, K> mapKeyFunction() {
        return GET_KEY;
    }

    @SuppressWarnings("unchecked")
    public static <V> Function<Map.Entry<?, V>, V> mapValueFunction() {
        return GET_VALUE;
    }

}