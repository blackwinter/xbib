package org.xbib.io.http.client.exception;

import java.io.IOException;

@SuppressWarnings("serial")
public class TooManyConnectionsException extends IOException {

    public TooManyConnectionsException(int max) {
        super("Too many connections: " + max);
    }
}
