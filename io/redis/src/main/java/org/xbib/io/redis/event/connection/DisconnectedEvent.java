package org.xbib.io.redis.event.connection;

import java.net.SocketAddress;

/**
 * Event for a disconnect on TCP-level.
 */
public class DisconnectedEvent extends ConnectionEventSupport {
    public DisconnectedEvent(SocketAddress local, SocketAddress remote) {
        super(local, remote);
    }
}
