package org.xbib.openurl.entities;

import org.xbib.openurl.descriptors.Descriptor;

/**
 * The Entity at which a request for service is targeted.
 *
 * You will probably want to use one of the Transports
 * described in the OpenURL spec that are ready to accommodate it:
 * openurl-by-ref, openurl-by-val, or openurl-inline.
 *
 * For each transportation via an OpenURL Transport,
 * a base URL specifies the "Internet host and port, and path"
 * of the target of the transportation, an HTTP(S)-based service
 * called a Resolver.
 */
public interface Resolver<D extends Descriptor> extends Entity<Descriptor> {
}
