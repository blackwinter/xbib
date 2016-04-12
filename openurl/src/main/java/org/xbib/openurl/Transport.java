package org.xbib.openurl;

import java.net.URI;
import java.util.Map;

/**
 * A Transport is a method by which a ContextObject Representation may
 * be transported over a network. A Transport is the combination of a
 * network protocol and a method by which this network protocol transports
 * a ContextObject Representation.
 *
 * Transports must be registered before use in an Application.
 *
 * Communities may use Transports that are already in the Registry,
 * or they may register additional Transports as needed.
 *
 * Upon registration, a Transport is assigned a Registry Identifier,
 * formed by concatenating four character strings:
 * <ul>
 * <li>info:ofi/, which represents the namespace under the info scheme
 * reserved for Registry Identifiers </li>
 * <li>tsp:, a character string that uniquely identifies a core component
 * of the OpenURL Framework, which for Transports must be tsp: </li>
 * <li>a character string that is assigned on registration and identifies
 * the network protocol used by the Transport followed by a colon
 * character (‘:’) </li>
 * <li>a character string that is assigned on registration and
 * identifies the actual Transport.  </li>
 * </ul>
 * Registry Identifiers of Transports are used primarily to support
 * Registry management and to identify Transports in Community Profiles.
 * In typical use, Registry Identifiers of Transports do not show up in
 * Representations of ContextObjects or their Entities.
 */
public interface Transport {

    URI HTTP_OPENURL_INLINE_URI = URI.create("info:ofi/tsp:http:openurl-inline");
    URI HTTP_OPENURL_BY_VAL_URI = URI.create("info:ofi/tsp:http:openurl-by-val");
    URI HTTP_OPENURL_BY_REF_URI = URI.create("info:ofi/tsp:http:openurl-by-ref");
    URI HTTPS_OPENURL_INLINE_URI = URI.create("info:ofi/tsp:https:openurl-inline");
    URI HTTPS_OPENURL_BY_VAL_URI = URI.create("info:ofi/tsp:https:openurl-by-val");
    URI HTTPS_OPENURL_BY_REF_URI = URI.create("info:ofi/tsp:https:openurl-by-ref");

    /**
     * @return a Transport identifier from the OpenURL Registry
     */
    URI getTransportID();

    /**
     * Transforms a request into an equivalent
     * OpenURLRequest representation.
     *
     * @param map      the request as it was received from the client
     * @param isSecure if the request is secure
     * @return the entire request represented in OpenURL
     * @throws OpenURLException if map can not be transformed to an OpenURL request
     */
    OpenURLRequest toOpenURLRequest(Map<String, String[]> map, boolean isSecure)
            throws OpenURLException;
}