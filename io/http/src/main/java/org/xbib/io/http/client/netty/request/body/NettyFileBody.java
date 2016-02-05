package org.xbib.io.http.client.netty.request.body;

import io.netty.channel.Channel;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedNioFile;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.channel.ChannelManager;
import org.xbib.io.http.client.netty.request.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class NettyFileBody implements NettyBody {

    private final File file;
    private final long offset;
    private final long length;
    private final AsyncHttpClientConfig config;

    public NettyFileBody(File file, AsyncHttpClientConfig config) {
        this(file, 0, file.length(), config);
    }

    public NettyFileBody(File file, long offset, long length, AsyncHttpClientConfig config) {
        if (!file.isFile()) {
            throw new IllegalArgumentException(String.format("File %s is not a file or doesn't exist", file.getAbsolutePath()));
        }
        this.file = file;
        this.offset = offset;
        this.length = length;
        this.config = config;
    }

    public File getFile() {
        return file;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public long getContentLength() {
        return length;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void write(Channel channel, NettyResponseFuture<?> future) throws IOException {
        @SuppressWarnings("resource")
        // Netty will close the ChunkedNioFile or the DefaultFileRegion
        final FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel();

        Object message = (ChannelManager.isSslHandlerConfigured(channel.pipeline()) || config.isDisableZeroCopy()) ? //
                new ChunkedNioFile(fileChannel, offset, length, config.getChunkedFileChunkSize())
                : new DefaultFileRegion(fileChannel, offset, length);

        channel.write(message, channel.newProgressivePromise())//
                .addListener(new ProgressListener(future.getAsyncHandler(), future, false, getContentLength()));
        channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }
}
