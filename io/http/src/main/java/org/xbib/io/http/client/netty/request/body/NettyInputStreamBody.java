package org.xbib.io.http.client.netty.request.body;

import io.netty.channel.Channel;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedStream;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.request.ProgressListener;

import java.io.IOException;
import java.io.InputStream;

import static org.xbib.io.http.client.util.MiscUtils.closeSilently;

public class NettyInputStreamBody implements NettyBody {


    private final InputStream inputStream;

    public NettyInputStreamBody(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
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
        final InputStream is = inputStream;
        if (future.isStreamWasAlreadyConsumed()) {
            if (is.markSupported()) {
                is.reset();
            } else {
                return;
            }
        } else {
            future.setStreamWasAlreadyConsumed(true);
        }

        channel.write(new ChunkedStream(is), channel.newProgressivePromise()).addListener(
                new ProgressListener(future.getAsyncHandler(), future, false, getContentLength()) {
                    public void operationComplete(ChannelProgressiveFuture cf) {
                        closeSilently(is);
                        super.operationComplete(cf);
                    }
                });
        channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }
}
