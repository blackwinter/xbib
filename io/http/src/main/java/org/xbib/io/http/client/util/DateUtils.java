package org.xbib.io.http.client.util;

public final class DateUtils {

    private DateUtils() {
    }

    public static long millisTime() {
        return System.nanoTime() / 1000000;
    }
}
