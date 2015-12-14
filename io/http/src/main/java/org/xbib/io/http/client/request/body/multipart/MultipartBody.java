package org.xbib.io.http.client.request.body.multipart;

import io.netty.buffer.ByteBuf;
import org.xbib.io.http.client.netty.request.body.BodyChunkedInput;
import org.xbib.io.http.client.request.body.RandomAccessBody;
import org.xbib.io.http.client.request.body.multipart.part.MultipartPart;
import org.xbib.io.http.client.request.body.multipart.part.MultipartState;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.xbib.io.http.client.util.MiscUtils.closeSilently;

public class MultipartBody implements RandomAccessBody {

    private final List<MultipartPart<? extends Part>> parts;
    private final String contentType;
    private final byte[] boundary;
    private final long contentLength;
    private int currentPartIndex;
    private boolean done = false;
    private AtomicBoolean closed = new AtomicBoolean();

    public MultipartBody(List<MultipartPart<? extends Part>> parts, String contentType, byte[] boundary) {
        this.boundary = boundary;
        this.contentType = contentType;
        this.parts = parts;
        this.contentLength = computeContentLength();
    }

    private long computeContentLength() {
        try {
            long total = 0;
            for (MultipartPart<? extends Part> part : parts) {
                long l = part.length();
                if (l < 0) {
                    return -1;
                }
                total += l;
            }
            return total;
        } catch (Exception e) {
            return 0L;
        }
    }

    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            for (MultipartPart<? extends Part> part : parts) {
                closeSilently(part);
            }
        }
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBoundary() {
        return boundary;
    }

    // Regular Body API
    public BodyState transferTo(ByteBuf target) throws IOException {

        if (done) {
            return BodyState.STOP;
        }

        while (target.isWritable() && !done) {
            MultipartPart<? extends Part> currentPart = parts.get(currentPartIndex);
            currentPart.transferTo(target);

            if (currentPart.getState() == MultipartState.DONE) {
                currentPartIndex++;
                if (currentPartIndex == parts.size()) {
                    done = true;
                }
            }
        }

        return BodyState.CONTINUE;
    }

    // RandomAccessBody API, suited for HTTP but not for HTTPS (zero-copy)
    @Override
    public long transferTo(WritableByteChannel target) throws IOException {

        if (done) {
            return -1L;
        }

        long transferred = 0L;
        boolean slowTarget = false;

        while (transferred < BodyChunkedInput.DEFAULT_CHUNK_SIZE && !done && !slowTarget) {
            MultipartPart<? extends Part> currentPart = parts.get(currentPartIndex);
            transferred += currentPart.transferTo(target);
            slowTarget = currentPart.isTargetSlow();

            if (currentPart.getState() == MultipartState.DONE) {
                currentPartIndex++;
                if (currentPartIndex == parts.size()) {
                    done = true;
                }
            }
        }

        return transferred;
    }
}
