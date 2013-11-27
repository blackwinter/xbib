package org.xbib.stream.delegates;

import org.xbib.stream.Stream;

/**
 * A {@link Stream} that notifies an {@link org.xbib.stream.delegates.StreamListener} of key iteration events.
 *
 * @param <E> the type of stream elements
 */
public class MonitoredStream<E> extends AbstractDelegateStream<E, E> {

    private final StreamListener listener;

    private boolean started = false;

    /**
     * Creates an instance with a {@link Stream} and a {@link org.xbib.stream.delegates.StreamListener}.
     *
     * @param stream   the stream
     * @param listener the listener
     * @throws IllegalArgumentException if the stream or the listener are {@code null}
     */
    public MonitoredStream(Stream<E> stream, StreamListener listener) throws IllegalArgumentException {

        super(stream);

        if (listener == null) {
            throw new IllegalArgumentException("invalid null listener");
        }

        this.listener = listener;
    }

    @Override
    protected E delegateNext() {

        E element = stream().next();

        if (!started) {
            listener.onStart();
            started = true;
        }

        if (!delegateHasNext()) {
            listener.onEnd();
        }

        return element;

    }

    @Override
    protected boolean delegateHasNext() {
        return stream().hasNext();
    }

    @Override
    public void close() {
        super.close();
        listener.onClose();
    }
}
