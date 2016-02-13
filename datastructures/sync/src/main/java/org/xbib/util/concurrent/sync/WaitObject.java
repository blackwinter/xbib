package org.xbib.util.concurrent.sync;

class WaitObject {
    WaitObject next;
    WaitObject prev;
    LockImpl lock;
    LockManager.Session session;
    int flags;
    int rank;
    boolean signaled;
    Object data;

    WaitObject(LockManager.Session session, LockImpl lock, int flags, int rank) {
        this.session = session;
        this.flags = flags;
        this.lock = lock;
        this.rank = rank;
    }

    WaitObject() {
        prune();
    }

    void sendNotification() {
        sendNotification(null);
    }

    synchronized void sendNotification(Object data) {
        session.waitObject = null;
        this.data = data;
        signaled = true;
        notify();
    }

    void waitNotification() throws InterruptedException {
        synchronized (this) {
            lock.criticalSection.leave();
            wait();
        }
        lock.criticalSection.enter();
    }

    void waitNotificationWithTimeout(long timeout) throws InterruptedException {
        synchronized (this) {
            lock.criticalSection.leave();
            wait(timeout);
        }
        lock.criticalSection.enter();
    }

    boolean detectDeadlock(LockManager.Session session) {
        return ((flags & LockManager.TIMED_WAIT) == 0) &&
                detectDeadlock(session, lock.writer, lock.readers);
    }

    boolean detectDeadlock(LockManager.Session session,
                                     LockManager.LockObject writer, LockManager.LockObject readers) {
        if (writer != null) {
            if (writer.owner == session) {
                return true;
            } else if (writer.owner.waitObject != null) {
                return writer.owner.waitObject.detectDeadlock(session);
            }
        } else {
            WaitObject rwob;
            for (LockManager.LockObject reader = readers; reader != null; reader = reader.nextOwner) {
                if (reader.owner == session) {
                    return true;
                } else if (reader.owner != this.session && (rwob = reader.owner.waitObject) != null) {
                    if (rwob.detectDeadlock(session)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    final void linkAfter(WaitObject after) {
        prev = after;
        next = after.next;
        next.prev = this;
        after.next = this;
    }

    final void unlink() {
        next.prev = prev;
        prev.next = next;
    }

    final void prune() {
        next = prev = this;
    }

}



