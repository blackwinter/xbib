package org.xbib.util.concurrent.sync;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LockManager {

    static final int LOCKED_SERVER = 0x1000;
    static final int TIMED_WAIT = 0x2000;
    private final HashMap<Thread,Session> sessions = new HashMap<>();
    private final HashMap<Integer,LockImpl> locks = new HashMap<>();
    private final HashMap<String,LockImpl> locksByName = new HashMap<>();
    final CriticalSection criticalSection = new CriticalSection();
    int lastLockId;

    public synchronized LockImpl getLock(int oid) {
        return locks.get(oid);
    }

    public synchronized LockImpl createLock(Session session, String name) {
        LockImpl lock = locksByName.get(name);
        if (lock != null) {
            session.exists = true;
        } else {
            lock = new LockImpl(this, name);
            locksByName.put(name, lock);
            session.exists = false;
        }
        session.beginAccess(lock);
        return lock;
    }

    public synchronized LockImpl openLock(Session session, String name) {
        LockImpl lock = locksByName.get(name);
        if (lock != null) {
            session.beginAccess(lock);
        }
        return lock;
    }

    public void deleteLock(LockImpl lock) {
        locksByName.remove(lock.getName());
        locks.remove(lock.id);
    }

    protected void assignId(LockImpl lock) {
        lock.id = ++lastLockId;
        locks.put(lock.id, lock);
    }

    protected Session getSession() {
        Thread t = Thread.currentThread();
        Session s = sessions.get(t);
        if (s == null) {
            s = new Session();
            sessions.put(t, s);
        }
        return s;
    }

    protected WaitObject waitNotification(LockImpl lock, int flags, int rank) throws InterruptedException {
        Session session = getSession();
        WaitObject wob = new WaitObject(session, lock, flags, rank);
        lock.addWaitObject(wob);
        session.waitObject = wob;
        if ((flags & LOCKED_SERVER) != 0) {
            criticalSection.leave();
        }
        wob.waitNotification();
        return wob;
    }

    protected WaitObject waitNotificationWithTimeout(LockImpl lock, long timeout, int flags, int rank) throws InterruptedException {
        Session session = getSession();
        WaitObject wob = new WaitObject(session, lock, flags, rank);
        if (timeout != 0) {
            criticalSection.enter();
            wob.flags |= TIMED_WAIT;
            lock.addWaitObject(wob);
            session.waitObject = wob;
            criticalSection.leave();
            wob.waitNotificationWithTimeout(timeout);
            if (!wob.signaled) {
                criticalSection.enter();
                wob.session.waitObject = null;
                wob.unlink();
                criticalSection.leave();
            }
        }
        return wob;
    }


    class Session {

        WaitObject waitObject;
        LockObject lockObject;
        Set<LockImpl> lockHashSet = new HashSet<>();
        boolean exists;

        public synchronized void close() {
            while (lockObject != null) {
                try {
                    lockObject.unlock();
                } catch (NotOwnerException e) {
                    //
                } catch (InterruptedException e) {
                    //
                }
                lockObject = lockObject.nextLock;
            }
            for (LockImpl lock : lockHashSet) {
                lock.endAccess();
            }
            lockHashSet = null;
        }

        protected void beginAccess(LockImpl lock) {
            lock.beginAccess();
            lockHashSet.add(lock);
        }

        protected void endAccess(LockImpl lock) throws IOException {
            lock.endAccess();
            lockHashSet.remove(lock);
        }

        protected synchronized LockObject addLock(LockImpl lock, int flags) {
            LockObject lob = new LockObject(lock, this, flags);
            lob.nextLock = lockObject;
            lockObject = lob;
            return lob;
        }

        protected synchronized void removeLock(LockObject lock) {
            for (LockObject lob = lockObject, prev = null;
                 lob != null;
                 prev = lob, lob = lob.nextLock) {
                if (lob == lock) {
                    if (prev == null) {
                        lockObject = lob.nextLock;
                    } else {
                        prev.nextLock = lob.nextLock;
                    }
                    break;
                }
            }
        }
    }


    class LockObject {
        LockObject nextLock;
        LockObject nextOwner;
        int flags;
        LockImpl lock;
        Session owner;

        LockObject(LockImpl lock, Session session, int flags) {
            this.lock = lock;
            this.owner = session;
            this.flags = flags;
        }

        void unlock() throws NotOwnerException, InterruptedException {
            lock.unlock(owner);
        }
    }

}