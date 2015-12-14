package org.xbib.io.http.client.reactivestreams;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;
import org.xbib.io.http.client.ListenableFuture;
import org.xbib.io.http.client.handler.StreamedAsyncHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.test.TestUtils.createTempFile;

public class ReactiveStreamsDownLoadTest {

    private int serverPort = 8080;
    private File largeFile;
    private File smallFile;

    @BeforeClass(alwaysRun = true)
    public void setUpBeforeTest() throws Exception {
        largeFile = createTempFile(15 * 1024);
        smallFile = createTempFile(20);
        HttpStaticFileServer.start(serverPort);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        HttpStaticFileServer.shutdown();
    }

    @Test(groups = "standalone")
    public void streamedResponseLargeFileTest() throws Throwable {
        try (AsyncHttpClient c = asyncHttpClient()) {
            String largeFileName = "http://localhost:" + serverPort + "/" + largeFile.getName();
            ListenableFuture<SimpleStreamedAsyncHandler> future = c.prepareGet(largeFileName).execute(new SimpleStreamedAsyncHandler());
            byte[] result = future.get().getBytes();
            assertEquals(result.length, largeFile.length());
        }
    }

    @Test(groups = "standalone")
    public void streamedResponseSmallFileTest() throws Throwable {
        try (AsyncHttpClient c = asyncHttpClient()) {
            String smallFileName = "http://localhost:" + serverPort + "/" + smallFile.getName();
            ListenableFuture<SimpleStreamedAsyncHandler> future = c.prepareGet(smallFileName).execute(new SimpleStreamedAsyncHandler());
            byte[] result = future.get().getBytes();
            assertEquals(result.length, smallFile.length());
        }
    }

    static protected class SimpleStreamedAsyncHandler implements StreamedAsyncHandler<SimpleStreamedAsyncHandler> {
        private final SimpleSubscriber<HttpResponseBodyPart> subscriber;

        public SimpleStreamedAsyncHandler() {
            this(new SimpleSubscriber<HttpResponseBodyPart>());
        }

        public SimpleStreamedAsyncHandler(SimpleSubscriber<HttpResponseBodyPart> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public State onStream(Publisher<HttpResponseBodyPart> publisher) {
            publisher.subscribe(subscriber);
            return State.CONTINUE;
        }

        @Override
        public void onThrowable(Throwable t) {
            throw new AssertionError(t);
        }

        @Override
        public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
            throw new AssertionError("Should not have received body part");
        }

        @Override
        public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
            return State.CONTINUE;
        }

        @Override
        public State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
            return State.CONTINUE;
        }

        @Override
        public SimpleStreamedAsyncHandler onCompleted() throws Exception {
            return this;
        }

        public byte[] getBytes() throws Throwable {
            List<HttpResponseBodyPart> bodyParts = subscriber.getElements();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            for (HttpResponseBodyPart part : bodyParts) {
                bytes.write(part.getBodyPartBytes());
            }
            return bytes.toByteArray();
        }
    }

    /**
     * Simple subscriber that requests and buffers one element at a time.
     */
    static protected class SimpleSubscriber<T> implements Subscriber<T> {
        private final List<T> elements = Collections.synchronizedList(new ArrayList<T>());
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile Subscription subscription;
        private volatile Throwable error;

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(T t) {
            elements.add(t);
            subscription.request(1);
        }

        @Override
        public void onError(Throwable error) {
            this.error = error;
            latch.countDown();
        }

        @Override
        public void onComplete() {
            latch.countDown();
        }

        public List<T> getElements() throws Throwable {
            latch.await();
            if (error != null) {
                throw error;
            } else {
                return elements;
            }
        }
    }
}
