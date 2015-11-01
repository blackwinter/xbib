package org.xbib.util;

public class MutableLong extends Number implements Comparable<MutableLong>, Mutable<Number> {
    private long value;

    public MutableLong() {
        super();
    }

    public MutableLong(long value) {
        super();
        this.value = value;
    }

    public MutableLong(Number value) {
        super();
        this.value = value.longValue();
    }

    public MutableLong(String value) throws NumberFormatException {
        super();
        this.value = Long.parseLong(value);
    }

    public Long getValue() {
        return new Long(this.value);
    }

    public void setValue(Number value) {
        this.value = value.longValue();
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public void add(long operand) {
        this.value += operand;
    }

    public void add(Number operand) {
        this.value += operand.longValue();
    }

    public void subtract(long operand) {
        this.value -= operand;
    }

    public void subtract(Number operand) {
        this.value -= operand.longValue();
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public Long toLong() {
        return Long.valueOf(longValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MutableLong) {
            return value == ((MutableLong) obj).longValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    public int compareTo(MutableLong other) {
        long anotherVal = other.value;
        return value < anotherVal ? -1 : (value == anotherVal ? 0 : 1);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}