package org.xbib.time.chronic;

import org.xbib.time.chronic.tags.Pointer;

import java.util.Calendar;

public class Options {
    private Pointer.PointerType context;
    private Calendar now;
    private boolean guess;
    private int ambiguousTimeRange;
    private boolean compatibilityMode;

    public Options() {
        this(Pointer.PointerType.FUTURE, Calendar.getInstance(), true, 6);
    }

    public Options(Calendar now) {
        this(Pointer.PointerType.FUTURE, now, true, 6);
    }

    public Options(Calendar now, boolean guess) {
        this(Pointer.PointerType.FUTURE, now, guess, 6);
    }

    public Options(Pointer.PointerType context) {
        this(context, Calendar.getInstance(), true, 6);
    }

    public Options(boolean guess) {
        this(Pointer.PointerType.FUTURE, Calendar.getInstance(), guess, 6);
    }

    public Options(int ambiguousTimeRange) {
        this(Pointer.PointerType.FUTURE, Calendar.getInstance(), true, ambiguousTimeRange);
    }

    public Options(Pointer.PointerType context, Calendar now, boolean guess, int ambiguousTimeRange) {
        this.context = context;
        this.now = now;
        this.guess = guess;
        this.ambiguousTimeRange = ambiguousTimeRange;
    }

    public boolean isCompatibilityMode() {
        return compatibilityMode;
    }

    public void setCompatibilityMode(boolean compatibilityMode) {
        this.compatibilityMode = compatibilityMode;
    }

    public Pointer.PointerType getContext() {
        return context;
    }

    public void setContext(Pointer.PointerType context) {
        this.context = context;
    }

    public Calendar getNow() {
        return now;
    }

    public void setNow(Calendar now) {
        this.now = now;
    }

    public boolean isGuess() {
        return guess;
    }

    public void setGuess(boolean guess) {
        this.guess = guess;
    }

    public int getAmbiguousTimeRange() {
        return ambiguousTimeRange;
    }

    public void setAmbiguousTimeRange(int ambiguousTimeRange) {
        this.ambiguousTimeRange = ambiguousTimeRange;
    }
}
