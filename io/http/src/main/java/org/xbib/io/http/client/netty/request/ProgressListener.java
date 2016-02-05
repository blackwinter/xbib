package org.xbib.io.http.client.netty.request;

import io.netty.channel.Channel;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.handler.ProgressAsyncHandler;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.channel.ChannelState;
import org.xbib.io.http.client.netty.channel.Channels;
import org.xbib.io.http.client.netty.future.StackTraceInspector;

import java.nio.channels.ClosedChannelException;

public class ProgressListener implements ChannelProgressiveFutureListener {

    private final AsyncHandler<?> asyncHandler;
    private final NettyResponseFuture<?> future;
    private final boolean notifyHeaders;
    private final long expectedTotal;
    private long lastProgress = 0L;

    public ProgressListener(AsyncHandler<?> asyncHandler,
                            NettyResponseFuture<?> future,
                            boolean notifyHeaders,
                            long expectedTotal) {
        this.asyncHandler = asyncHandler;
        this.future = future;
        this.notifyHeaders = notifyHeaders;
        this.expectedTotal = expectedTotal;
    }

    private boolean abortOnThrowable(Throwable cause, Channel channel) {

        if (cause != null && future.getChannelState() != ChannelState.NEW) {
            if (cause instanceof IllegalStateException || cause instanceof ClosedChannelException || StackTraceInspector.recoverOnReadOrWriteException(cause)) {
                Channels.silentlyCloseChannel(channel);

            } else {
                future.abort(cause);
            }
            return true;
        }

        return false;
    }

    @Override
    public void operationComplete(ChannelProgressiveFuture cf) {
        // The write operation failed. If the channel was cached, it means it got asynchronously closed.
        // Let's retry a second time.
        if (!abortOnThrowable(cf.cause(), cf.channel())) {

            future.touch();

            /**
             * We need to make sure we aren't in the middle of an authorization
             * process before publishing events as we will re-publish again the
             * same event after the authorization, causing unpredictable
             * behavior.
             */
            boolean startPublishing = !future.getInAuth().get() && !future.getInProxyAuth().get();

            if (startPublishing && asyncHandler instanceof ProgressAsyncHandler) {
                ProgressAsyncHandler<?> progressAsyncHandler = (ProgressAsyncHandler<?>) asyncHandler;
                if (notifyHeaders) {
                    progressAsyncHandler.onHeadersWritten();
                } else {
                    progressAsyncHandler.onContentWritten();
                }
            }
        }
    }

    @Override
    public void operationProgressed(ChannelProgressiveFuture f, long progress, long total) {
        future.touch();
        if (!notifyHeaders && asyncHandler instanceof ProgressAsyncHandler) {
            long lastLastProgress = lastProgress;
            lastProgress = progress;
            if (total < 0) {
                total = expectedTotal;
            }
            ProgressAsyncHandler.class.cast(asyncHandler).onContentWriteProgress(progress - lastLastProgress, progress, total);
        }
    }
}
