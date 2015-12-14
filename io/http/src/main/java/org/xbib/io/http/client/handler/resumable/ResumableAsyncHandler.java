package org.xbib.io.http.client.handler.resumable;

import io.netty.handler.codec.http.HttpHeaders;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.RequestBuilder;
import org.xbib.io.http.client.Response;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An {@link AsyncHandler} which support resumable download, e.g when used with an {@link ResumableIOExceptionFilter},
 * this handler can resume the download operation at the point it was before the interruption occurred. This prevent
 * having to
 * download the entire file again. It's the responsibility of the {@link ResumableAsyncHandler}
 * to track how many bytes has been transferred and to properly adjust the file's write position.
 * In case of a JVM crash/shutdown, you can create an instance of this class and pass the last valid bytes position.
 */
public class ResumableAsyncHandler implements AsyncHandler<Response> {
    private final static ResumableIndexThread resumeIndexThread = new ResumableIndexThread();
    private static Map<String, Long> resumableIndex;
    private final AtomicLong byteTransferred;
    private final ResumableProcessor resumableProcessor;
    private final AsyncHandler<Response> decoratedAsyncHandler;
    private final boolean accumulateBody;
    private String url;
    private Response.ResponseBuilder responseBuilder = new Response.ResponseBuilder();
    private ResumableListener resumableListener = new NULLResumableListener();

    private ResumableAsyncHandler(long byteTransferred, ResumableProcessor resumableProcessor,
                                  AsyncHandler<Response> decoratedAsyncHandler, boolean accumulateBody) {

        this.byteTransferred = new AtomicLong(byteTransferred);

        if (resumableProcessor == null) {
            resumableProcessor = new NULLResumableHandler();
        }
        this.resumableProcessor = resumableProcessor;

        resumableIndex = resumableProcessor.load();
        resumeIndexThread.addResumableProcessor(resumableProcessor);

        this.decoratedAsyncHandler = decoratedAsyncHandler;
        this.accumulateBody = accumulateBody;
    }

    public ResumableAsyncHandler(long byteTransferred) {
        this(byteTransferred, null, null, false);
    }

    public ResumableAsyncHandler(boolean accumulateBody) {
        this(0, null, null, accumulateBody);
    }

    public ResumableAsyncHandler() {
        this(0, null, null, false);
    }

    public ResumableAsyncHandler(AsyncHandler<Response> decoratedAsyncHandler) {
        this(0, new PropertiesBasedResumableProcessor(), decoratedAsyncHandler, false);
    }

    public ResumableAsyncHandler(long byteTransferred, AsyncHandler<Response> decoratedAsyncHandler) {
        this(byteTransferred, new PropertiesBasedResumableProcessor(), decoratedAsyncHandler, false);
    }

    public ResumableAsyncHandler(ResumableProcessor resumableProcessor) {
        this(0, resumableProcessor, null, false);
    }

