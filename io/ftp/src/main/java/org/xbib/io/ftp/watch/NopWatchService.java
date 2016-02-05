package org.xbib.io.ftp.watch;

import java.io.IOException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

public enum NopWatchService implements WatchService {
    INSTANCE;

    @Override
    public void close() throws IOException {
    }

    @Override
    public WatchKey poll() {
        return null;
    }

    @Override
    public WatchKey poll(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        unit.sleep(timeout);
        return null;
    }

    @Override
    public WatchKey take()
            throws InterruptedException {
        return null;
    }
}
