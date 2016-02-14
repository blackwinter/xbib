package org.xbib.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class URIBuilder {

    private URI uri;
    private String scheme;
    private String authority;
    private String path;
    private String fragment;
    private Map<String,String> params;

    public URIBuilder() {
        this.params = new LinkedHashMap<>();
    }

    public URIBuilder(String base) {
        // TODO see IRI.builder
        this(URI.create(base));
    }

    public URIBuilder(URI base) {
        this.uri = base;
        this.scheme = uri.getScheme();
        this.authority = uri.getAuthority();
        this.path = uri.getPath();
        this.fragment = uri.getFragment();
        this.params = parseQueryString(uri);
    }

    public URIBuilder(URI base, Charset encoding) {
        this.uri = base;
        this.params = parseQueryString(uri, encoding);
    }

    public URIBuilder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public URIBuilder authority(String authority) {
        this.authority = authority;
        return this;
    }

    public URIBuilder path(String path) {
        this.path = path;
        return this;
    }

    public URIBuilder fragment(String fragment) {
        this.fragment = fragment;
        return this;
    }
    /**
     * This method adds a single key/value parameter to the query
     * string of a given URI. Existing keys will be overwritten.
     *
     * @param key      the key
     * @param value    the value
     */
    public URIBuilder addParameter(String key, String value) {
        params.put(key, value);
        return this;
    }

    /**
     * This method adds a single key/value parameter to the query
     * string of a given URI, URI-escaped with the given encoding.
     * Existing keys will be overwritten.
     *
     * @param key      the key
     * @param value    the value
     * @param encoding the encoding
     */
    public URIBuilder addParameter(String key, String value, Charset encoding) {
        params.put(key, URIFormatter.encode(value, encoding));
        return this;
    }

    public URI build() {
        try {
            return new URI(scheme, authority, path, URIFormatter.renderQueryString(params), fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public URI build(Charset encoding) {
        try {
            return new URI(scheme, authority, path, URIFormatter.renderQueryString(params, encoding), fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * This method parses a query string and returns a map of decoded
     * request parameters. We do not rely on java.net.URI because it does not
     * decode plus characters. The encoding is UTF-8.
     *
     * @param uri the URI to examine for request parameters
     * @return a map
     */
    public static Map<String, String> parseQueryString(URI uri) {
        return parseQueryString(uri, StandardCharsets.UTF_8);
    }

    /**
     * This method parses a query string and returns a map of decoded
     * request parameters. We do not rely on java.net.URI because it does not
     * decode plus characters.
     *
     * @param uri      the URI to examine for request parameters
     * @param encoding the encoding
     * @return a Map
     */
    public static Map<String, String> parseQueryString(URI uri, Charset encoding) {
        return parseQueryString(uri, encoding, null);
    }

    /**
     * This method parses a query string and returns a map of decoded
     * request parameters. We do not rely on java.net.URI because it does not
     * decode plus characters. A listener can process the parameters in order.
     *
     * @param uri      the URI to examine for request parameters
     * @param encoding the encoding
     * @param listener a listner for processing the URI parameters in order, or null
     * @return a Map of parameters
     */
    public static Map<String, String> parseQueryString(URI uri, Charset encoding, ParameterListener listener) {
        Map<String, String> m = new HashMap<>();
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        if (uri.getRawQuery() == null) {
            return m;
        }
        // we use getRawQuery because we do our decoding by ourselves
        StringTokenizer st = new StringTokenizer(uri.getRawQuery(), "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            String k;
            String v;
            int pos = pair.indexOf('=');
            if (pos < 0) {
                k = pair;
                v = null;
            } else {
                k = pair.substring(0, pos);
                v = decode(pair.substring(pos + 1, pair.length()), encoding);
            }
            m.put(k, v);
            if (listener != null) {
                listener.received(k, v);
            }
        }
        return m;
    }

    /**
     * Decodes an octet according to RFC 2396. According to this spec,
     * any characters outside the range 0x20 - 0x7E must be escaped because
     * they are not printable characters, except for any characters in the
     * fragment identifier. This method will translate any escaped characters
     * back to the original.
     *
     * @param octet      the octet to decode
     * @param encoding the encoding to decode into
     * @return The decoded URI
     */
    public static String decode(String octet, Charset encoding) {
        StringBuilder sb = new StringBuilder();
        boolean fragment = false;
        for (int i = 0; i < octet.length(); i++) {
            char ch = octet.charAt(i);
            switch (ch) {
                case '+':
                    sb.append(' ');
                    break;
                case '#':
                    sb.append(ch);
                    fragment = true;
                    break;
                case '%':
                    if (!fragment) {
                        // fast hex decode
                        sb.append((char) ((Character.digit(octet.charAt(++i), 16) << 4)
                                | Character.digit(octet.charAt(++i), 16)));
                    } else {
                        sb.append(ch);
                    }
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return new String(sb.toString().getBytes(StandardCharsets.ISO_8859_1), encoding);
    }

    public interface ParameterListener {
        void received(String k, String v);
    }

}
