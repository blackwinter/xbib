package org.xbib.io.http.client.future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A list of ({@code Runnable}, {@code Executor}) pairs that guarantees
 * that every {@code Runnable} that is added using the add method will be
 * executed in its associated {@code Executor} after {@link #run()} is called.
 * {@code Runnable}s added after {@code run} is called are still guaranteed to
 * execute.
 */
public final class ExecutionList implements Runnable {

    // Logger to log exceptions caught when running runnables.
    private static final Logger log = LogManager.getLogger(ExecutionList.class.getName());

    // The runnable,executor pairs to execute.
    private final Queue<RunnableExecutorPair> runnables = new LinkedBlockingQueue<>();

    // Boolean we use mark when execution has started.  Only accessed from within
    // synchronized blocks.
    private boolean executed = false;

    /**
     * Add the runnable/executor pair to the list of pairs to execute.  Executes
     * the pair immediately if we've already started execution.
     *
     * @param runnable the runnable to be executed on complete
     * @param executor teh executor to run the runnable
     */
    public void add(Runnable runnable, Executor executor) {
        boolean executeImmediate = false;

        // Lock while we check state.  We must maintain the lock while adding the
        // new pair so that another thread can't run the list out from under us.
        // We only add to the list if we have not yet started execution.
        synchronized (runnables) {
            if (!executed) {
                runnables.add(new RunnableExecutorPair(runnable, executor));
            } else {
                executeImmediate = true;
            }
        }

        // Execute the runnable immediately.  Because of scheduling this may end up
        // getting called before some of the previously added runnables, but we're
        // ok with that.  If we want to change the contract to guarantee ordering
        // among runnables we'd have to modify the logic here to allow it.
        if (executeImmediate) {
            executor.execute(runnable);
        }
    }

    /**
     * Runs this execution list, executing all pairs in the order they were
     * added.  Pairs added after this method has started executing the list will
     * be executed immediately.
     */
    public void run() {

        // Lock while we update our state so the add method above will finish adding
        // any listeners before we start to run them.
        synchronized (runnables) {
            executed = true;
        }

        // At this point the runnables will never be modified by another
        // thread, so we are safe using it outside of the synchronized block.
        while (!runnables.isEmpty()) {
            runnables.poll().execute();
        }
    }

    private static class RunnableExecutorPair {
        final Runnable runnable;
        final Executor executor;

        RunnableExecutorPair(Runnable runnable, Executor executor) {
            this.runnable = runnable;
            this.executor = executor;
        }

        void execute() {
            try {
                executor.execute(runnable);
            } catch (RuntimeException e) {
                // Log it and keep going, bad runnable and/or executor.  Don't
                // punish the other runnables if we're given a bad one.  We only
                // catch RuntimeException because we want Errors to propagate up.
                log.error("RuntimeException while executing runnable " + runnable + " with executor " + executor, e);
            }
        }
    }
}
