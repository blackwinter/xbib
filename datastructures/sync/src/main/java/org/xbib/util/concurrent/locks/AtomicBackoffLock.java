package org.xbib.util.concurrent.locks;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AtomicBackoffLock implements Lock {

    private final int _spinsBeforeYield;
    private final int _spinsBeforeSleep;
    private final long _initSleepTime;

    private final AtomicBoolean state;

    public AtomicBackoffLock() {
        this(128, 256, 1, false);
    }

    public AtomicBackoffLock(boolean lock) {
        this(128, 256, 1, lock);
    }

    public AtomicBackoffLock(int spinsInterval) {
        this(spinsInterval, spinsInterval << 1, 1, false);
    }

    public AtomicBackoffLock(int spinsInterval, boolean lock) {
        this(spinsInterval, spinsInterval << 1, 1, lock);
    }

    public AtomicBackoffLock(int spinsBeforeYield, int spinsBeforeSleep, long initSleepTime, boolean lock) {
        this._spinsBeforeSleep = spinsBeforeSleep;
        this._spinsBeforeYield = spinsBeforeYield;
        this._initSleepTime = initSleepTime;
        this.state = new AtomicBoolean(lock);
    }

    public boolean isLocked() {
        return state.get();
    }

    public void lock() {
        final int spinsBeforeYield = _spinsBeforeYield;
        final int spinsBeforeSleep = _spinsBeforeSleep;
        long sleepTime = _initSleepTime;
        int spins = 0;
        while (true) {
            if (!state.get()) { // test-and-test-and-set
                if (!state.getAndSet(true)) {
                    return;
                }
            }
            if (spins < spinsBeforeYield) { // spin phase
                ++spins;
            } else if (spins < spinsBeforeSleep) { // yield phase
                ++spins;
                Thread.yield();
            } else { // back-off phase
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                sleepTime = (3 * sleepTime) >> 1 + 1; // 50% is arbitrary
            }
        }
    }

    public void unlock() {
        state.set(false);
    }

}