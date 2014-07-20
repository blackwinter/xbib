package org.asynchttpclient.org.jboss.netty.handler.codec.http;

import java.util.Collection;

import org.asynchttpclient.Cookie;

/**
 * Encodes {@link Cookie}s into an HTTP header value.  This encoder can encode
 * the HTTP cookie version 0, 1, and 2.
 * <p>
 * This encoder is stateful.  It maintains an internal data structure that
 * holds the {@link Cookie}s added by the {@link #add(String, String)}
 * method.  Once {@link #encode()} is called, all added {@link Cookie}s are
 * encoded into an HTTP header value and all {@link Cookie}s in the internal
 * data structure are removed so that the encoder can start over.
 * <pre>
 * // Client-side example
 * {@link io.netty.handler.codec.http.HttpRequest} req = ...;
 * {@link org.asynchttpclient.org.jboss.netty.handler.codec.http.CookieEncoder} encoder = new {@link org.asynchttpclient.org.jboss.netty.handler.codec.http.CookieEncoder}(false);
 * encoder.addCookie("JSESSIONID", "1234");
 * res.setHeader("Cookie", encoder.encode());
 *
 * // Server-side example
 * {@link io.netty.handler.codec.http.HttpResponse} res = ...;
 * {@link org.asynchttpclient.org.jboss.netty.handler.codec.http.CookieEncoder} encoder = new {@link org.asynchttpclient.org.jboss.netty.handler.codec.http.CookieEncoder}(true);
 * encoder.addCookie("JSESSIONID", "1234");
 * res.setHeader("Set-Cookie", encoder.encode());
 * </pre>
 *
 * @see org.asynchttpclient.org.jboss.netty.handler.codec.http.CookieDecoder
 *
 */
public final class CookieEncoder {

    private CookieEncoder() {
    }
    
    public static String encodeClientSide(Collection<Cookie> cookies, boolean useRFC6265Style) {
        StringBuilder sb = new StringBuilder();

        for (Cookie cookie: cookies) {
            if (useRFC6265Style)
                encodeRFC6265Style(sb, cookie);
            else
                encodeRFC2965Style(sb, cookie);
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }
    
    private static void encodeRFC6265Style(StringBuilder sb, Cookie cookie) {
        addUnquoted(sb, cookie.getName(), cookie.getRawValue());
    }
    
    private static void encodeRFC2965Style(StringBuilder sb, Cookie cookie) {
        if (cookie.getVersion() >= 1) {
            add(sb, '$' + CookieHeaderNames.VERSION, 1);
        }

        add(sb, cookie.getName(), cookie.getValue());

        if (cookie.getPath() != null) {
            add(sb, '$' + CookieHeaderNames.PATH, cookie.getPath());
        }

        if (cookie.getDomain() != null) {
            add(sb, '$' + CookieHeaderNames.DOMAIN, cookie.getDomain());
        }

        if (cookie.getVersion() >= 1) {
            if (!cookie.getPorts().isEmpty()) {
                sb.append('$');
                sb.append(CookieHeaderNames.PORT);
                sb.append((char) HttpConstants.EQUALS);
                sb.append((char) HttpConstants.DOUBLE_QUOTE);
                for (int port: cookie.getPorts()) {
                    sb.append(port);
                    sb.append((char) HttpConstants.COMMA);
                }
                sb.setCharAt(sb.length() - 1, (char) HttpConstants.DOUBLE_QUOTE);
                sb.append((char) HttpConstants.SEMICOLON);
                sb.append((char) HttpConstants.SP);
            }
        }
    }

    private static void add(StringBuilder sb, String name, String val) {
        if (val == null) {
            addQuoted(sb, name, "");
            return;
        }

        for (int i = 0; i < val.length(); i ++) {
            char c = val.charAt(i);
            switch (c) {
            case '\t': case ' ': case '"': case '(':  case ')': case ',':
            case '/':  case ':': case ';': case '<':  case '=': case '>':
            case '?':  case '@': case '[': case '\\': case ']':
            case '{':  case '}':
                addQuoted(sb, name, val);
                return;
            }
        }

        addUnquoted(sb, name, val);
    }

    private static void addUnquoted(StringBuilder sb, String name, String val) {
        sb.append(name);
        sb.append((char) HttpConstants.EQUALS);
        sb.append(val);
        sb.append((char) HttpConstants.SEMICOLON);
        sb.append((char) HttpConstants.SP);
    }

    private static void addQuoted(StringBuilder sb, String name, String val) {
        if (val == null) {
            val = "";
        }

        sb.append(name);
        sb.append((char) HttpConstants.EQUALS);
        sb.append((char) HttpConstants.DOUBLE_QUOTE);
        sb.append(val.replace("\\", "\\\\").replace("\"", "\\\""));
        sb.append((char) HttpConstants.DOUBLE_QUOTE);
        sb.append((char) HttpConstants.SEMICOLON);
        sb.append((char) HttpConstants.SP);
    }

    private static void add(StringBuilder sb, String name, int val) {
        sb.append(name);
        sb.append((char) HttpConstants.EQUALS);
        sb.append(val);
        sb.append((char) HttpConstants.SEMICOLON);
        sb.append((char) HttpConstants.SP);
    }
}