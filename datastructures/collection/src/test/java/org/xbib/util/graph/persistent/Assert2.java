package org.xbib.util.graph.persistent;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.Objects;

class Assert2 {

    public static <T> void assertSize(int expected, Iterable<T> actual, String message) {
        final int actualSize = size(actual);
        if(actualSize != expected) {
            fail(formatSize(message, expected, actualSize));
        }
    }

    public static <T> void assertSize(int expected, T[] actual, String message) {
        final int actualSize = actual.length;
        if(actualSize != expected) {
            fail(formatSize(message, expected, actualSize));
        }
    }

    public static <T> void assertSize(int expected, Iterable<T> actual) {
        assertSize(expected, actual, null);
    }

    public static <T> void assertSize(int expected, T[] actual) {
        assertSize(expected, actual, null);
    }

    private static String formatSize(String message, int expected, int actual) {
        final String formatted = preformat(message);
        return formatted + "expected size: " + expected + " but was: " + actual;
    }


    public static <T> void assertEmpty(Iterable<T> actual, String message) {
        final int actualSize = size(actual);
        if(actualSize != 0) {
            fail(formatEmpty(message, actualSize));
        }
    }

    public static <T> void assertEmpty(T[] actual, String message) {
        final int actualSize = actual.length;
        if(actualSize != 0) {
            fail(formatEmpty(message, actualSize));
        }
    }

    public static <T> void assertEmpty(Iterable<T> actual) {
        assertEmpty(actual, null);
    }

    public static <T> void assertEmpty(T[] actual) {
        assertEmpty(actual, null);
    }

    private static String formatEmpty(String message, int actual) {
        final String formatted = preformat(message);
        return formatted + "expected empty, but size was: " + actual;
    }

    private static int size(Iterable<?> iterable) {
        final Iterator<?> iterator = iterable.iterator();
        int count = 0;
        while(iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    public static <T> void assertIterableEquals(Iterable<T> expected, Iterable<T> actual, String message) {
        if(!elementsEqual(expected, actual)) {
            fail(formatEquals(message, expected, actual));
        }
    }

    private static <T> String formatEquals(String message, T expected, T actual) {
        final String formatted = preformat(message);
        final String expectedString = String.valueOf(expected);
        final String actualString = String.valueOf(actual);
        return formatted + "expected: " + formatClassAndValue(expected, expectedString) + " but was: "
            + formatClassAndValue(actual, actualString);
    }

    private static boolean elementsEqual(Iterable<?> iterable1, Iterable<?> iterable2) {
        final Iterator<?> iterator1 = iterable1.iterator();
        final Iterator<?> iterator2 = iterable2.iterator();
        while(iterator1.hasNext()) {
            if(!iterator2.hasNext()) {
                return false;
            }
            final Object o1 = iterator1.next();
            final Object o2 = iterator2.next();
            if(!Objects.equals(o1, o2)) {
                return false;
            }
        }
        return !iterator2.hasNext();
    }

    public static <T> void assertContains(T expected, Iterable<? extends T> actual, String message) {
        if(!contains(actual, expected)) {
            fail(formatContains(message, expected, actual));
        }
    }

    public static <T> void assertContains(T expected, Iterable<? extends T> actual) {
        assertContains(expected, actual, null);
    }

    private static <T> String formatContains(String message, T expected, Iterable<? extends T> actual) {
        final String formatted = preformat(message);
        final String expectedString = String.valueOf(expected);
        final String actualString = String.valueOf(actual);
        return formatted + "expected: " + formatClassAndValue(expected, expectedString) + " contained in: "
            + formatClassAndValue(actual, actualString);
    }


    public static <T> void assertNotContains(T expected, Iterable<? extends T> actual, String message) {
        if(contains(actual, expected)) {
            fail(formatNotContains(message, expected, actual));
        }
    }

    public static <T> void assertNotContains(T expected, Iterable<? extends T> actual) {
        assertNotContains(expected, actual, null);
    }

    private static <T> String formatNotContains(String message, T expected, Iterable<? extends T> actual) {
        final String formatted = preformat(message);
        final String expectedString = String.valueOf(expected);
        final String actualString = String.valueOf(actual);
        return formatted + "expected: " + formatClassAndValue(expected, expectedString) + " not contained in: "
            + formatClassAndValue(actual, actualString);
    }

    private static <T> boolean contains(Iterable<? extends T> iterable, T element) {
        for(T obj : iterable) {
            if(Objects.equals(obj, element)) {
                return true;
            }
        }
        return false;
    }

    private static String preformat(String message) {
        String formatted = "";
        if(message != null && !message.equals("")) {
            formatted = message + " ";
        }
        return formatted;
    }

    private static String formatClassAndValue(Object value, String valueString) {
        final String className = value == null ? "null" : value.getClass().getName();
        return className + "<" + valueString + ">";
    }
}
