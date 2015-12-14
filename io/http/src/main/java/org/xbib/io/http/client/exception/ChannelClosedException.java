package org.xbib.io.http.client.exception;

import java.io.IOException;

import static org.xbib.io.http.client.util.MiscUtils.trimStackTrace;

@SuppressWarnings("serial")
public final class ChannelClosedException extends IOException {

    public static final ChannelClosedException INSTANCE = trimStackTrace(new ChannelClosedException());

    private ChannelClosedException() {
        super("Channel closed");
    }
}
