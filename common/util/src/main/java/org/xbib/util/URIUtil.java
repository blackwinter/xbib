/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * URI utilities
 */
public final class URIUtil {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    private URIUtil() {
    }

    /**
     * Used to convert to hex.  We don't use Integer.toHexString, since
     * it converts to lower case (and the Sun docs pretty clearly specify
     * upper case here), and because it doesn't provide a leading 0.
     */
    private static final String hex = "0123456789ABCDEF";

    /**
     * This method adds a single key/value parameter to the query
     * string of a given URI. Existing keys will be overwritten.
     *
     * @param uri
     * @param key
     * @param value
     * @param encoding
     * @return uri
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public static URI addParameter(URI uri, String key, String value, Charset encoding)
            throws UnsupportedEncodingException, URISyntaxException {
        Map<String, String> m = parseQueryString(uri, encoding);
        m.put(key, value);
        String query = renderQueryString(m);
        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());
    }

    /**
     * This method adds a map of key/value parameters to the query
     * string of a given URI. Existing keys will be overwritten.
     *
     * @param uri
     * @param m
     * @param encoding
     * @return the URI
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public static URI addParameter(URI uri, Map<String, String> m, Charset encoding)
            throws URISyntaxException {
        Map<String, String> oldMap = parseQueryString(uri, encoding);
        oldMap.putAll(m);
        String query = renderQueryString(oldMap);
        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());
    }

    private static String concat(Collection<String> c) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> it = c.iterator(); it.hasNext(); ) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(it.next());
        }
        return sb.toString();
    }

    /**
     * Decodes an octet according to RFC 2396. According to this spec,
     * any characters outside the range 0x20 - 0x7E must be escaped because
     * they are not printable characters, except for any characters in the
     * fragment identifier. This method will translate any escaped characters
     * back to the original.
     *
     * @param s        The URI to decode.
     * @param encoding The encoding to decode into.
     * @return The decoded URI
     */
    public static String decode(String s, Charset encoding) {
        StringBuilder sb = new StringBuilder();
        boolean fragment = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
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
                        sb.append((char) ((Character.digit(s.charAt(++i), 16) << 4)
                                | Character.digit(s.charAt(++i), 16)));
                    } else {
                        sb.append(ch);
                    }
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return new String(sb.toString().getBytes(LATIN1), encoding);
    }

    /**
     * <p>Escape a string into URI syntax</p>
     * <p>This function applies the URI escaping rules defined in
     * section 2 of [RFC 2396], as amended by [RFC 2732], to the string
     * supplied as the first argument, which typically represents all or part
     * of a URI, URI reference or IRI. The effect of the function is to
     * replace any special character in the string by an escape sequence of
     * the form %xx%yy..., where xxyy... is the hexadecimal representation of
     * the octets used to represent the character in US-ASCII for characters
     * in the ASCII repertoire, and a different character encoding for
     * non-ASCII characters.</p>
     * <p>If the second argument is true, all characters are escaped
     * other than lower case letters a-z, upper case letters A-Z, digits 0-9,
     * and the characters referred to in [RFC 2396] as "marks": specifically,
     * "-" | "_" | "." | "!" | "~" | "" | "'" | "(" | ")". The "%" character
     * itself is escaped only if it is not followed by two hexadecimal digits
     * (that is, 0-9, a-f, and A-F).</p>
     * <p>[RFC 2396] does not define whether escaped URIs should use
     * lower case or upper case for hexadecimal digits. To ensure that escaped
     * URIs can be compared using string comparison functions, this function
     * must always use the upper-case letters A-F.</p>
     * <p>The character encoding used as the basis for determining the
     * octets depends on the setting of the second argument.</p>
     *
     * @param s        The String to convert
     * @param encoding The encoding to use for unsafe characters
     * @return The converted String
     * @throws UnsupportedEncodingException If the named encoding is not
     *                                      supported
     */
    public static String encode(String s, Charset encoding) {
        int length = s.length();
        int start = 0;
        int i = 0;
        StringBuilder result = new StringBuilder(length);
        while (true) {
            while ((i < length) && isSafe(s.charAt(i))) {
                i++;
            }
            // Safe character can just be added
            result.append(s.substring(start, i));
            // Are we done?
            if (i >= length) {
                return result.toString();
            } else if (s.charAt(i) == ' ') {
                result.append('+'); // Replace space char with plus symbol.
                i++;
            } else {
                // Get all unsafe characters
                start = i;
                char c;
                while ((i < length) && ((c = s.charAt(i)) != ' ') && !isSafe(c)) {
                    i++;
                }
                // Convert them to %XY encoded strings
                String unsafe = s.substring(start, i);
                byte[] bytes = unsafe.getBytes(encoding);
                for (int j = 0; j < bytes.length; j++) {
                    result.append('%');
                    int val = bytes[j];
                    result.append(hex.charAt((val & 0xf0) >> 4));
                    result.append(hex.charAt(val & 0x0f));
                }
            }
            start = i;
        }
    }

    /**
     * This method takes a String of an URI with an unescaped query
     * string and converts it into a URI with encoded query string format.
     * Useful for processing command line input.
     *
     * @param s the URI string
     * @return a string with the URL encoded data.
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public static URI encodeQueryString(String s) throws URISyntaxException {
        if (s == null) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        int questionmark = s.indexOf('?');
        if (questionmark > 0) {
            StringTokenizer st = new StringTokenizer(s.substring(questionmark + 1), "&");
            while (st.hasMoreTokens()) {
                String pair = st.nextToken();
                int pos = pair.indexOf('=');
                if (pos == -1) {
                    throw new URISyntaxException(s, "missing '='");
                }
                if (out.length() > 0) {
                    out.append("&");
                }
                out.append(pair.substring(0, pos + 1)).append(encode(pair.substring(pos + 1), UTF8));
            }
            return new URI(s.substring(0, questionmark + 1) + out.toString());
        } else {
            return new URI(s);
        }
    }

    /**
     * Get properties from URI
     *
     * @param uri the URI
     * @return the properties
     * @throws UnsupportedEncodingException
     */
    public static Properties getPropertiesFromURI(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        Properties properties = new Properties();
        properties.setProperty("uri", uri.toString());
        String scheme = uri.getScheme();
        // ensure scheme is not null
        scheme = scheme != null ? scheme : "";
        properties.setProperty("scheme", scheme);
        String type = "default";
        if (scheme.startsWith("jdbc:")) {
            int pos = scheme.substring(5).indexOf(':');
            type = (pos > 0) ? scheme.substring(5).substring(0, pos) : "default";
            if (type != null) {
                properties.setProperty("type", type);
            }
        }
        String host = uri.getHost();
        host = (host != null) ? host : "";
        int port = uri.getPort();
        String path = uri.getPath();
        String[] s = (path != null) ? path.split("/") : new String[]{};
        String cluster = (s.length > 1) ? s[1] : "";
        String collection = (s.length > 2) ? s[2] : "";
        String userInfo = uri.getUserInfo();
        String[] ui = (userInfo != null) ? userInfo.split(":") : new String[]{};
        properties.setProperty("username", (ui.length > 0) ? ui[0] : "");
        properties.setProperty("password", (ui.length > 1) ? ui[1] : "");
        Map<String, String> m = parseQueryString(uri);
        if ((m != null) && (m.size() > 0)) {
            properties.setProperty("requestkeys", concat(m.keySet()));
            properties.setProperty("requestquery", renderQueryString(m));
            properties.putAll(m);
        }
        if (host != null) {
            properties.setProperty("host", host);
        }
        if (port != 0) {
            properties.setProperty("port", Integer.toString(port));
        }
        if (path != null) {
            properties.setProperty("path", path);
        }
        if (uri.getFragment() != null) {
            properties.setProperty("fragment", uri.getFragment());
        }
        if (cluster != null) {
            properties.setProperty("cluster", cluster);
            properties.setProperty("index", cluster);
        }
        if (collection != null) {
            properties.setProperty("collection", collection);
            properties.setProperty("type", collection);
        }
        return properties;
    }

    /**
     * Returns true if the given char is
     * either a uppercase or lowercase letter from 'a' till 'z', or a digit
     * froim '0' till '9', or one of the characters '-', '_', '.' or ''. Such
     * 'safe' character don't have to be url encoded.
     *
     * @param c
     * @return true or false
     */
    private static boolean isSafe(char c) {
        return (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))
                || ((c >= '0') && (c <= '9')) || (c == '-') || (c == '_') || (c == '.') || (c == '*'));
    }

    /**
     * This method parses a query string and returns a map of decoded
     * request parameters. We do not rely on java.net.URI because it does not
     * decode plus characters. The encoding is UTF-8.
     *
     * @param uri the URI to examine for request parameters
     * @return a map
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> parseQueryString(URI uri) {
        return parseQueryString(uri, UTF8);
    }

    /**
     * This method parses a query string and returns a map of decoded
     * request parameters. We do not rely on java.net.URI because it does not
     * decode plus characters.
     *
     * @param uri      the URI to examine for request parameters
     * @param encoding the encoding
     * @return a Map
     * @throws UnsupportedEncodingException
     * @throws IllegalArgumentException
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
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> parseQueryString(URI uri, Charset encoding, ParameterListener listener) {
        Map<String, String> m = new HashMap();
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
     * This method takes a Map of key/value elements and converts it
     * into a URL encoded querystring format.
     *
     * @param m a map of key/value arrays.
     * @return a string with the URL encoded data.
     * @throws UnsupportedEncodingException
     */
    public static String renderQueryString(Map<String, String> m) {
        String key = null;
        String value = null;
        StringBuilder out = new StringBuilder();
        Charset encoding = m.containsKey("encoding") ? Charset.forName(m.get("encoding")) : UTF8;
        for (Iterator<Map.Entry<String, String>> iter = m.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, String> me = iter.next();
            key = me.getKey();
            String o = me.getValue();
            value = o != null ? encode(o, encoding) : null;
            if (key != null) {
                if (out.length() > 0) {
                    out.append("&");
                }
                out.append(key);
                if ((value != null) && (value.length() > 0)) {
                    out.append("=").append(value);
                }
            }
        }
        return out.toString();
    }

    /**
     * This method takes a Map of key/value elements and generates a
     * string for queries.
     *
     * @param m a map of key/value arrays.
     * @return a string
     */
    public static String renderRawQueryString(Map<String, String> m) {
        String key;
        String value;
        StringBuilder out = new StringBuilder();
        for (Iterator<Map.Entry<String, String>> iter = m.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, String> me = iter.next();
            key = me.getKey();
            value = me.getValue();
            if ((key != null) && (value != null) && (value.length() > 0)) {
                if (out.length() > 0) {
                    out.append("&");
                }
                out.append(key).append("=").append(value);
            }
        }
        return out.toString();
    }

    public interface ParameterListener {
        public void received(String k, String v);
    }
}
