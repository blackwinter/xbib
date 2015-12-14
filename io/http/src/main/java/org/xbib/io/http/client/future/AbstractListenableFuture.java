package org.xbib.io.http.client.future;

import org.xbib.io.http.client.ListenableFuture;

import java.util.concurrent.Executor;

/**
 * An abstract base implementation of the listener support provided by
 * {@link ListenableFuture}. This class uses an {@link ExecutionList} to
 * guarantee that all registered listeners will be executed. Listener/Executor
 * pairs are stored in the execution list and executed in the order in which
 * they were added, but because of thread scheduling issues there is no
 * guarantee that the JVM will execute them in order. In addition, listeners
 * added after the task is complete will be executed immediately, even if some
 * previously added listeners have not yet been executed.
 */
public abstract class AbstractListenableFuture<V> implements ListenableFuture<V> {

    // The execution list to hold our executors.
    private final ExecutionList executionList = new ExecutionList();

    /*
    * Adds a listener/executor pair to execution list to execute when this task
    * is completed.
    */

    public ListenableFuture<V> addListener(Runnable listener, Executor exec) {
        executionList.add(listener, exec);
        return this;
    }

    /*
    * Execute the execution list.
    */
    protected void runListeners() {
        executionList.run();
    }
}
