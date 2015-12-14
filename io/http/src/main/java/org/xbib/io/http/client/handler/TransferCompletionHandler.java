package org.xbib.io.http.client.handler;

import io.netty.handler.codec.http.HttpHeaders;
import org.xbib.io.http.client.AsyncCompletionHandlerBase;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.Response;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A {@link AsyncHandler} that can be used to notify a set of {@link TransferListener}
 * <br>
 * <blockquote>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient();
 * TransferCompletionHandler tl = new TransferCompletionHandler();
 * tl.addTransferListener(new TransferListener() {
 *
 * public void onRequestHeadersSent(HttpHeaders headers) {
 * }
 *
 * public void onResponseHeadersReceived(HttpHeaders headers) {
 * }
 *
 * public void onBytesReceived(ByteBuffer buffer) {
 * }
 *
 * public void onBytesSent(long amount, long current, long total) {
 * }
 *
 * public void onRequestResponseCompleted() {
 * }
 *
 * public void onThrowable(Throwable t) {
 * }
 * });
 *
 * Response response = httpClient.prepareGet("http://...").execute(tl).get();
 * </pre>
 *
 * </blockquote>
 */
public class TransferCompletionHandler extends AsyncCompletionHandlerBase {
    private final ConcurrentLinkedQueue<TransferListener> listeners = new ConcurrentLinkedQueue<>();
    private final boolean accumulateResponseBytes;
    private HttpHeaders headers;

    /**
     * Create a TransferCompletionHandler that will not accumulate bytes. The resulting {@link
     * Response#getResponseBody()},
     * {@link Response#getResponseBodyAsStream()} will throw an IllegalStateException if called.
     */
    public TransferCompletionHandler() {
        this(false);
    }

    /**
     * Create a TransferCompletionHandler that can or cannot accumulate bytes and make it available when {@link
     * Response#getResponseBody()} get called. The
     * default is false.
     *
     * @param accumulateResponseBytes true to accumulates bytes in memory.
     */
    public TransferCompletionHandler(boolean accumulateResponseBytes) {
        this.accumulateResponseBytes = accumulateResponseBytes;
    }

    public TransferCompletionHandler addTransferListener(TransferListener t) {
        listeners.offer(t);
        return this;
    }

    public TransferCompletionHandler removeTransferListener(TransferListener t) {
        listeners.remove(t);
        return this;
    }

    public void headers(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public State onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
        fireOnHeaderReceived(headers.getHeaders());
        return super.onHeadersReceived(headers);
    }

    @Override
    public State onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
        State s = State.CONTINUE;
        if (accumulateResponseBytes) {
            s = super.onBodyPartReceived(content);
        }
        fireOnBytesReceived(content.getBodyPartBytes());
        return s;
    }

    @Override
    public Response onCompleted(Response response) throws Exception {
        fireOnEnd();
        return response;
    }

    @Override
    public State onHeadersWritten() {
        if (headers != null) {
            fireOnHeadersSent(headers);
        }
        return State.CONTINUE;
    }

    @Override
    public State onContentWriteProgress(long amount, long current, long total) {
        fireOnBytesSent(amount, current, total);
        return State.CONTINUE;
    }

    @Override
    public void onThrowable(Throwable t) {
        fireOnThrowable(t);
    }

    private void fireOnHeadersSent(HttpHeaders headers) {
        for (TransferListener l : listeners) {
            try {
                l.onRequestHeadersSent(headers);
            } catch (Throwable t) {
                l.onThrowable(t);
            }
        }
    }

    private void fireOnHeaderReceived(HttpHeaders headers) {
        for (TransferListener l : listeners) {
            try {
                l.onResponseHeadersReceived(headers);
            } catch (Throwable t) {
                l.onThrowable(t);
            }
        }
    }

    private void fireOnEnd() {
        for (TransferListener l : listeners) {
            try {
                l.onRequestResponseCompleted();
            } catch (Throwable t) {
                l.onThrowable(t);
            }
        }
    }

    private void fireOnBytesReceived(byte[] b) {
        for (TransferListener l : listeners) {
            try {
                l.onBytesReceived(b);
            } catch (Throwable t) {
                l.onThrowable(t);
            }
        }
    }

    private void fireOnBytesSent(long amount, long current, long total) {
        for (TransferListener l : listeners) {
            try {
                l.onBytesSent(amount, current, total);
            } catch (Throwable t) {
                l.onThrowable(t);
            }
        }
    }

    private void fireOnThrowable(Throwable t) {
        for (TransferListener l : listeners) {
            try {
                l.onThrowable(t);
            } catch (Throwable t2) {
                //
            }
        }
    }
}
