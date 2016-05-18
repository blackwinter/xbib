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
public class BodyReferenceCounted extends AbstractReferenceCounted {

    private final RandomAccessBody body;
    private long transfered;
    private final BodyFileRegion bodyFileRegion;

    public BodyReferenceCounted(RandomAccessBody body) {
        this.body = body;
        this.bodyFileRegion = new BodyFileRegion();
    }

    @Override
    public BodyReferenceCounted touch(Object hint) {
        return this;
    }

    @Override
    protected void deallocate() {
        closeSilently(body);
    }

    public FileRegion fileRegion() {
        return bodyFileRegion;
    }

    public class BodyFileRegion implements FileRegion {

        @Override
        public long position() {
            return 0;
        }

        @Override
        public long transfered() {
            return transfered;
        }

        @Override
        public long transferred() {
            return transfered;
        }

        @Override
        public long count() {
            return body.getContentLength();
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
        public int refCnt() {
            return BodyReferenceCounted.super.refCnt();
        }

        @Override
        public FileRegion retain() {
            BodyReferenceCounted.super.retain();
            return this;
        }

        @Override
        public FileRegion retain(int increment) {
            BodyReferenceCounted.super.retain(increment);
            return this;
        }

        @Override
        public FileRegion touch() {
            return touch(null);
        }

        @Override
        public FileRegion touch(Object hint) {
            return this;
        }

        @Override
        public boolean release() {
            return BodyReferenceCounted.super.release();
        }

        @Override
        public boolean release(int decrement) {
            return BodyReferenceCounted.super.release(decrement);
        }
    }

}
