package org.xbib.io.redis.event.connection;

import java.net.SocketAddress;

/**
 * Event for a connection deactivation.
 */
public class ConnectionDeactivatedEvent extends ConnectionEventSupport {
    public ConnectionDeactivatedEvent(SocketAddress local, SocketAddress remote) {
        super(local, remote);
    }
}
