package org.xbib.io.http.client.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.handler.AsyncHandlerExtensions;
import org.xbib.io.http.client.netty.request.NettyRequest;
import org.xbib.io.http.client.reactivestreams.ReactiveStreamsTest;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertTrue;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.test.TestUtils.LARGE_IMAGE_BYTES;

public class NettyReactiveStreamsTest extends ReactiveStreamsTest {

    @Test(groups = "standalone")
    public void testRetryingOnFailingStream() throws Exception {
        try (AsyncHttpClient client = asyncHttpClient()) {
            final CountDownLatch streamStarted = new CountDownLatch(1); // allows us to wait until subscriber has received the first body chunk
            final CountDownLatch streamOnHold = new CountDownLatch(1); // allows us to hold the subscriber from processing further body chunks
            final CountDownLatch replayingRequest = new CountDownLatch(1); // allows us to block until the request is being replayed ( this is what we want to test here!)

            // a ref to the publisher is needed to get a hold on the channel (if there is a better way, this should be changed) 
            final AtomicReference<StreamedResponsePublisher> publisherRef = new AtomicReference<>(null);

            // executing the request
            client.preparePost(getTargetUrl())
                    .setBody(LARGE_IMAGE_BYTES)
                    .execute(new ReplayedSimpleAsyncHandler(replayingRequest,
                            new BlockedStreamSubscriber(streamStarted, streamOnHold)) {
                        @Override
                        public State onStream(Publisher<HttpResponseBodyPart> publisher) {
                            if (!(publisher instanceof StreamedResponsePublisher)) {
                                throw new IllegalStateException(String.format("publisher %s is expected to be an instance of %s", publisher, StreamedResponsePublisher.class));
                            } else if (!publisherRef.compareAndSet(null, (StreamedResponsePublisher) publisher)) {
                                // abort on retry
                                return State.ABORT;
                            }
                            return super.onStream(publisher);
                        }
                    });

            // before proceeding, wait for the subscriber to receive at least one body chunk
            streamStarted.await();
            // The stream has started, hence `StreamedAsyncHandler.onStream(publisher)` was called, and `publisherRef` was initialized with the `publisher` passed to `onStream`
            assertTrue(publisherRef.get() != null, "Expected a not null publisher.");

            // close the channel to emulate a connection crash while the response body chunks were being received.
            StreamedResponsePublisher publisher = publisherRef.get();
            final CountDownLatch channelClosed = new CountDownLatch(1);

            getChannel(publisher).close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    channelClosed.countDown();
                }
            });
            streamOnHold.countDown(); // the subscriber is set free to process new incoming body chunks.
            channelClosed.await(); // the channel is confirmed to be closed

            // now we expect a new connection to be created and AHC retry logic to kick-in automatically
            replayingRequest.await(); // wait until we are notified the request is being replayed

            // Change this if there is a better way of stating the test succeeded 
            assertTrue(true);
        }
    }

    private Channel getChannel(StreamedResponsePublisher publisher) throws Exception {
        Field field = publisher.getClass().getDeclaredField("channel");
        field.setAccessible(true);
        return (Channel) field.get(publisher);
    }

    private static class BlockedStreamSubscriber extends SimpleSubscriber<HttpResponseBodyPart> {
        private final CountDownLatch streamStarted;
        private final CountDownLatch streamOnHold;

        public BlockedStreamSubscriber(CountDownLatch streamStarted, CountDownLatch streamOnHold) {
            this.streamStarted = streamStarted;
            this.streamOnHold = streamOnHold;
        }

        @Override
        public void onNext(HttpResponseBodyPart t) {
            streamStarted.countDown();
            try {
                streamOnHold.await();
            } catch (InterruptedException e) {
                //
            }
            super.onNext(t);
        }
    }

    private static class ReplayedSimpleAsyncHandler extends SimpleStreamedAsyncHandler implements AsyncHandlerExtensions {
        private final CountDownLatch replaying;

        public ReplayedSimpleAsyncHandler(CountDownLatch replaying, SimpleSubscriber<HttpResponseBodyPart> subscriber) {
            super(subscriber);
            this.replaying = replaying;
        }

        @Override
        public void onHostnameResolutionAttempt(String name) {
        }

        @Override
        public void onHostnameResolutionSuccess(String name, List<InetSocketAddress> addresses) {
        }

        @Override
        public void onHostnameResolutionFailure(String name, Throwable cause) {
        }

        @Override
        public void onTcpConnectAttempt(InetSocketAddress address) {
        }

        @Override
        public void onTcpConnectSuccess(InetSocketAddress address, Channel connection) {
        }

        @Override
        public void onTcpConnectFailure(InetSocketAddress address, Throwable cause) {
        }

        @Override
        public void onTlsHandshakeAttempt() {
        }

        @Override
        public void onTlsHandshakeSuccess() {
        }

        @Override
        public void onTlsHandshakeFailure(Throwable cause) {
        }

        @Override
        public void onConnectionPoolAttempt() {
        }

        @Override
        public void onConnectionPooled(Channel connection) {
        }

        @Override
        public void onConnectionOffer(Channel connection) {
        }

        @Override
        public void onRequestSend(NettyRequest request) {
        }

        @Override
        public void onRetry() {
            replaying.countDown();
        }
    }
}
