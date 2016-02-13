package org.xbib.util.concurrent.locks;

class ReentrantLock {
    private final Object lock = new Object();
    private Thread thread = null;
    private int lockCount = 0;

    public void lock() throws InterruptedException {
        synchronized (lock) {
            Thread callingThread = Thread.currentThread();
            while (thread != null && thread != callingThread) {
                wait();
            }
            thread = callingThread;
            lockCount++;
        }
    }

    public void unlock() {
        synchronized (lock) {
            if (Thread.currentThread() == thread) {
                if (--lockCount == 0) {
                    thread = null;
                    notify();
                }
            }
        }
    }
}
