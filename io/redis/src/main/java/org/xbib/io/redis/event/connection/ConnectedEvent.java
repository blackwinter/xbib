package org.xbib.io.redis.event.connection;

import java.net.SocketAddress;

/**
 * Event for a established TCP-level connection.
 */
public class ConnectedEvent extends ConnectionEventSupport {
    public ConnectedEvent(SocketAddress local, SocketAddress remote) {
        super(local, remote);
    }
}
