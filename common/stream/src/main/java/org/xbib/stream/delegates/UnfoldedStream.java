package org.xbib.stream.delegates;

import org.xbib.stream.Stream;
import org.xbib.stream.exceptions.StreamSkipSignal;
import org.xbib.stream.exceptions.StreamStopSignal;
import org.xbib.stream.generators.Generator;

/**
 * A {@link Stream} of elements generated by unfolding the elements of an input {@link Stream} into multiple elements.
 *
 * @param <E1> the type of elements of the input stream
 * @param <E2> the type of stream elements
 */
public class UnfoldedStream<E1, E2> extends AbstractDelegateStream<E1, E2> {

    private final Generator<E1, Stream<E2>> generator;
    private Stream<E2> unfold;

    /**
     * Creates an instance with a {@link Stream} and an element {@link Generator}.
     *
     * @param stream    the stream
     * @param generator the generator
     * @throws IllegalArgumentException if the stream or the generator are <code>null</code>
     */
    public UnfoldedStream(Stream<E1> stream, Generator<E1, Stream<E2>> generator) throws IllegalArgumentException {

        super(stream);

        if (generator == null) {
            throw new IllegalArgumentException("invalid null generator");
        }

        this.generator = generator;
    }

    private RuntimeException lookAheadFailure;

    @Override
    protected E2 delegateNext() {
        return lookAheadFailureOrNextInUnfold();
    }

    @Override
    protected boolean delegateHasNext() {

        if (!hasUnfold()) {
            return false;
        }

        return existsInThisOrNextUnfold();
    }

    @Override
    public void close() {

        if (unfold != null) {
            unfold.close();
        }

        stream().close();
    }

    //helpers
    private boolean hasUnfold() {

        if (unfold == null) {
            if (stream().hasNext()) {
                try {
                    unfold = generator.yield(stream().next());
                } catch (StreamStopSignal stop) {
                    return false;
                } catch (StreamSkipSignal skip) {
                    return hasUnfold();
                } catch (RuntimeException unchecked) {
                    lookAheadFailure = unchecked;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    private boolean existsInThisOrNextUnfold() {

        boolean hasNext = unfold.hasNext();

        if (!hasNext) {
            unfold.close();
            unfold = null;
            return delegateHasNext();
        }

        return hasNext;
    }

    private E2 lookAheadFailureOrNextInUnfold() {

        try {
            if (lookAheadFailure != null) {
                throw lookAheadFailure;
            } else {
                return unfold.next();
            }
        } finally {
            lookAheadFailure = null;
        }
    }
}
