package org.xbib.io.redis;

/**
 * Cursor result using the Streaming API. Provides the count of retrieved elements.
 */
public class StreamScanCursor extends ScanCursor {
    private long count;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
