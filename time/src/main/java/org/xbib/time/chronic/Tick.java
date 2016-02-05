package org.xbib.time.chronic;

public class Tick {
    private int time;
    private boolean ambiguous;

    public Tick(int time, boolean ambiguous) {
        this.time = time;
        this.ambiguous = ambiguous;
    }

    public boolean isAmbiguous() {
        return ambiguous;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public Tick times(int other) {
        return new Tick(time * other, ambiguous);
    }

    public int intValue() {
        return time;
    }

    public float floatValue() {
        return time;
    }

    @Override
    public String toString() {
        return time + (ambiguous ? "?" : "");
    }
}
