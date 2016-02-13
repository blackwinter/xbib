package org.xbib.util.concurrent.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A fair fast scalable reader-writer lock.
 * O. Krieger, M. Stumm, R. Unrau, J. Hanna, "A fair fast scalable reader-writer lock,"
 * In Proc. International Conference on Parallel Processing  (CRC Press), vol. II - Software, pp. II-201-II-204, 1993.
 */
public final class FairReadWriteLock implements ReadWriteLock {

    private final ThreadLocal<QNode> myNode;
    private final AtomicReference<QNode> tail;

    private final java.util.concurrent.locks.Lock readLock; // readers apply here
    private final java.util.concurrent.locks.Lock writeLock; // writers apply here

    public FairReadWriteLock() {
        this.myNode = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };
        this.tail = new AtomicReference<QNode>(null);
        this.readLock = new ReadLock(this);
        this.writeLock = new WriteLock(this);
    }

    public java.util.concurrent.locks.Lock readLock() {
        return readLock;
    }

    public java.util.concurrent.locks.Lock writeLock() {
        return writeLock;
    }

    public ReadWriteSpinLockAdapter asSpinLock() {
        return new ReadWriteSpinLockAdapter(this);
    }

    private enum State {
        reader, writer, active_reader
    }

    private static final class QNode {
        final Lock el; // a spin lock
        State state;
        boolean locked; // a local spin variable
        QNode next, prev; // neighbor pointers

        QNode() {
            this.el = new AtomicBackoffLock();
        }
    }

    private static final class ReadWriteSpinLockAdapter {

        private final FairReadWriteLock delegate;

        ReadWriteSpinLockAdapter(FairReadWriteLock lock) {
            this.delegate = lock;
        }

        public void readLock() {
            delegate.readLock().lock();
        }

        public void readUnlock() {
            delegate.readLock().unlock();
        }

        public void writeLock() {
            delegate.writeLock().lock();
        }

        public void writeUnlock() {
            delegate.writeLock().unlock();
        }

    }

    private static final class WriteLock implements java.util.concurrent.locks.Lock {

        private final FairReadWriteLock rwlock;

        WriteLock(FairReadWriteLock rwlock) {
            this.rwlock = rwlock;
        }

        public void lock() {
            QNode i = rwlock.myNode.get();
            i.state = State.writer;
            i.locked = true;
            i.next = null;
            QNode pred = rwlock.tail.getAndSet(i);
            if (pred != null) {
                pred.next = i;
                // wait until predecessor gives up the lock
                while (pred.locked) {
                    ;
                }
            }
        }

        public void unlock() {
            QNode i = rwlock.myNode.get();
            if (i.next == null) {
                if (rwlock.tail.compareAndSet(i, null)) {
                    return;
                }
                // wait until predecessor fills in its next fields
                while (i.next == null) {
                    ;
                }
            }
            i.next.prev = null;
            i.next.locked = false;
        }

        public boolean isLocked() {
            throw new UnsupportedOperationException();
        }

        public void lockInterruptibly() throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        public boolean tryLock() {
            throw new UnsupportedOperationException();
        }

        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ReadLock implements java.util.concurrent.locks.Lock {

        private final FairReadWriteLock rwlock;

        ReadLock(FairReadWriteLock rwlock) {
            this.rwlock = rwlock;
        }

        public void lock() {
            QNode i = rwlock.myNode.get();
            i.state = State.reader;
            i.locked = true;
            i.next = i.prev = null;
            QNode pred = rwlock.tail.getAndSet(i);
            if (pred != null) {
                i.prev = pred;
                pred.next = i;
                if (pred.state != State.active_reader) {
                    // wait until predecessor gives up the lock
                    while (pred.locked) {
                        ;
                    }
                }
            }
            if (i.next != null && i.next.state == State.reader) {
                i.next.locked = false;
            }
            i.state = State.active_reader;
        }

        public void unlock() {
            QNode i = rwlock.myNode.get();
            QNode prev = i.prev;
            if (prev != null) {
                prev.el.lock();
                while (prev != i.prev) {
                    prev.el.unlock();
                    prev = i.prev;
                    if (prev == null) {
                        break;
                    }
                    prev.el.lock();
                }
                if (prev != null) {
                    i.el.lock();
                    prev.next = null;
                    if (i.next == null) {
                        if (!rwlock.tail.compareAndSet(i, i.prev)) {
                            while (i.next == null) {
                                ;
                            }
                        }
                    }
                    if (i.next != null) {
                        i.next.prev = i.prev;
                        i.prev.next = i.next;
                    }
                    i.el.unlock();
                    prev.el.unlock();
                    return;
                }
            }
            i.el.lock();
            if (i.next == null) {
                if (!rwlock.tail.compareAndSet(i, null)) {
                    while (i.next == null) {
                        ;
                    }
                }
            }
            if (i.next != null) {
                i.next.locked = false;
                i.prev.prev = null;
            }
            i.el.unlock();
        }

        public void lockInterruptibly() throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        public boolean tryLock() {
            throw new UnsupportedOperationException();
        }

        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }
    }
}