package org.xbib.io.redis;

import java.net.SocketAddress;

/**
 * Connection identifier. A connection identifier consists of the {@link #localAddress()} and the {@link #remoteAddress()}.
 */
public interface ConnectionId {

    /**
     * Returns the local address.
     *
     * @return the local address
     */
    SocketAddress localAddress();

    /**
     * Returns the remote address.
     *
     * @return the remote address
     */
    SocketAddress remoteAddress();
}
