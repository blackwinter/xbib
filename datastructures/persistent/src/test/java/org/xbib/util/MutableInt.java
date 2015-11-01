package org.xbib.util;

public class MutableInt extends Number implements Comparable<MutableInt>, Mutable<Number> {

    private int value;

    public MutableInt() {
        super();
    }

    public MutableInt(int value) {
        super();
        this.value = value;
    }

    public MutableInt(Number value) {
        super();
        this.value = value.intValue();
    }

    public MutableInt(String value) throws NumberFormatException {
        super();
        this.value = Integer.parseInt(value);
    }

    public Integer getValue() {
        return new Integer(this.value);
    }

    public void setValue(Number value) {
        this.value = value.intValue();
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public void add(int operand) {
        this.value += operand;
    }

    public void add(Number operand) {
        this.value += operand.intValue();
    }

    public void subtract(int operand) {
        this.value -= operand;
    }

    public void subtract(Number operand) {
        this.value -= operand.intValue();
    }

    @Override
    public int intValue() {
        return value;
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

    public Integer toInteger() {
        return Integer.valueOf(intValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MutableInt) {
            return value == ((MutableInt) obj).intValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value;
    }

    public int compareTo(MutableInt other) {
        int anotherVal = other.value;
        return value < anotherVal ? -1 : (value == anotherVal ? 0 : 1);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}