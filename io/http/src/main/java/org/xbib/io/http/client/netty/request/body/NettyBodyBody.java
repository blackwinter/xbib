package org.xbib.io.http.client.netty.request.body;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.channel.ChannelManager;
import org.xbib.io.http.client.netty.request.ProgressListener;
import org.xbib.io.http.client.request.body.Body;
import org.xbib.io.http.client.request.body.RandomAccessBody;
import org.xbib.io.http.client.request.body.generator.BodyGenerator;
import org.xbib.io.http.client.request.body.generator.FeedListener;
import org.xbib.io.http.client.request.body.generator.FeedableBodyGenerator;
import org.xbib.io.http.client.request.body.generator.ReactiveStreamsBodyGenerator;

import java.io.IOException;

import static org.xbib.io.http.client.util.MiscUtils.closeSilently;

public class NettyBodyBody implements NettyBody {

    private final Body body;
    private final AsyncHttpClientConfig config;

    public NettyBodyBody(Body body, AsyncHttpClientConfig config) {
        this.body = body;
        this.config = config;
    }

    public Body getBody() {
        return body;
    }

    @Override
    public long getContentLength() {
        return body.getContentLength();
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void write(final Channel channel, NettyResponseFuture<?> future) throws IOException {

        Object msg;
        if (body instanceof RandomAccessBody && !ChannelManager.isSslHandlerConfigured(channel.pipeline()) && !config.isDisableZeroCopy()) {
            msg = new BodyReferenceCounted((RandomAccessBody) body);

        } else {
            msg = new BodyChunkedInput(body);

            BodyGenerator bg = future.getTargetRequest().getBodyGenerator();
            if (bg instanceof FeedableBodyGenerator && !(bg instanceof ReactiveStreamsBodyGenerator)) {
                final ChunkedWriteHandler chunkedWriteHandler = channel.pipeline().get(ChunkedWriteHandler.class);
                FeedableBodyGenerator.class.cast(bg).setListener(new FeedListener() {
                    @Override
                    public void onContentAdded() {
                        chunkedWriteHandler.resumeTransfer();
                    }

                    @Override
                    public void onError(Throwable t) {
                    }
                });
            }
        }
        ChannelFuture writeFuture = channel.write(msg, channel.newProgressivePromise());

        writeFuture.addListener(new ProgressListener(future.getAsyncHandler(), future, false, getContentLength()) {
            public void operationComplete(ChannelProgressiveFuture cf) {
                closeSilently(body);
                super.operationComplete(cf);
            }
        });
        channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }
}
