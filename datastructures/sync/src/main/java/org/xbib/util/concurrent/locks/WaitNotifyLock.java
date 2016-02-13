package org.xbib.util.concurrent.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class WaitNotifyLock implements Lock {

    Thread owner;
    int lockCount;

    public synchronized void lock() {
        while (owner != null && owner != Thread.currentThread()) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        owner = Thread.currentThread();
        lockCount++;
    }

    public synchronized void unlock() {
        lockCount--;
        if (lockCount <= 0) {
            owner = null;
            notify();
        }
    }

    public Condition newCondition() {
        return new WaitNotifyCondition();
    }

    public void lockInterruptibly() {
    }

    public boolean tryLock() {
        return false;
    }

    public boolean tryLock(long time, java.util.concurrent.TimeUnit unit) {
        return false;
    }

    private class WaitNotifyCondition implements Condition {

        public synchronized void await() throws InterruptedException {
            InterruptedException ex = null;
            unlock();
            try {
                wait();
            } catch (InterruptedException e) {
                ex = e;
            }
            lock();
            if (ex != null) {
                throw ex;
            }
        }

        public synchronized void signal() {
            notify();
        }

        public synchronized void signalAll() {
            notifyAll();
        }

        public boolean await(long time, java.util.concurrent.TimeUnit unit) {
            return false;
        }

        public long awaitNanos(long nanosTimeout) {
            return 0;
        }

        public void awaitUninterruptibly() {
        }

        public boolean awaitUntil(java.util.Date deadline) {
            return false;
        }

    }

}
