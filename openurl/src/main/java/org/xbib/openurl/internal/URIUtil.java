package org.xbib.openurl.internal;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Helper class for serializing ContextObjects into an KEV format conforming to
 * ANSI/NISO Z39.88-2004, Part 2 "The KEV Context Object Format".
 */
public final class URIUtil {
    /**
     * Decodes an octet according to RFC 2396. According to this spec,
     * any characters outside the range 0x20 - 0x7E must be escaped because
     * they are not printable characters, except for any characters in the
     * fragment identifier. This method will translate any escaped characters
     * back to the original.
     *
     * @param octet    the octet to decode.
     * @param encoding The encoding to decode into.
     * @return The decoded URI as string
     */
    public static String decode(String octet, String encoding) {
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
        try {
            return new String(sb.toString().getBytes("ISO-8859-1"), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new Error("encoding " + encoding + " not supported");
        }
    }

    /**
     * This method parses a query string and returns a map of decoded
     * request parameters. We do not rely on java.net.URI because it does not
     * decode 'plus' (+) characters. The default encoding is UTF-8.
     *
     * @param uri the URI to examine for request parameters
     * @return map of query parameters
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String[]> parseQueryString(URI uri) throws UnsupportedEncodingException {
        return parseQueryString(uri, "UTF-8");
    }

    public static Map<String, String[]> parseQueryString(String value) throws UnsupportedEncodingException {
        return parseQueryString(value, "UTF-8");
    }

    /**
     * This method parses a query string and returns a map of decoded
     * request parameters. We do not rely on java.net.URI because it does not
     * decode 'plus' (+) characters.
     *
     * @param uri      the URI to examine for request parameters
     * @param encoding the encoding
     * @return map of query parameters
     * @throws UnsupportedEncodingException
     * @throws IllegalArgumentException
     */
    public static Map<String, String[]> parseQueryString(URI uri, String encoding)
            throws UnsupportedEncodingException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        if (uri.getRawQuery() == null) {
            return null;
        }
        return parseQueryString(uri.getRawQuery(), encoding);
    }

    public static Map<String, String[]> parseQueryString(String value, String encoding)
            throws UnsupportedEncodingException {
        HashMap<String, String[]> m = new HashMap();
        StringTokenizer st = new StringTokenizer(value, "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int pos = pair.indexOf('=');
            if (pos < 0) {
                m.put(pair, null);
            } else {
                String key = pair.substring(0, pos);
                String val = decode(pair.substring(pos + 1, pair.length()), encoding);
                String[] s = m.get(key);
                m.put(key, s == null ? new String[]{val} : append(s, val));
            }
        }
        return m;
    }

    private static String[] append(String[] a, String b) {
        String[] c = new String[a.length + 1];
        System.arraycopy(a, 0, c, 0, a.length);
        c[a.length + 1] = b;
        return c;
    }

    /**
     * This method adds a single key/value parameter to the
     * string of a given URI. An existing key will be overwritten.
     *
     * @param uri      the URI
     * @param key      the key
     * @param value    the value
     * @param encoding the character set encoding
     * @return the new URI
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public URI addParameter(URI uri, String key, String value, String encoding)
            throws UnsupportedEncodingException, URISyntaxException {
        Map m = parseQueryString(uri, encoding);
        m.put(key, value);
        String query = renderQueryString(m);
        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());
    }

    /**
     * This method adds a map of key/value parameters to the query
     * string of a given URI. Existing keys will be overwritten.
     *
     * @param uri      the URI
     * @param map      the query parameter map
     * @param encoding the character encoding
     * @return the new URI
     * @throws UnsupportedEncodingException DOCUMENT ME!
     * @throws URISyntaxException           DOCUMENT ME!
     */
    public URI addParameter(URI uri, Map map, String encoding)
            throws UnsupportedEncodingException, URISyntaxException {
        Map currentMap = parseQueryString(uri, encoding);
        currentMap.putAll(map);
        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), renderQueryString(currentMap), uri.getFragment());
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
     * @param s        the String to convert
     * @param encoding The encoding to use for unsafe characters
     * @return The converted String
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    public String encode(String s, String encoding)
            throws UnsupportedEncodingException {
        int length = s.length();
        int start = 0;
        int i = 0;
        StringBuilder result = new StringBuilder(length);
        while (true) {
            while ((i < length) && isSafe(s.charAt(i))) {
                i++;
            }
            result.append(s.substring(start, i));
            if (i >= length) {
                return result.toString();
            } else if (s.charAt(i) == ' ') {
                result.append('+');
                i++;
            } else {
                start = i;
                char c;
                while ((i < length) && ((c = s.charAt(i)) != ' ') && !isSafe(c)) {
                    i++;
                }
                String unsafe = s.substring(start, i);
                byte[] bytes = unsafe.getBytes(encoding);
                for (int j = 0; j < bytes.length; j++) {
                    result.append('%');
                    int codepoint = bytes[j];
                    result.append((char) ((Character.digit(codepoint, 16) << 4)
                            | Character.digit(codepoint, 16)));
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
    public URI encodeQueryString(String s)
            throws UnsupportedEncodingException, URISyntaxException {
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
                out.append(pair.substring(0, pos + 1))
                        .append(encode(pair.substring(pos + 1), "UTF-8"));
            }
            return new URI(s.substring(0, questionmark + 1) + out.toString());
        } else {
            return new URI(s);
        }
    }

    /**
     * Returns true if the given character is
     * either a uppercase or lowercase letter from 'a' till 'z', or a digit
     * froim '0' till '9', or one of the characters '-', '_', '.' or ''. Such
     * 'safe' character don't have to be encoded.
     *
     * @param c character to test
     * @return true if character is safe
     */
    private boolean isSafe(char c) {
        return (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))
                || ((c >= '0') && (c <= '9')) || (c == '-') || (c == '_') || (c == '.') || (c == '*'));
    }

    /**
     * This method takes a Map of key/value string  and converts it
     * into a URI RFC 2936 encoded query format.
     *
     * @param map a map of key/value arrays.
     * @return a string with the URL encoded data.
     * @throws UnsupportedEncodingException
     */
    public String renderQueryString(Map<String, String[]> map) throws UnsupportedEncodingException {
        return renderQueryString(map, "UTF-8");
    }

    public String renderQueryString(Map<String, String[]> map, String encoding) throws UnsupportedEncodingException {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String[]> me : map.entrySet()) {
            String key = me.getKey();
            String[] values = me.getValue();
            for (String value : values) {
                value = (value != null) ? encode(value, encoding) : null;
                if ((key != null) && (value != null)) {
                    if (out.length() > 0) {
                        out.append("&");
                    }
                    out.append(key).append("=").append(value);
                }
            }
        }
        return out.toString();
    }

    /**
     * This method takes a Map of key/value elements and generates a
     * string for queries.
     *
     * @param map a map of key/value strings.
     * @return a string
     */
    public String renderRawQueryString(Map<String, String> map) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> me : map.entrySet()) {
            String key = me.getKey();
            String value = me.getValue();
            if ((key != null) && (value != null)) {
                if (out.length() > 0) {
                    out.append("&");
                }
                out.append(key).append("=").append(value);
            }
        }
        return out.toString();
    }

}
