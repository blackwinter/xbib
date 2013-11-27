package org.xbib.stream.delegates;

import org.xbib.stream.Stream;
import org.xbib.stream.handlers.FaultHandler;

/**
 * A {@link Stream} that guards failures with a {@link org.xbib.stream.handlers.FaultHandler}
 *
 * @param <E> the type of stream element
 */
public class GuardedStream<E> extends AbstractDelegateStream<E, E> {

    /**
     * Creates an instance with a {@link Stream} and a {@link org.xbib.stream.handlers.FaultHandler}
     *
     * @param stream  the stream
     * @param handler the handler
     * @throws IllegalArgumentException if the stream or the handler are <code>null</code>
     */
    public GuardedStream(Stream<E> stream, FaultHandler handler) throws IllegalArgumentException {

        super(stream);

        if (handler == null) {
            throw new IllegalArgumentException("invalid null generator");
        }

        this.setHandler(handler);
    }

    @Override
    protected E delegateNext() {
        return stream().next();
    }

    @Override
    protected boolean delegateHasNext() {
        return stream().hasNext();
    }

}
