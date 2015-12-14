package org.xbib.io.http.client.netty.request.body;

import com.typesafe.netty.HandlerSubscriber;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.EventExecutor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.xbib.io.http.client.netty.NettyResponseFuture;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

public class NettyReactiveStreamsBody implements NettyBody {

    private static final String NAME_IN_CHANNEL_PIPELINE = "request-body-streamer";

    private final Publisher<ByteBuffer> publisher;

    public NettyReactiveStreamsBody(Publisher<ByteBuffer> publisher) {
        this.publisher = publisher;
    }

    @Override
    public long getContentLength() {
        return -1L;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void write(Channel channel, NettyResponseFuture<?> future) throws IOException {
        if (!future.isStreamWasAlreadyConsumed()) {
            future.setStreamWasAlreadyConsumed(true);
            NettySubscriber subscriber = new NettySubscriber(channel, future);
            channel.pipeline().addLast(NAME_IN_CHANNEL_PIPELINE, subscriber);
            publisher.subscribe(new SubscriberAdapter(subscriber));
        }
    }

    private static class SubscriberAdapter implements Subscriber<ByteBuffer> {
        private volatile Subscriber<HttpContent> subscriber;

        public SubscriberAdapter(Subscriber<HttpContent> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onSubscribe(Subscription s) {
            subscriber.onSubscribe(s);
        }

        @Override
        public void onNext(ByteBuffer t) {
            ByteBuf buffer = Unpooled.wrappedBuffer(t.array());
            HttpContent content = new DefaultHttpContent(buffer);
            subscriber.onNext(content);
        }

        @Override
        public void onError(Throwable t) {
            subscriber.onError(t);
        }

        @Override
        public void onComplete() {
            subscriber.onComplete();
        }
    }

    private static class NettySubscriber extends HandlerSubscriber<HttpContent> {

        private final Channel channel;
        private final NettyResponseFuture<?> future;

        public NettySubscriber(Channel channel, NettyResponseFuture<?> future) {
            super(channel.eventLoop());
            this.channel = channel;
            this.future = future;
        }

        @Override
        protected void complete() {
            EventExecutor executor = channel.eventLoop();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            removeFromPipeline();
                        }
                    });
                }
            });
        }

        @Override
        protected void error(Throwable error) {
            if (error == null) {
                throw null;
            }
            removeFromPipeline();
            future.abort(error);
        }

        private void removeFromPipeline() {
            try {
                channel.pipeline().remove(this);
            } catch (NoSuchElementException e) {
                //
            }
        }
    }
}
