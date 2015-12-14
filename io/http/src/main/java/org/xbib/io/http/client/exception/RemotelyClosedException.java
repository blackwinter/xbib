package org.xbib.io.http.client.exception;

import java.io.IOException;

import static org.xbib.io.http.client.util.MiscUtils.trimStackTrace;

@SuppressWarnings("serial")
public final class RemotelyClosedException extends IOException {

    public static final RemotelyClosedException INSTANCE = trimStackTrace(new RemotelyClosedException());

    public RemotelyClosedException() {
        super("Remotely closed");
    }
}
