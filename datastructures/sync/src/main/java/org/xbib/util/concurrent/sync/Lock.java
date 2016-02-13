package org.xbib.util.concurrent.sync;

import java.io.IOException;

/**
 * Synchronization object for setting exlusive and shared locks.
 * Locks are not nested - if session sets the same lock several time,
 * all lock requests succeed but single unlock request will
 * unlock the object. It is possible to upgrade the lock:
 * if session first lock object in shared mode and then in exclusive
 * mode, lock will be upgraded to exclusive (<B>Attention!</B> upgrading
 * locks can easily cause a deadlock if several session will try to upgrade
 * their shared locks simultaneously).<BR>
 * It is not possible to downgrade the lock - locking of object in shared
 * mode after granted exlusive lock has no effect.<BR>
 * Locks are granted in FIFO order - it means that if object is locked
 * shared mode, then request for exclusive lock comes and is blocked and
 * then if yet another request for shared lock is received, it will
 * be also blocked and placed inqueue <B>after</B> excusive lock request.
 * Server is able to detect deadlock for this primitive.<P>
 * Semantic of methods inherited from <code>JIPCPrimitive</code>:
 * <DL>
 * <DT><code>waitFor</code><DD>set exlusive lock
 * <DT><code>reset</code><DD>removes all locks (shared or exclusive). It is not
 * required that session, invoking <code>reset</code> method be owner of this lock
 * </DL>
 */
public interface Lock {
    /**
     * Rank with which requsts will be queued if rank was not explicitely specified.
     */
    int DEFAULT_RANK = 0;

    /**
     * Get primitive name. Name of the primitive is unique with primitives
     * of the same type (events, semaphores,...). It is possible
     * that, for example, event and mutex has the same name.
     *
     * @return primitive name
     */
    String getName();

    /**
     * Primitive returned by <code>createXXX</code> method already exists
     * This method should be call immediatly after <code>createXXX</code>
     * to check if new primitive was created or existed one was returned.
     *
     * @return <code>true</code> if <code>createXXX</code> method doesn't
     * create new primitive
     */
    boolean alreadyExists();

    /**
     * Wait until state of primitive is switched. Semantic of this method
     * depends on particular primitive type and is explained in specification
     * of each primitive.
     */
    void waitFor() throws InterruptedException;

    /**
     * Wait until state of primitive is switched with timeout.
     * Semantic of this method depends on particular primitive type and is
     * explained in specification of each primitive.
     *
     * @param timeout operation timeout in millisoconds
     * @return <code>false</code> if timeout is expired before primitive
     * state is changed
     */
    boolean waitFor(long timeout) throws InterruptedException;

    /**
     * Priority wait until state of primitive is switched.
     * Requests with the lowest <code>rank</code> value will be satisfied first.
     * Semantic of this method depends on particular primitive type and is explained
     * in specification of each primitive.
     *
     * @param rank processes will be placed in wait queue in the order of increasing
     *             rank value and in the same order will be taken from the queue.
     */
    void priorityWait(int rank) throws InterruptedException;

    /**
     * Priority wait until state of primitive is switched with timeout.
     * Requests with the lowest <code>rank</code> value will be satisfied first.
     * Semantic of this method depends on particular primitive type and is
     * explained in specification of each primitive.
     *
     * @param rank    processes will be placed in wait queue in the order of increasing
     *                rank value and in the same order will be taken from the queue.
     * @param timeout operation timeout in millisoconds
     * @return <code>false</code> if timeout is expired before primitive
     * state is changed
     */
    boolean priorityWait(int rank, long timeout) throws InterruptedException;

    /**
     * Reset primitive.  Semantic of this method
     * depends on particular primitive type and is explained in specification
     * of each primitive.
     */
    void reset() throws IOException, InterruptedException;

    /**
     * Close primitive. This method decrease access counter of the primitive
     * and once it becomes zero, primitive is destroyed.
     */
    void close() throws IOException;

    /**
     * Set exlusive lock. No other session can set exclusive or shared lock.
     */
    void exclusiveLock() throws InterruptedException;

    /**
     * Set exclusive lock with timeout. If lock can not be graned
     * within specifed time, request is failed.
     *
     * @param timeout time in milliseconds
     * @return <code>true</code> if lock is granted, <code>false</code> of timeout
     * is expired
     */
    boolean exclusiveLock(long timeout) throws InterruptedException;

    /**
     * Set shared lock. No other session can set exclusive lock but
     * other shared locks are possible.
     */
    void sharedLock() throws InterruptedException;

    /**
     * Set shared lock with timeout. If lock can not be graned
     * within specifed time, request is failed.
     *
     * @param timeout time in milliseconds
     * @return <code>true</code> if lock is granted, <code>false</code> of timeout
     * is expired
     */
    boolean sharedLock(long timeout) throws InterruptedException;

    /**
     * Set exlusive lock. No other session can set exclusive or shared lock.
     *
     * @param rank processes will be placed in wait queue in the order of increasing
     *             rank value and in the same order will be taken from the queue.
     */
    void priorityExclusiveLock(int rank) throws InterruptedException;

    /**
     * Set exclusive lock with timeout. If lock can not be graned
     * within specifed time, request is failed.
     *
     * @param rank    processes will be placed in wait queue in the order of increasing
     *                rank value and in the same order will be taken from the queue.
     * @param timeout time in milliseconds
     * @return <code>true</code> if lock is granted, <code>false</code> of timeout
     * is expired
     */
    boolean priorityExclusiveLock(int rank, long timeout) throws InterruptedException;

    /**
     * Set shared lock. No other session can set exclusive lock but
     * other shared locks are possible.
     *
     * @param rank processes will be placed in wait queue in the order of increasing
     *             rank value and in the same order will be taken from the queue.
     */
    void prioritySharedLock(int rank) throws InterruptedException;

    /**
     * Set shared lock with timeout. If lock can not be graned
     * within specifed time, request is failed.
     *
     * @param rank    processes will be placed in wait queue in the order of increasing
     *                rank value and in the same order will be taken from the queue.
     * @param timeout time in milliseconds
     * @return <code>true</code> if lock is granted, <code>false</code> of timeout
     * is expired
     */
    boolean prioritySharedLock(int rank, long timeout) throws InterruptedException;

    /**
     * Remove lock from the object
     */
    void unlock() throws NotOwnerException, InterruptedException;
}



