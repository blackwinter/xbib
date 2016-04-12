package org.xbib.openurl.entities;

import org.xbib.openurl.descriptors.Descriptor;

/**
 * ReferringEntity is a fancy word meaning "where". In other
 * words, <em>where</em> did the client issue the request?
 * Most Transports will want to pull this out the HTTP "Referer"
 * header.
 */
public interface ReferringEntity extends Entity<Descriptor> {
}
