package org.xbib.io.http.client.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.exception.ChannelClosedException;
import org.xbib.io.http.client.netty.Callback;
import org.xbib.io.http.client.netty.DiscardEvent;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.channel.ChannelManager;
import org.xbib.io.http.client.netty.channel.Channels;
import org.xbib.io.http.client.netty.future.StackTraceInspector;
import org.xbib.io.http.client.netty.request.NettyRequestSender;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import static org.xbib.io.http.client.util.MiscUtils.getCause;

@Sharable
public class AsyncHttpClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(AsyncHttpClientHandler.class);

    private final AsyncHttpClientConfig config;
    private final ChannelManager channelManager;
    private final NettyRequestSender requestSender;
    private final Protocol protocol;

    public AsyncHttpClientHandler(AsyncHttpClientConfig config,//
                                  ChannelManager channelManager,//
                                  NettyRequestSender requestSender,//
                                  Protocol protocol) {
        this.config = config;
        this.channelManager = channelManager;
        this.requestSender = requestSender;
        this.protocol = protocol;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        Channel channel = ctx.channel();
        Object attribute = Channels.getAttribute(channel);

        try {
            if (attribute instanceof Callback) {
                Callback ac = (Callback) attribute;
                if (msg instanceof LastHttpContent) {
                    ac.call();
                } else if (!(msg instanceof HttpContent)) {
                    logger.info("received unexpected message while expecting a chunk: " + msg);
                    ac.call();
                    Channels.setDiscard(channel);
                }

            } else if (attribute instanceof NettyResponseFuture) {
                NettyResponseFuture<?> future = (NettyResponseFuture<?>) attribute;
                protocol.handle(channel, future, msg);

            } else if (attribute instanceof StreamedResponsePublisher) {

                StreamedResponsePublisher publisher = (StreamedResponsePublisher) attribute;

                if (msg instanceof HttpContent) {
                    ByteBuf content = ((HttpContent) msg).content();
                    // Republish as a HttpResponseBodyPart
                    if (content.readableBytes() > 0) {
                        HttpResponseBodyPart part = config.getResponseBodyPartFactory().newResponseBodyPart(content, false);
                        ctx.fireChannelRead(part);
                    }
                    if (msg instanceof LastHttpContent) {
                        // Remove the handler from the pipeline, this will trigger
                        // it to finish
                        ctx.pipeline().remove(publisher);
                        // Trigger a read, just in case the last read complete
                        // triggered no new read
                        ctx.read();
                        // Send the last content on to the protocol, so that it can
                        // conclude the cleanup
                        protocol.handle(channel, publisher.future(), msg);
                    }
                } else {
                    logger.info("received unexpected message while expecting a chunk: " + msg);
                    ctx.pipeline().remove((StreamedResponsePublisher) attribute);
                    Channels.setDiscard(channel);
                }
            } else if (attribute != DiscardEvent.INSTANCE) {
                // unhandled message
                logger.debug("orphan channel {} with attribute {} received message {}, closing", channel, attribute, msg);
                Channels.silentlyCloseChannel(channel);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        if (requestSender.isClosed()) {
            return;
        }

        Channel channel = ctx.channel();
        channelManager.removeAll(channel);

        try {
            super.channelInactive(ctx);
        } catch (Exception ex) {
            logger.trace("super.channelInactive", ex);
        }

        Object attribute = Channels.getAttribute(channel);
        logger.debug("channel closed: {} with attribute {}", channel, attribute);
        if (attribute instanceof StreamedResponsePublisher) {
            // setting `attribute` to be the underlying future so that the retry
            // logic can kick-in
            attribute = ((StreamedResponsePublisher) attribute).future();
        }
        if (attribute instanceof Callback) {
            Callback callback = (Callback) attribute;
            Channels.setAttribute(channel, callback.future());
            callback.call();

        } else if (attribute instanceof NettyResponseFuture<?>) {
            NettyResponseFuture<?> future = NettyResponseFuture.class.cast(attribute);
            future.touch();

            if (!config.getIoExceptionFilters().isEmpty() && requestSender.applyIoExceptionFiltersAndReplayRequest(future, ChannelClosedException.INSTANCE, channel)) {
                return;
            }

            protocol.onClose(future);
            requestSender.handleUnexpectedClosedChannel(channel, future);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        Throwable cause = getCause(e.getCause());

        if (cause instanceof PrematureChannelClosureException || cause instanceof ClosedChannelException) {
            return;
        }

        Channel channel = ctx.channel();
        NettyResponseFuture<?> future = null;

        logger.debug("unexpected I/O exception on channel {}", channel, cause);

        try {
            Object attribute = Channels.getAttribute(channel);
            if (attribute instanceof StreamedResponsePublisher) {
                ctx.fireExceptionCaught(e);
                // setting `attribute` to be the underlying future so that the
                // retry logic can kick-in
                attribute = ((StreamedResponsePublisher) attribute).future();
            }
            if (attribute instanceof NettyResponseFuture<?>) {
                future = (NettyResponseFuture<?>) attribute;
                future.attachChannel(null, false);
                future.touch();

                if (cause instanceof IOException) {

                    // FIXME why drop the original exception and throw a new
                    // one?
                    if (!config.getIoExceptionFilters().isEmpty()) {
                        if (!requestSender.applyIoExceptionFiltersAndReplayRequest(future, ChannelClosedException.INSTANCE, channel))
                        // Close the channel so the recovering can occurs.
                        {
                            Channels.silentlyCloseChannel(channel);
                        }
                        return;
                    }
                }

                if (StackTraceInspector.recoverOnReadOrWriteException(cause)) {
                    logger.debug("trying to recover from dead channel: {}", channel);
                    return;
                }
            } else if (attribute instanceof Callback) {
                future = Callback.class.cast(attribute).future();
            }
        } catch (Throwable t) {
            cause = t;
        }

        if (future != null) {
            try {
                logger.debug("was unable to recover, future {}", future);
                requestSender.abort(channel, future, cause);
                protocol.onError(future, e);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }

        channelManager.closeChannel(channel);
        // FIXME not really sure
        // ctx.fireChannelRead(e);
        Channels.silentlyCloseChannel(channel);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (!isHandledByReactiveStreams(ctx)) {
            ctx.read();
        } else {
            ctx.fireChannelReadComplete();
        }
    }

    private boolean isHandledByReactiveStreams(ChannelHandlerContext ctx) {
        return Channels.getAttribute(ctx.channel()) instanceof StreamedResponsePublisher;
    }
}
