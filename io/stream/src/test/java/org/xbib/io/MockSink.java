package org.xbib.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A scriptable sink. Like Mockito, but worse and requiring less configuration.
 */
class MockSink implements Sink {
    private final List<String> log = new ArrayList<String>();
    private final Map<Integer, IOException> callThrows = new LinkedHashMap<>();

    public void assertLog(String... messages) {
        assertEquals(Arrays.asList(messages), log);
    }

    public void assertLogContains(String message) {
        assertTrue(log.contains(message));
    }

    public void scheduleThrow(int call, IOException e) {
        callThrows.put(call, e);
    }

    private void throwIfScheduled() throws IOException {
        IOException exception = callThrows.get(log.size() - 1);
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        log.add("write(" + source + ", " + byteCount + ")");
        source.skip(byteCount);
        throwIfScheduled();
    }

    @Override
    public void flush() throws IOException {
        log.add("flush()");
        throwIfScheduled();
    }

    @Override
    public Timeout timeout() {
        log.add("timeout()");
        return Timeout.NONE;
    }

    @Override
    public void close() throws IOException {
        log.add("close()");
        throwIfScheduled();
    }
}
