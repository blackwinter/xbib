package org.xbib.openurl.internal.transports;

import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequest;
import org.xbib.openurl.Transport;
import org.xbib.openurl.config.OpenURLConfig;

import java.net.URI;
import java.util.Map;

/**
 * An Inline OpenURL Transport transports exactly one KEV ContextObject
 * Representation as part of the query string used in an HTTP(S) GET request
 * or in the message body of an HTTP(S) POST.
 *
 * This differs from the By-Value OpenURL Transport, where the
 * KEV ContextObject Representation is the value associated with the
 * url_ctx_val key.
 *
 * The Inline OpenURL Transport strongly resembles OpenURL 0.1.
 *
 * The Inline OpenURL Transport may be used only for the transportation of
 * one, and only one, KEV ContextObject Representation.
 *
 * It must not be used for the transportation of ContextObject Representations
 * that conform to any other ContextObject Format.
 *
 * The Inline OpenURL Transport uses the HTTP network protocol or its
 * secure sibling, HTTPS.
 *
 * The Registry Identifiers for these Transports are:
 * Inline OpenURL Transport over HTTP info:ofi/tsp:http:openurl-inline
 * Inline OpenURL Transport over HTTPS info:ofi/tsp:https:openurl-inline
 *
 * For each transportation of a KEV ContextObject Representation via the
 * Inline OpenURL Transport, a base URL specifies the
 * "Internet host and port, and path" of the target of the transportation,
 * an HTTP(S)-based service called a Resolver.
 *
 * An Inline OpenURL Transport conveys exactly one KEV ContextObject
 * Representation via HTTP(S) GET and HTTP(S) POST.
 *
 * The KEV ContextObject Format supports Character Encodings other than
 * the default UTF-8 encoded Unicode. As a result, it is possible to submit
 * KEV ContextObjects Representations via HTML forms.
 *
 * The Character Encoding is declared by assigning a value to the ctx_enc key.
 * This value must be a Registry Identifier of a registered Character Encoding.
 */
public class HTTPSOpenURLInlineTransport extends HTTPOpenURLInlineTransport {

    /**
     * Construct an HTTPS OpenURL Inline Transport object
     *
     * @param config
     */
    public HTTPSOpenURLInlineTransport(OpenURLConfig config) {
        super(config);
    }

    @Override
    public URI getTransportID() {
        return Transport.HTTPS_OPENURL_INLINE_URI;
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
