package org.xbib.util.concurrent.sync;

import java.io.IOException;

class LockImpl implements Lock {
    static final int EXCLUSIVE_LOCK = 1;
    static final int SHARED_LOCK = 2;
    private final LockManager lockManager;
    private final String name;
    final CriticalSection criticalSection;
    private final WaitObject waitObject;
    LockManager.LockObject writer;
    LockManager.LockObject readers;
    int accessCount;
    int id;

    LockImpl(LockManager lockManager, String name) {
        this.lockManager = lockManager;
        this.name = name;
        this.waitObject = new WaitObject();
        this.criticalSection = new CriticalSection();
        lockManager.assignId(this);
    }

    public void exclusiveLock() throws InterruptedException {
        priorityExclusiveLock(DEFAULT_RANK);
    }

    public void priorityExclusiveLock(int rank) throws InterruptedException {
        LockManager.Session session = lockManager.getSession();
        criticalSection.enter();
        if (writer == null || writer.owner != session) {
            if (writer == null
                    && (readers == null || readers.owner == session && readers.nextOwner == null)) {
                if (readers != null) {
                    readers.flags = EXCLUSIVE_LOCK;
                    writer = readers;
                    readers = null;
                } else {
                    writer = session.addLock(this, EXCLUSIVE_LOCK);
                }
            } else {
                WaitObject wob;
                lockManager.criticalSection.enter();
                if (writer != null) {
                    if ((wob = writer.owner.waitObject) != null) {
                        if (wob.detectDeadlock(session)) {
                            lockManager.criticalSection.leave();
                            criticalSection.leave();
                            throw new DeadlockException();
                        }
                    }
                } else {
                    for (LockManager.LockObject lob = readers; lob != null; lob = lob.nextOwner) {
                        if (lob.owner == session) {
                            WaitObject head = waitObject;
                            wob = head;
                            while ((wob = wob.next) != head) {
                                if ((wob.flags & EXCLUSIVE_LOCK) != 0) {
                                    lockManager.criticalSection.leave();
                                    criticalSection.leave();
                                    throw new DeadlockException();
                                }
                            }
                        } else if ((wob = lob.owner.waitObject) != null) {
                            if (wob.detectDeadlock(session)) {
                                lockManager.criticalSection.leave();
                                criticalSection.leave();
                                throw new DeadlockException();
                            }
                        }
                    }
                }
                lockManager.waitNotification(this, LockManager.LOCKED_SERVER | EXCLUSIVE_LOCK, rank);
            }
        }
        criticalSection.leave();
    }

    public boolean exclusiveLock(long timeout) throws InterruptedException {
        return priorityExclusiveLock(DEFAULT_RANK, timeout);
    }

    public boolean priorityExclusiveLock(int rank, long timeout) throws InterruptedException {
        LockManager.Session session = lockManager.getSession();
        boolean result = true;
        criticalSection.enter();
        if (writer == null || writer.owner != session) {
            if (writer == null
                    && (readers == null || readers.owner == session && readers.nextOwner == null)) {
                if (readers != null) {
                    readers.flags = EXCLUSIVE_LOCK;
                    writer = readers;
                    readers = null;
                } else {
                    writer = session.addLock(this, EXCLUSIVE_LOCK);
                }
            } else {
                WaitObject wob = lockManager.waitNotificationWithTimeout(this, timeout, EXCLUSIVE_LOCK, rank);
                if (!wob.signaled) {
                    result = false;
                }
            }
        }
        criticalSection.leave();
        return result;
    }

    public void sharedLock() throws InterruptedException {
        prioritySharedLock(DEFAULT_RANK);
    }

    public void prioritySharedLock(int rank) throws InterruptedException {
        LockManager.Session session = lockManager.getSession();
        criticalSection.enter();
        if (writer == null || writer.owner != session) {
            if (writer == null) {
                LockManager.LockObject lob;
                for (lob = readers; lob != null && lob.owner != session; lob = lob.nextOwner) {
                    ;
                }
                if (lob == null) {
                    lob = session.addLock(this, SHARED_LOCK);
                    lockManager.criticalSection.enter();
                    lob.nextOwner = readers;
                    readers = lob;
                    lockManager.criticalSection.leave();
                }
            } else {
                WaitObject wob;
                lockManager.criticalSection.enter();
                if ((wob = writer.owner.waitObject) != null) {
                    if (wob.detectDeadlock(session)) {
                        lockManager.criticalSection.leave();
                        criticalSection.leave();
                        throw new DeadlockException();
                    }
                }
                lockManager.waitNotification(this, LockManager.LOCKED_SERVER | SHARED_LOCK, rank);
            }
        }
        criticalSection.leave();
    }

