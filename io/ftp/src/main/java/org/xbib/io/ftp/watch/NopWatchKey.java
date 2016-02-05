package org.xbib.io.ftp.watch;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.Watchable;
import java.util.Collections;
import java.util.List;

public final class NopWatchKey implements WatchKey {
    private boolean valid = true;

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public List<WatchEvent<?>> pollEvents() {
        return Collections.emptyList();
    }

    @Override
    public boolean reset() {
        final boolean ret = valid;
        valid = false;
        return ret;
    }

    @Override
    public void cancel() {
        valid = false;
    }

    @Override
    public Watchable watchable() {
        return NopWatchable.INSTANCE;
    }
}
