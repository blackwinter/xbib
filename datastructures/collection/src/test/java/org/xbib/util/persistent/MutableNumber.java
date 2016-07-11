package org.xbib.util.persistent;

public class MutableNumber extends Number implements Comparable<MutableNumber>, Mutable<Number> {

    private int value;

    public MutableNumber() {
        super();
    }

    public MutableNumber(int value) {
        super();
        this.value = value;
    }

    public MutableNumber(Number value) {
        super();
        this.value = value.intValue();
    }

    public MutableNumber(String value) throws NumberFormatException {
        super();
        this.value = Integer.parseInt(value);
    }

    public Number getValue() {
        return this.value;
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
        if (obj instanceof MutableNumber) {
            return value == ((MutableNumber) obj).intValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value;
    }

    public int compareTo(MutableNumber other) {
        int anotherVal = other.value;
        return value < anotherVal ? -1 : (value == anotherVal ? 0 : 1);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}