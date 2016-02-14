package org.xbib.util;

import java.nio.charset.Charset;
import java.util.Map;

public class URIFormatter {

    /**
     * This method takes a Map of key/value elements and converts it
     * into a URL encoded querystring format.
     *
     * @param m a map of key/value arrays
     * @param encoding the charset
     * @return a string with the URL encoded data
     */
    public static String renderQueryString(Map<String, String> m, Charset encoding) {
        String key;
        String value;
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> me : m.entrySet()) {
            key = me.getKey();
            value = encode(me.getValue(), encoding);
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
     */
    public static String encode(String s, Charset encoding) {
        if (s == null) {
            return null;
        }
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
                for (byte aByte : bytes) {
                    result.append('%');
                    result.append(hex.charAt(((int) aByte & 0xf0) >> 4));
                    result.append(hex.charAt((int) aByte & 0x0f));
                }
            }
            start = i;
        }
    }

    /**
     * Returns true if the given char is
     * either a uppercase or lowercase letter from 'a' till 'z', or a digit
     * froim '0' till '9', or one of the characters '-', '_', '.' or ''. Such
     * 'safe' character don't have to be url encoded.
     *
     * @param c the character
     * @return true or false
     */
    private static boolean isSafe(char c) {
        return (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))
                || ((c >= '0') && (c <= '9')) || (c == '-') || (c == '_') || (c == '.') || (c == '*'));
    }

    /**
     * Used to convert to hex.  We don't use Integer.toHexString, since
     * it converts to lower case (and the Sun docs pretty clearly specify
     * upper case here), and because it doesn't provide a leading 0.
     */
    private static final String hex = "0123456789ABCDEF";


}
