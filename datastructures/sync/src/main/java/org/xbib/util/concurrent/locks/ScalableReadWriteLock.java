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
public final class ScalableReadWriteLock implements ReadWriteLock {

    private final ThreadLocal<QNode> myNode;
    private final AtomicReference<QNode> tail;

    private final java.util.concurrent.locks.Lock readLock; // readers apply here
    private final java.util.concurrent.locks.Lock writeLock; // writers apply here

    public ScalableReadWriteLock() {
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

    private enum State {
        reader, writer, active_reader
    }

    private static final class QNode {
        final Lock el; // a spin lock
        State state;
        boolean locked; // a local spin variable
        QNode next, prev; // neighbor pointers

        QNode() {
            next = prev = null;
            this.el = new AtomicBackoffLock();
        }
    }

    private static final class WriteLock implements java.util.concurrent.locks.Lock {

        private final ScalableReadWriteLock rwlock;

        WriteLock(ScalableReadWriteLock rwlock) {
            this.rwlock = rwlock;
        }

        public void lock() {
            QNode qnode = rwlock.myNode.get();
            qnode.state = State.writer;
            qnode.locked = true;
            qnode.next = null;
            QNode pred = rwlock.tail.getAndSet(qnode);
            if (pred != null) {
                pred.next = qnode;
                // wait until predessor gives up the lock
                while (pred.locked) {
                    ;
                }
            }
        }

        public void unlock() {
            QNode qnode = rwlock.myNode.get();
            if (qnode.next == null) {
                if (rwlock.tail.compareAndSet(qnode, null)) {
                    return;
                }
                // wait until predecessor fills in its next fields
                while (qnode.next == null) {
                    ;
                }
            }
            qnode.next.prev = null;
            qnode.next.locked = false;
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

        private final ScalableReadWriteLock rwlock;

        ReadLock(ScalableReadWriteLock rwlock) {
            this.rwlock = rwlock;
        }

        public void lock() {
            QNode qnode = rwlock.myNode.get();
            qnode.state = State.reader;
            qnode.locked = true;
            qnode.next = qnode.prev = null;
            QNode pred = rwlock.tail.getAndSet(qnode);
            if (pred != null) {
                qnode.prev = pred;
                pred.next = qnode;
                if (pred.state != State.active_reader) {
                    // wait until predessor gives up the lock
                    while (pred.locked) {
                        ;
                    }
                }
            }
            if (qnode.next != null && qnode.next.state == State.reader) {
                qnode.next.locked = false;
            }
            qnode.state = State.active_reader;
        }

        public void unlock() {
            QNode qnode = rwlock.myNode.get();
            QNode prev = qnode.prev;
            if (prev != null) {
                prev.el.lock();
                while (prev != qnode.prev) {
                    prev.el.unlock();
                    prev = qnode.prev;
                    if (prev == null) {
                        break;
                    }
                    prev.el.lock();
                }
                if (prev != null) {
                    qnode.el.lock();
                    prev.next = null;
                    if (qnode.next == null) {
                        if (!rwlock.tail.compareAndSet(qnode, qnode.prev)) {
                            while (qnode.next == null) {
                                ;
                            }
                        }
                    }
                    if (qnode.next != null) {
                        qnode.next.prev = qnode.prev;
                        qnode.prev.next = qnode.next;
                    }
                    qnode.el.unlock();
                    prev.el.unlock();
                    return;
                }
                prev.el.unlock();
            }
            qnode.el.lock();
            if (qnode.next == null) {
                if (!rwlock.tail.compareAndSet(qnode, null)) {
                    while (qnode.next == null) {
                        ;
                    }
                }
            }
            if (qnode.next != null) {
                qnode.next.locked = false;
                qnode.prev.prev = null;
            }
            qnode.el.unlock();
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
}