package org.xbib.openurl.internal.transports;

import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequest;
import org.xbib.openurl.Transport;
import org.xbib.openurl.config.OpenURLConfig;

import java.net.URI;
import java.util.Map;

/**
 * A By-Value OpenURL Transport transports the actual ContextObject
 * Representation, not its network location. Depending on the constraints
 * of the ContextObject Format, the Representation may contain the
 * description of one or more ContextObjects. The By-Value OpenURL Transport
 * may transport a ContextObject Representation that conforms to any
 * registered ContextObject Format.
 *
 * The By-Value OpenURL Transport uses the HTTP network protocol or its
 * secure sibling, HTTPS. The Registry Identifiers for these Transports are:
 * <ul>
 * <li>By-Value OpenURL Transport over HTTP info:ofi/tsp:http:openurl-by-val </li>
 * <li>By-Value OpenURL Transport over HTTPS info:ofi/tsp:https:openurl-by-val </li>
 * </ul>
 *
 * For each transportation via the By-Value OpenURL Transport, a base URL
 * specifies the “Internet host and port, and path” of the target of the
 * transportation, an HTTP(S)-based service called a Resolver.
 *
 * A By-Value OpenURL Transport may convey a ContextObject Representation
 * via HTTP(S) GET or HTTP(S) POST.
 */
public class HTTPSOpenURLByValTransport extends HTTPOpenURLByValTransport {

    /**
     * Construct an HTTPS OpenURL By-Val Transport object
     *
     * @param config
     */
    public HTTPSOpenURLByValTransport(OpenURLConfig config) {
        super(config);
    }

    @Override
    public URI getTransportID() {
        return Transport.HTTPS_OPENURL_BY_VAL_URI;
    }

    @Override
    public OpenURLRequest toOpenURLRequest(Map<String, String[]> req, boolean isSecure)
            throws OpenURLException {
        if (!isSecure) {
            throw new OpenURLException("this transport is not for insecure requests");
        }
        return super.toOpenURLRequest(req, !isSecure);
    }
}