    public ResumableAsyncHandler(ResumableProcessor resumableProcessor, boolean accumulateBody) {
        this(0, resumableProcessor, null, accumulateBody);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AsyncHandler.State onStatusReceived(final HttpResponseStatus status) throws Exception {
        responseBuilder.accumulate(status);
        if (status.getStatusCode() == 200 || status.getStatusCode() == 206) {
            url = status.getUri().toUrl();
        } else {
            return AsyncHandler.State.ABORT;
        }

        if (decoratedAsyncHandler != null) {
            return decoratedAsyncHandler.onStatusReceived(status);
        }

        return AsyncHandler.State.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onThrowable(Throwable t) {
        if (decoratedAsyncHandler != null) {
            decoratedAsyncHandler.onThrowable(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {

        if (accumulateBody) {
            responseBuilder.accumulate(bodyPart);
        }

        State state = State.CONTINUE;
        try {
            resumableListener.onBytesReceived(bodyPart.getBodyByteBuffer());
        } catch (IOException ex) {
            return AsyncHandler.State.ABORT;
        }

        if (decoratedAsyncHandler != null) {
            state = decoratedAsyncHandler.onBodyPartReceived(bodyPart);
        }

        byteTransferred.addAndGet(bodyPart.getBodyPartBytes().length);
        resumableProcessor.put(url, byteTransferred.get());

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response onCompleted() throws Exception {
        resumableProcessor.remove(url);
        resumableListener.onAllBytesReceived();

        if (decoratedAsyncHandler != null) {
            decoratedAsyncHandler.onCompleted();
        }
        // Not sure
        return responseBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AsyncHandler.State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
        responseBuilder.accumulate(headers);
        String contentLengthHeader = headers.getHeaders().get(HttpHeaders.Names.CONTENT_LENGTH);
        if (contentLengthHeader != null) {
            if (Long.parseLong(contentLengthHeader) == -1L) {
                return AsyncHandler.State.ABORT;
            }
        }

        if (decoratedAsyncHandler != null) {
            return decoratedAsyncHandler.onHeadersReceived(headers);
        }
        return AsyncHandler.State.CONTINUE;
    }

    /**
     * Invoke this API if you want to set the Range header on your {@link Request} based on the last valid bytes
     * position.
     *
     * @param request {@link Request}
     * @return a {@link Request} with the Range header properly set.
     */
    public Request adjustRequestRange(Request request) {

        Long ri = resumableIndex.get(request.getUrl());
        if (ri != null) {
            byteTransferred.set(ri);
        }

        // The Resumable
        if (resumableListener != null && resumableListener.length() > 0 && byteTransferred.get() != resumableListener.length()) {
            byteTransferred.set(resumableListener.length());
        }

        RequestBuilder builder = new RequestBuilder(request);
        if (request.getHeaders().get(HttpHeaders.Names.RANGE) == null && byteTransferred.get() != 0) {
            builder.setHeader(HttpHeaders.Names.RANGE, "bytes=" + byteTransferred.get() + "-");
        }
        return builder.build();
    }

    /**
     * Set a {@link ResumableListener}
     *
     * @param resumableListener a {@link ResumableListener}
     * @return this
     */
    public ResumableAsyncHandler setResumableListener(ResumableListener resumableListener) {
        this.resumableListener = resumableListener;
        return this;
    }

    /**
     * An interface to implement in order to manage the way the incomplete file management are handled.
     */
    public interface ResumableProcessor {

        /**
         * Associate a key with the number of bytes successfully transferred.
         *
         * @param key              a key. The recommended way is to use an url.
         * @param transferredBytes The number of bytes successfully transferred.
         */
        void put(String key, long transferredBytes);

        /**
         * Remove the key associate value.
         *
         * @param key key from which the value will be discarded
         */
        void remove(String key);

        /**
         * Save the current {@link Map} instance which contains information about the current transfer state.
         * This method *only* invoked when the JVM is shutting down.
         *
         * @param map the current transfer state
         */
        void save(Map<String, Long> map);

        /**
         * Load the {@link Map} in memory, contains information about the transferred bytes.
         *
         * @return {@link Map} current transfer state
         */
        Map<String, Long> load();

    }

    private static class ResumableIndexThread extends Thread {

        public final ConcurrentLinkedQueue<ResumableProcessor> resumableProcessors = new ConcurrentLinkedQueue<>();

        public ResumableIndexThread() {
            Runtime.getRuntime().addShutdownHook(this);
        }

        public void addResumableProcessor(ResumableProcessor p) {
            resumableProcessors.offer(p);
        }

        public void run() {
            for (ResumableProcessor p : resumableProcessors) {
                p.save(resumableIndex);
            }
        }
    }

    private static class NULLResumableHandler implements ResumableProcessor {

        public void put(String url, long transferredBytes) {
        }

        public void remove(String uri) {
        }

        public void save(Map<String, Long> map) {
        }

        public Map<String, Long> load() {
            return new HashMap<>();
        }
    }

    private static class NULLResumableListener implements ResumableListener {

        private long length = 0L;

        public void onBytesReceived(ByteBuffer byteBuffer) throws IOException {
            length += byteBuffer.remaining();
        }

        public void onAllBytesReceived() {
        }

        public long length() {
            return length;
        }

    }
}
