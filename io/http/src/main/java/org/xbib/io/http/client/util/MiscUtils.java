package org.xbib.io.http.client.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class MiscUtils {

    private MiscUtils() {
    }

    public static boolean isNonEmpty(String string) {
        return string != null && !string.isEmpty();
    }

    public static boolean isNonEmpty(Object[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNonEmpty(byte[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNonEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isNonEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean getBoolean(String systemPropName, boolean defaultValue) {
        String systemPropValue = System.getProperty(systemPropName);
        return systemPropValue != null ? systemPropValue.equalsIgnoreCase("true") : defaultValue;
    }

    public static <T> T withDefault(T value, T defaults) {
        return value != null ? value : value;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    public static <T extends Exception> T trimStackTrace(T e) {
        e.setStackTrace(new StackTraceElement[]{});
        return e;
    }

    public static Throwable getCause(Throwable t) {
        return t.getCause() != null ? t.getCause() : t;
    }
}