    public boolean sharedLock(long timeout) throws InterruptedException {
        return prioritySharedLock(DEFAULT_RANK, timeout);
    }

    public boolean prioritySharedLock(int rank, long timeout) throws InterruptedException {
        LockManager.Session session = lockManager.getSession();
        boolean result = true;
        criticalSection.enter();
        if (writer == null || writer.owner != session) {
            if (writer == null) {
                LockManager.LockObject lob;
                for (lob = readers; lob != null && lob.owner != session; lob = lob.nextOwner) {
                    ;
                }
                if (lob == null) {
                    lob = session.addLock(this, SHARED_LOCK);
                    lockManager.criticalSection.enter();
                    lob.nextOwner = readers;
                    readers = lob;
                    lockManager.criticalSection.leave();
                }
            } else {
                WaitObject wob = lockManager.waitNotificationWithTimeout(this, timeout, SHARED_LOCK, rank);
                if (!wob.signaled) {
                    result = false;
                }
            }
        }
        criticalSection.leave();
        return result;
    }

    public void unlock() throws NotOwnerException, InterruptedException {
        unlock(lockManager.getSession());
    }

    public void reset() throws InterruptedException {
        criticalSection.enter();
        if (writer != null) {
            writer.owner.removeLock(writer);
            writer = null;
        } else {
            for (LockManager.LockObject lob = readers; lob != null; lob = lob.nextOwner) {
                lob.owner.removeLock(lob);
            }
            readers = null;
        }
        retry();
        criticalSection.leave();
    }

    public void priorityWait(int rank) throws InterruptedException {
        priorityExclusiveLock(rank);
    }

    public boolean priorityWait(int rank, long timeout) throws InterruptedException {
        return priorityExclusiveLock(rank, timeout);
    }

    public String getName() {
        return name;
    }

    public boolean alreadyExists() {
        return lockManager.getSession().exists;
    }

    public void close() throws IOException {
        lockManager.getSession().endAccess(this);
    }

    public boolean waitFor(long timeout) throws InterruptedException {
        return priorityWait(DEFAULT_RANK, timeout);
    }

    public void waitFor() throws InterruptedException {
        priorityWait(DEFAULT_RANK);
    }

    protected void unlock(LockManager.Session session) throws NotOwnerException, InterruptedException {
        criticalSection.enter();
        if (writer != null) {
            if (writer.owner != session) {
                criticalSection.leave();
                throw new NotOwnerException();
            }
            session.removeLock(writer);
            writer = null;
        } else {
            LockManager.LockObject lob;
            LockManager.LockObject prev;
            for (lob = readers, prev = null; lob != null && lob.owner != session; prev = lob, lob = lob.nextOwner) {
                ;
            }
            if (lob == null) {
                criticalSection.leave();
                throw new NotOwnerException();
            }
            if (prev == null) {
                readers = lob.nextOwner;
            } else {
                prev.nextOwner = lob.nextOwner;
            }
            session.removeLock(lob);
        }
        retry();
        criticalSection.leave();
    }

    protected void endAccess() {
        synchronized (lockManager) {
            if (--accessCount == 0) {
                lockManager.deleteLock(this);
            }
        }
    }

    protected void beginAccess() {
        accessCount += 1;
    }

    protected void retry() throws InterruptedException {
        WaitObject head = waitObject;
        WaitObject wob = head;
        while ((wob = head.next) != head
                && ((wob.flags & EXCLUSIVE_LOCK) == 0 || readers == null
                || (readers.owner == wob.session && readers.nextOwner == null))) {
            wob.sendNotification();
            wob.unlink();
            if ((wob.flags & EXCLUSIVE_LOCK) == 0) {
                LockManager.LockObject lob = wob.session.addLock(this, SHARED_LOCK);
                lockManager.criticalSection.enter();
                lob.nextOwner = readers;
                readers = lob;
                lockManager.criticalSection.leave();
            } else {
                if (readers != null) {
                    readers.flags = EXCLUSIVE_LOCK;
                    writer = readers;
                    readers = null;
                } else {
                    writer = wob.session.addLock(this, EXCLUSIVE_LOCK);
                }
                break;
            }
        }
    }

    protected void addWaitObject(WaitObject wob) {
        WaitObject head = waitObject;
        WaitObject last = head;
        while ((last = last.prev) != head && last.rank > wob.rank) {
            ;
        }
        wob.linkAfter(last);
    }
}




