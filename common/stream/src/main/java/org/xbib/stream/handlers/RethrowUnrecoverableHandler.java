package org.xbib.stream.handlers;

import org.xbib.stream.exceptions.StreamContingency;


/**
 * A {@link org.xbib.stream.handlers.FaultHandler} that silently absorbs {@link StreamContingency}s
 * but re-throws all other failures.
 */
public class RethrowUnrecoverableHandler implements FaultHandler {

    @Override
    public void handle(RuntimeException failure) {

        if (!isContingency(failure)) {
            throw failure;
        }
    }

    /**
     * Indicates whether a failure or its indirect causes are annotated with {@link org.xbib.stream.exceptions.StreamContingency}.
     * @param t the failure
     * @return <code>true</code> if the failure or its indirect causes are annotated with {@link org.xbib.stream.exceptions.StreamContingency}.
     */
    private static boolean isContingency(Throwable t) {
        return t.getClass().isAnnotationPresent(StreamContingency.class)
                || ((t.getCause()!=null) &&(isContingency(t.getCause())));
    }
}
