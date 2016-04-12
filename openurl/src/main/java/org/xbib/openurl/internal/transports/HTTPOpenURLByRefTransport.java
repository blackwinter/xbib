package org.xbib.openurl.internal.transports;

import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequest;
import org.xbib.openurl.Transport;
import org.xbib.openurl.config.OpenURLConfig;

import java.net.URI;
import java.util.Map;

/**
 * A By-Reference OpenURL Transport transports the network location
 * of a ContextObject Representation. The Representation itself is not
 * transported, but resides at a network location.
 *
 * Depending on the constraints of the ContextObject Format, the
 * Representation stored at a network location may contain the description
 * of one or more ContextObjects. The By-Reference OpenURL Transport may
 * be used for a ContextObject Representation that conforms to any
 * registered ContextObject Format.
 *
 * The By-Reference OpenURL Transport uses the HTTP network protocol
 * or its secure sibling, HTTPS. The Registry Identifiers for these
 * Transports are:
 * <ul>
 * <li>By-Reference OpenURL Transport over HTTP
 * info:ofi/tsp:http:openurl-by-ref  </li>
 * <li>By-Reference OpenURL Transport over HTTPS
 * info:ofi/tsp:https:openurl-by-ref </li>
 *
 * This Section describes both Transports, which are identical except
 * for their use of HTTP or HTTPS as the respective network protocol.
 *
 * For each transportation via the By-Reference OpenURL Transport,
 * a base URL specifies the "Internet host and port, and path"
 * of the target of the transportation, an HTTP(S)-based service
 * called a Resolver.
 *
 * A By-Reference OpenURL Transport may convey the network location
 * of a ContextObject Representation via HTTP(S) GET or HTTP(S) POST.
 */
public class HTTPOpenURLByRefTransport implements Transport {
    /**
     * Construct an HTTP OpenURL By-Ref Transport object
     *
     * @param config
     */
    public HTTPOpenURLByRefTransport(OpenURLConfig config) {
    }

    @Override
    public URI getTransportID() {
        return Transport.HTTP_OPENURL_BY_REF_URI;
    }

    @Override
    public OpenURLRequest toOpenURLRequest(Map<String, String[]> req, boolean isSecure)
            throws OpenURLException {
        return null;
    }
}
