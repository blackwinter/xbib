package org.xbib.io.http.client.exception;

import java.io.IOException;

@SuppressWarnings("serial")
public class TooManyConnectionsPerHostException extends IOException {

    public TooManyConnectionsPerHostException(int max) {
        super("Too many connections: " + max);
    }
}
