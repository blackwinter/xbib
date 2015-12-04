
package org.xbib.graphics.barcode.tools;

public class MessageUtil {

    /**
     * Un-escapes escaped Unicode characters in a message. This is used to support characters
     * not encodable in XML, such as the RS or GS characters.
     * @param message the message
     * @return the processed message
     */
    public static String unescapeUnicode(String message) {
       StringBuffer sb = new StringBuffer();
       if (message == null) {
           return null;
       }
       int sz = message.length();
       StringBuffer unicode = new StringBuffer(4);
       boolean hadSlash = false;
       boolean inUnicode = false;
       for (int i = 0; i < sz; i++) {
           char ch = message.charAt(i);
           if (inUnicode) {
               unicode.append(ch);
               if (unicode.length() == 4) {
                   try {
                       int value = Integer.parseInt(unicode.toString(), 16);
                       sb.append((char)value);
                       unicode.setLength(0);
                       inUnicode = false;
                       hadSlash = false;
                   } catch (NumberFormatException nfe) {
                       throw new java.lang.IllegalArgumentException(
                               "Unable to parse Unicode value: " + unicode);
                   }
               }
               continue;
           }
           if (hadSlash) {
               hadSlash = false;
               if (ch == 'u') {
                   inUnicode = true;
               } else {
                   sb.append(ch);
               }
               continue;
           } else if (ch == '\\') {
               hadSlash = true;
               continue;
           }
           sb.append(ch);
       }
       return sb.toString();
   }

    /**
     * Filters non-printable ASCII characters (0-31 and 127) from a string with spaces and
     * returns that. Please note that non-printable characters outside the ASCII character
     * set are not touched by this method.
     * @param text the text to be filtered.
     * @return the filtered text
     */
    public static String filterNonPrintableCharacters(String text) {
        int len = text.length();
        StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            final char ch = text.charAt(i);
            if (ch < 32 || ch == 127) {
                sb.append(' '); //Replace non-printables with a space
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

}
