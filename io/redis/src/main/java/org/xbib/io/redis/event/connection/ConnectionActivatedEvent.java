package org.xbib.io.redis.event.connection;

import org.xbib.io.redis.ClientOptions;

import java.net.SocketAddress;

/**
 * Event for a connection activation (after SSL-handshake, {@link ClientOptions#isPingBeforeActivateConnection() PING before
 * activation}, and buffered command replay).
 */
public class ConnectionActivatedEvent extends ConnectionEventSupport {
    public ConnectionActivatedEvent(SocketAddress local, SocketAddress remote) {
        super(local, remote);
    }
}
