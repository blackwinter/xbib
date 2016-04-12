package org.xbib.openurl.internal.transports;

import org.xbib.openurl.Format;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequest;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.Transport;
import org.xbib.openurl.config.OpenURLConfig;
import org.xbib.openurl.internal.parsers.KEVParser;
import org.xbib.openurl.internal.parsers.XMLParser;

import java.io.ByteArrayInputStream;
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
public class HTTPOpenURLByValTransport implements Transport {

    private OpenURLConfig config;

    /**
     * Construct an HTTP OpenURL By-Value Transport object
     *
     * @param config
     */
    public HTTPOpenURLByValTransport(OpenURLConfig config) {
        this.config = config;
    }

    public URI getTransportID() {
        return HTTP_OPENURL_BY_VAL_URI;
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
            String[] vals = req.get("url_ctx_val");
            String val = vals != null && vals.length == 1 ? vals[0] : "";
            String[] fmts = req.get("url_ctx_fmt");
            String fmt = fmts != null && fmts.length == 1 ? fmts[0] : "";
            if (fmt.startsWith(Format.FORMAT_XML_URI.toString())) {
                XMLParser parser = new XMLParser(config);
                return processor.createOpenURLRequest(parser.createContextObject(URI.create(fmt),
                        new ByteArrayInputStream(val.getBytes())));
            } else if (fmt.startsWith(Format.FORMAT_KEV_MATRIX_URI.toString())) {
                KEVParser parser = new KEVParser(config);
                return processor.createOpenURLRequest(parser.createContextObject(URI.create(fmt), val));
            }
            return null;
        } catch (Exception e) {
            throw new OpenURLException(e.getMessage(), e);
        }
    }
}
