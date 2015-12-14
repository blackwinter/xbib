package org.xbib.io.http.client.cookie;

public class Cookie {

    private final String name;
    private final String value;
    private final boolean wrap;
    private final String domain;
    private final String path;
    private final long maxAge;
    private final boolean secure;
    private final boolean httpOnly;
    public Cookie(String name, String value, boolean wrap, String domain, String path, long maxAge, boolean secure, boolean httpOnly) {
        this.name = name;
        this.value = value;
        this.wrap = wrap;
        this.domain = domain;
        this.path = path;
        this.maxAge = maxAge;
        this.secure = secure;
        this.httpOnly = httpOnly;
    }

    public static Cookie newValidCookie(String name, String value, boolean wrap, String domain, String path, long maxAge, boolean secure, boolean httpOnly) {
        name = name.trim();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c > 127) {
                throw new IllegalArgumentException("name contains non-ascii character: " + name);
            }

            // Check prohibited characters.
            switch (c) {
                case '\t':
                case '\n':
                case 0x0b:
                case '\f':
                case '\r':
                case ' ':
                case ',':
                case ';':
                case '=':
                    throw new IllegalArgumentException("name contains one of the following prohibited characters: " + "=,; \\t\\r\\n\\v\\f: " + name);
            }
        }

        if (name.charAt(0) == '$') {
            throw new IllegalArgumentException("name starting with '$' not allowed: " + name);
        }

        domain = validateValue("domain", domain);
        path = validateValue("path", path);

        return new Cookie(name, value, wrap, domain, path, maxAge, secure, httpOnly);
    }

    private static String validateValue(String name, String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.length() == 0) {
            return null;
        }

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\r':
                case '\n':
                case '\f':
                case 0x0b:
                case ';':
                    throw new IllegalArgumentException(name + " contains one of the following prohibited characters: " + ";\\r\\n\\f\\v (" + value + ')');
            }
        }
        return value;
    }

    public String getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isWrap() {
        return wrap;
    }

    public String getPath() {
        return path;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(name);
        buf.append('=');
        if (wrap) {
            buf.append('"').append(value).append('"');
        } else {
            buf.append(value);
        }
        if (domain != null) {
            buf.append("; domain=");
            buf.append(domain);
        }
        if (path != null) {
            buf.append("; path=");
            buf.append(path);
        }
        if (maxAge >= 0) {
            buf.append("; maxAge=");
            buf.append(maxAge);
            buf.append('s');
        }
        if (secure) {
            buf.append("; secure");
        }
        if (httpOnly) {
            buf.append("; HTTPOnly");
        }
        return buf.toString();
    }
}
