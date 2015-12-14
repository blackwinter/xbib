package org.xbib.io.http.client.exception;

import java.io.IOException;

import static org.xbib.io.http.client.util.MiscUtils.trimStackTrace;

@SuppressWarnings("serial")
public class PoolAlreadyClosedException extends IOException {

    public static final PoolAlreadyClosedException INSTANCE = trimStackTrace(new PoolAlreadyClosedException());

    private PoolAlreadyClosedException() {
        super("Pool is already closed");
    }
}
