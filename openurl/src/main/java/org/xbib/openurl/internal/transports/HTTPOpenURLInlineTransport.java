package org.xbib.openurl.internal.transports;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.Format;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequest;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.Transport;
import org.xbib.openurl.config.OpenURLConfig;
import org.xbib.openurl.internal.parsers.KEVParser;

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
public class HTTPOpenURLInlineTransport implements Transport {

    private OpenURLConfig config;

    /**
     * Construct an HTTP OpenURL Inline Transport object
     *
     * @param config
     */
    public HTTPOpenURLInlineTransport(OpenURLConfig config) {
        this.config = config;
    }

    public URI getTransportID() {
        return HTTP_OPENURL_INLINE_URI;
    }

    public OpenURLRequest toOpenURLRequest(Map<String, String[]> req, boolean isSecure)
            throws OpenURLException {
        try {
            OpenURLRequestProcessor processor = config.getProcessor();
            if (isSecure) {
                throw new OpenURLException("this transport is not for secure requests");
            }
            if (req.containsKey("url_ctx_ref")) {
                throw new OpenURLException("this transport is not for by-ref requests");
            }
            if (req.containsKey("url_ctx_val")) {
                throw new OpenURLException("this transport is not for by-val requests");
            }
            String[] vers = req.get("url_ver");
            String ver = vers != null ? vers[0] : null;
            if (!(ContextObject.VERSION.equals(ver))) {
                throw new OpenURLException("this version is not for us: " + ver);
            }
            String[] url_tims = req.get("url_tim");
            String url_tim = url_tims != null && url_tims.length == 1 ? url_tims[0] : null;
            ContextObject contextObject = null;
            String[] fmts = req.get("url_ctx_fmt");
            String fmt = fmts != null && fmts.length == 1 ? fmts[0] : null;
            if (fmt == null) {
                fmt = Format.FORMAT_KEV_MATRIX_CONSTRAINT_URI.toString();
            }
            if (Format.FORMAT_KEV_MATRIX_CONSTRAINT_URI.toString().equals(fmt)) {
                KEVParser kevp = new KEVParser(config);
                contextObject = kevp.parseContextObject(req);
                return processor.createOpenURLRequest(contextObject);
            }
            throw new OpenURLException("invalid context format: " + fmt);
        } catch (Exception e) {
            throw new OpenURLException(e.getMessage(), e);
        }
    }
}
