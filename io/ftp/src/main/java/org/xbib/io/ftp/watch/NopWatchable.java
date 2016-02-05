package org.xbib.io.ftp.watch;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;

public enum NopWatchable implements Watchable {
    INSTANCE;

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events,
                             WatchEvent.Modifier... modifiers)
            throws IOException {
        return new NopWatchKey();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events)
            throws IOException {
        return new NopWatchKey();
    }
}
