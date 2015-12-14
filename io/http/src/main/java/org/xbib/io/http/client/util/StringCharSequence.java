package org.xbib.io.http.client.util;

/**
 * A CharSequence String wrapper that doesn't copy the char[] (damn new String implementation!!!)
 */
public class StringCharSequence implements CharSequence {

    public final int length;
    private final String value;
    private final int offset;

    public StringCharSequence(String value, int offset, int length) {
        this.value = value;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return value.charAt(offset + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        int offsetedEnd = offset + end;
        if (offsetedEnd < length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return new StringCharSequence(value, offset + start, end - start);
    }

    @Override
    public String toString() {
        return value.substring(offset, length);
    }
}
