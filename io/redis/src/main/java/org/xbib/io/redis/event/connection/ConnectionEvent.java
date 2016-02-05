package org.xbib.io.redis.event.connection;

import org.xbib.io.redis.ConnectionId;
import org.xbib.io.redis.event.Event;

/**
 * Interface for Connection-related events
 */
public interface ConnectionEvent extends ConnectionId, Event {

}
