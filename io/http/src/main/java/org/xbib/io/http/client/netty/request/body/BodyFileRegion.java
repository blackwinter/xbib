package org.xbib.io.http.client.netty.request.body;

import io.netty.channel.FileRegion;
import io.netty.util.AbstractReferenceCounted;
import org.xbib.io.http.client.request.body.RandomAccessBody;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

import static org.xbib.io.http.client.util.MiscUtils.closeSilently;

/**
 * Adapts a {@link RandomAccessBody} to Netty's {@link FileRegion}.
 */
public class BodyFileRegion extends AbstractReferenceCounted implements FileRegion {

    private final RandomAccessBody body;
    private long transfered;

    public BodyFileRegion(RandomAccessBody body) {
        this.body = body;
    }

    @Override
    public long position() {
        return 0;
    }

    @Override
    public long count() {
        return body.getContentLength();
    }

    @Override
    public long transfered() {
        return transfered;
    }

    @Override
    public long transferTo(WritableByteChannel target, long position) throws IOException {
        long written = body.transferTo(target);
        if (written > 0) {
            transfered += written;
        }
        return written;
    }

    @Override
    protected void deallocate() {
        closeSilently(body);
    }
}
