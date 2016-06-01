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
package org.xbib.iri;

import org.xbib.rdf.Node;
import org.xbib.scheme.HttpScheme;
import org.xbib.scheme.Scheme;
import org.xbib.scheme.SchemeRegistry;
import org.xbib.text.CharUtils;
import org.xbib.text.CharUtils.Profile;
import org.xbib.text.InvalidCharacterException;
import org.xbib.text.UrlEncoding;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IRI implements Comparable<IRI>, Node {

    private final static SchemeRegistry registry = SchemeRegistry.getInstance();

    protected Scheme schemeClass;

    private String scheme;

    private String schemeSpecificPart;

    private String authority;

    private String userinfo;

    private String host;

    private int port = -1;

    private String path;

    private String query;

    private String fragment;

    private String a_schemeSpecificPart;

    private String a_host;

    private String a_fragment;

    private String a_path;

    private String a_query;

    private String a_userinfo;

    private String a_authority;

    @Override
    public boolean isVisible() {
        return true;
    }

    public static class Builder {

        protected Scheme schemeClass;
        private String prefix;
        private String schemeSpecificPart;
        private String authority;
        private String userinfo;
        private String host;
        private int port = -1;
        private String path;
        private String query;
        private String fragment;

        private Builder() {
        }

        public Builder scheme(String scheme) {
            this.prefix = scheme;
            this.schemeClass = registry.getScheme(scheme);
            return this;
        }

        public Builder schemeSpecificPart(String schemeSpecificPart) {
            this.schemeSpecificPart = schemeSpecificPart;
            return this;
        }

        public Builder curie(String prefix, String path) {
            this.prefix = prefix;
            this.path = path;
            return this;
        }

        public Builder curie(String schemeAndPath) {
            int pos = schemeAndPath.indexOf(':');
            this.prefix = pos > 0 ? schemeAndPath.substring(0, pos) : null;
            this.path = pos > 0 ? schemeAndPath.substring(pos + 1) : schemeAndPath;
            return this;
        }

        public Builder authority(String authority) {
            this.authority = authority;
            return this;
        }

        public Builder userinfo(String userinfo) {
            this.userinfo = userinfo;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder fragment(String fragment) {
            this.fragment = fragment;
            return this;
        }

        public IRI build() {
            return schemeSpecificPart != null ?
                    new IRI(prefix, schemeSpecificPart, fragment) :
                    new IRI(schemeClass,
                            prefix,
                            authority,
                            userinfo,
                            host,
                            port,
                            path,
                            query,
                            fragment);
        }

    }

    public IRI(IRI iri) {
        fromIRI(iri);
        build();
    }

    public IRI(URI uri) {
        fromURI(uri);
        build();
    }

    public IRI(String scheme, String schemeSpecificPart, String fragment) {
        this.scheme = scheme.toLowerCase();
        this.schemeSpecificPart = schemeSpecificPart;
        this.fragment = fragment;
        build();
    }

    public IRI(String iri) {
        parse(CharUtils.stripBidi(iri));
        build();
    }

    IRI(Scheme schemeClass,
        String scheme,
        String authority,
        String userinfo,
        String host,
        int port,
        String path,
        String query,
        String fragment) {
        this.schemeClass = schemeClass;
        this.scheme = scheme != null ? scheme.toLowerCase() : null;
        this.authority = authority;
        this.userinfo = userinfo;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
        this.fragment = fragment;
        build();
    }

    public static IRI create(String iri) {
        return new IRI(iri);
    }

    private IRI build() {
        if (authority == null && (userinfo != null || host != null)) {
            StringBuilder buf = new StringBuilder();
            buildAuthority(buf, userinfo, host, port);
            authority = (buf.length() != 0) ? buf.toString() : null;
        }
        StringBuilder buf = new StringBuilder();
        buildSchemeSpecificPart(buf, authority, path, query, fragment);
        schemeSpecificPart = buf.toString();
        return this;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((authority == null) ? 0 : authority.hashCode());
        result = PRIME * result + ((fragment == null) ? 0 : fragment.hashCode());
        result = PRIME * result + ((host == null) ? 0 : host.hashCode());
        result = PRIME * result + ((path == null) ? 0 : path.hashCode());
        result = PRIME * result + port;
        result = PRIME * result + ((query == null) ? 0 : query.hashCode());
        result = PRIME * result + ((scheme == null) ? 0 : scheme.hashCode());
        result = PRIME * result + ((userinfo == null) ? 0 : userinfo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IRI other = (IRI) obj;
        if (authority == null) {
            if (other.authority != null) {
                return false;
            }
        } else if (!authority.equals(other.authority)) {
            return false;
        }
        if (fragment == null) {
            if (other.fragment != null) {
                return false;
            }
        } else if (!fragment.equals(other.fragment)) {
            return false;
        }
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (query == null) {
            if (other.query != null) {
                return false;
            }
        } else if (!query.equals(other.query)) {
            return false;
        }
        if (scheme == null) {
            if (other.scheme != null) {
                return false;
            }
        } else if (!scheme.equals(other.scheme)) {
            return false;
        }
        if (userinfo == null) {
            if (other.userinfo != null) {
                return false;
            }
        } else if (!userinfo.equals(other.userinfo)) {
            return false;
        }
        return true;
    }

    public String getAuthority() {
        return (authority != null && authority.length() > 0) ? authority : null;
    }

    public String getFragment() {
        return fragment;
    }

    public String getHost() {
        return (host != null && host.length() > 0) ? host : null;
    }

    public IDNA getIDN() {
        return new IDNA(host);
    }

    public String getASCIIHost() {
        if (host != null && a_host == null) {
            if (host.startsWith("[")) {
                a_host = host;
            } else {
                a_host = IDNA.toASCII(host);
            }
        }
        return (a_host != null && a_host.length() > 0) ? a_host : null;
    }

    public String getPath() {
        return path;
    }

    public int getPort() {
        return port;
    }

    public String getQuery() {
        return query;
    }

    public String getScheme() {
        return scheme;
    }

    public String getSchemeSpecificPart() {
        return schemeSpecificPart;
    }

    public String getUserInfo() {
        return userinfo;
    }

    public String getASCIIAuthority() {
        if (authority != null && a_authority == null) {
            a_authority = buildASCIIAuthority();
        }
        return (a_authority != null && a_authority.length() > 0) ? a_authority : null;
    }

    public String getASCIIFragment() {
        if (fragment != null && a_fragment == null) {
            a_fragment = UrlEncoding.encode(fragment, Profile.FRAGMENT.filter());
        }
        return a_fragment;
    }

    public String getASCIIPath() {
        if (path != null && a_path == null) {
            a_path = UrlEncoding.encode(path, Profile.PATH.filter());
        }
        return a_path;
    }

    public String getASCIIQuery() {
        if (query != null && a_query == null) {
            a_query = UrlEncoding.encode(query, Profile.QUERY.filter(), Profile.PATH.filter());
        }
        return a_query;
    }

    public String getASCIIUserInfo() {
        if (userinfo != null && a_userinfo == null) {
            a_userinfo = UrlEncoding.encode(userinfo, Profile.USERINFO.filter());
        }
        return a_userinfo;
    }

    public String getASCIISchemeSpecificPart() {
        if (a_schemeSpecificPart == null) {
            StringBuilder buf = new StringBuilder();
            buildSchemeSpecificPart(buf, getASCIIAuthority(), getASCIIPath(), getASCIIQuery(), getASCIIFragment());
            a_schemeSpecificPart = buf.toString();
        }
        return a_schemeSpecificPart;
    }

    private String buildASCIIAuthority() {
        if (schemeClass instanceof HttpScheme) {
            StringBuilder buf = new StringBuilder();
            buildAuthority(buf, getASCIIUserInfo(), getASCIIHost(), getPort());
            return buf.toString();
        } else {
            return UrlEncoding.encode(authority, Profile.AUTHORITY.filter());
        }
    }

    public boolean isAbsolute() {
        return scheme != null;
    }

    public boolean isOpaque() {
        return path == null;
    }

    public static IRI relativize(IRI b, IRI c) {
        if (c.isOpaque() || b.isOpaque()) {
            return c;
        }
        if ((b.scheme == null && c.scheme != null) || (b.scheme != null && c.scheme == null)
                || (b.scheme != null && !b.scheme.equalsIgnoreCase(c.scheme))) {
            return c;
        }
        String bpath = normalize(b.getPath());
        String cpath = normalize(c.getPath());
        if (!bpath.equals(cpath)) {
            if (bpath.charAt(bpath.length() - 1) != '/') {
                bpath += "/";
            }
            if (!cpath.startsWith(bpath)) {
                return c;
            }
        }
        return new IRI(null,
                null,
                null,
                null,
                null,
                -1,
                normalize(cpath.substring(bpath.length())),
                c.getQuery(),
                c.getFragment());
    }

    public boolean isPathAbsolute() {
        String path = getPath();
        return (path != null) && path.length() > 0 && path.charAt(0) == '/';
    }

    public boolean isSameDocumentReference() {
        return scheme == null && authority == null
                && (path == null || path.length() == 0 || path.equals("."))
                && query == null;
    }

    public IRI resolve(IRI iri) {
        return resolve(this, iri);
    }

    public IRI resolve(String iri) {
        return resolve(this, new IRI(iri));
    }

    public static IRI resolve(IRI b, String c) throws IOException {
        return resolve(b, new IRI(c));
    }

    public static IRI resolve(IRI b, IRI c) {
        if (c == null) {
            return null;
        }
        if ("".equals(c.toString()) || "#".equals(c.toString())
                || ".".equals(c.toString())
                || "./".equals(c.toString())) {
            return b;
        }
        if (b == null) {
            return c;
        }
        if (c.isOpaque() || b.isOpaque()) {
            return c;
        }
        if (c.isSameDocumentReference()) {
            String cfragment = c.getFragment();
            String bfragment = b.getFragment();
            if ((cfragment == null && bfragment == null) || (cfragment != null && cfragment.equals(bfragment))) {
                return b;
            } else {
                return new IRI(b.schemeClass, b.getScheme(), b.getAuthority(), b.getUserInfo(), b.getHost(), b.getPort(),
                        normalize(b.getPath()), b.getQuery(), cfragment);
            }
        }
        if (c.isAbsolute()) {
            return c;
        }
        Scheme _scheme = b.schemeClass;
        String scheme = b.scheme;
        String query = c.getQuery();
        String fragment = c.getFragment();
        String userinfo;
        String authority;
        String host;
        int port;
        String path;
        if (c.getAuthority() == null) {
            authority = b.getAuthority();
            userinfo = b.getUserInfo();
            host = b.getHost();
            port = b.getPort();
            path = c.isPathAbsolute() ? normalize(c.getPath()) : resolve(b.getPath(), c.getPath());
        } else {
            authority = c.getAuthority();
            userinfo = c.getUserInfo();
            host = c.getHost();
            port = c.getPort();
            path = normalize(c.getPath());
        }
        return new IRI(_scheme, scheme, authority, userinfo, host, port, path, query, fragment);
    }

    public IRI normalize() {
        return normalize(this);
    }

    public static IRI normalize(IRI iri) {
        if (iri.isOpaque() || iri.getPath() == null) {
            return iri;
        }
        IRI normalized = null;
        if (iri.schemeClass != null) {
            normalized = iri.schemeClass.normalize(iri);
        }
        return (normalized != null) ? normalized : new IRI(iri.schemeClass, iri.getScheme(), iri.getAuthority(), iri
                .getUserInfo(), iri.getHost(), iri.getPort(), normalize(iri.getPath()), UrlEncoding.encode(UrlEncoding
                .decode(iri.getQuery()), Profile.IQUERY.filter()), UrlEncoding
                .encode(UrlEncoding.decode(iri.getFragment()), Profile.IFRAGMENT.filter()));
    }

    protected static String normalize(String path) {
        if (path == null || path.length() == 0) {
            return "/";
        }
        String[] segments = path.split("/");
        if (segments.length < 2) {
            return path;
        }
        StringBuilder buf = new StringBuilder("/");
        for (int n = 0; n < segments.length; n++) {
            String segment = segments[n].intern();
            if (segment == ".") {
                segments[n] = null;
            } else if (segment == "src/main") {
                segments[n] = null;
                int i = n;
                while (--i > -1) {
                    if (segments[i] != null) {
                        break;
                    }
                }
                if (i > -1) {
                    segments[i] = null;
                }
            }
        }
        for (int n = 0; n < segments.length; n++) {
            if (segments[n] != null) {
                if (buf.length() > 1) {
                    buf.append('/');
                }
                buf.append(UrlEncoding.encode(UrlEncoding.decode(segments[n]), Profile.IPATHNODELIMS_SEG.filter()));
            }
        }
        if (path.endsWith("/") || path.endsWith("/.")) {
            buf.append('/');
        }
        return buf.toString();
    }

    private static void buildAuthority(StringBuilder buf, String aui, String ah, int port) {
        if (aui != null && aui.length() != 0) {
            buf.append(aui);
            buf.append('@');
        }
        if (ah != null && ah.length() != 0) {
            buf.append(ah);
        }
        if (port != -1) {
            buf.append(':');
            buf.append(port);
        }
    }

    private static void buildSchemeSpecificPart(StringBuilder buf, String authority, String path, String query, String fragment) {
        if (authority != null) {
            buf.append("//");
            buf.append(authority);
        }
        if (path != null && path.length() > 0) {
            buf.append(path);
        }
        if (query != null) {
            buf.append('?');
            buf.append(query);
        }
        if (fragment != null) {
            buf.append('#');
            buf.append(fragment);
        }
    }

    private static String resolve(String bpath, String cpath) {
        if (bpath == null && cpath == null) {
            return null;
        }
        if (bpath == null) {
            return (!cpath.startsWith("/")) ? "/" + cpath : cpath;
        }
        if (cpath == null) {
            return bpath;
        }
        StringBuilder buf = new StringBuilder("");
        int n = bpath.lastIndexOf('/');
        if (n > -1) {
            buf.append(bpath.substring(0, n + 1));
        }
        if (cpath.length() != 0) {
            buf.append(cpath);
        }
        if (buf.charAt(0) != '/') {
            buf.insert(0, '/');
        }
        return normalize(buf.toString());
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        String scheme = getScheme();
        if (scheme != null && scheme.length() != 0) {
            buf.append(scheme);
            buf.append(':');
        }
        buf.append(getSchemeSpecificPart());
        return buf.toString();
    }

    public String toEncodedString() {
        StringBuilder buf = new StringBuilder();
        String scheme = getScheme();
        if (scheme != null && scheme.length() != 0) {
            buf.append(scheme);
            buf.append(':');
        }
        buf.append(getSchemeSpecificPart());
        return UrlEncoding.encode(buf.toString(), Profile.SCHEMESPECIFICPART.filter());
    }

    public String toASCIIString() {
        StringBuilder buf = new StringBuilder();
        String scheme = getScheme();
        if (scheme != null && scheme.length() != 0) {
            buf.append(scheme);
            buf.append(':');
        }
        buf.append(getASCIISchemeSpecificPart());
        return buf.toString();
    }

    public String toBIDIString() {
        return CharUtils.wrapBidi(toString(), CharUtils.LRE);
    }

    public URI toURI() throws URISyntaxException {
        return new URI(toASCIIString());
    }

    public java.net.URL toURL() throws MalformedURLException, URISyntaxException {
        return toURI().toURL();
    }

    public static Builder builder() {
        return new Builder();
    }

    private void parseAuthority() {
        if (authority != null) {
            Matcher auth = AUTHORITYPATTERN.matcher(authority);
            if (auth.find()) {
                userinfo = auth.group(1);
                host = auth.group(2);
                if (auth.group(3) != null) {
                    port = Integer.parseInt(auth.group(3));
                } else {
                    port = -1;
                }
            }
            try {
                CharUtils.verify(userinfo, Profile.IUSERINFO);
                CharUtils.verify(host, Profile.IHOST);
            } catch (InvalidCharacterException e) {
                throw new IRISyntaxException(e);
            }
        }
    }

    private void fromURI(URI uri) {
        SchemeRegistry reg = SchemeRegistry.getInstance();
        scheme = uri.getScheme();
        schemeClass = reg.getScheme(scheme);
        authority = uri.getAuthority();
        path = uri.getPath();
        query = uri.getQuery();
        fragment = uri.getFragment();
        parseAuthority();
    }

    private void fromIRI(IRI uri) {
        SchemeRegistry reg = SchemeRegistry.getInstance();
        scheme = uri.getScheme();
        schemeClass = reg.getScheme(scheme);
        authority = uri.getAuthority();
        path = uri.getPath();
        query = uri.getQuery();
        fragment = uri.getFragment();
        parseAuthority();
    }

    private void parse(String iri) {
        try {
            SchemeRegistry reg = SchemeRegistry.getInstance();
            Matcher irim = IRIPATTERN.matcher(iri);
            if (irim.find()) {
                scheme = irim.group(1);
                schemeClass = reg.getScheme(scheme);
                authority = irim.group(2);
                path = irim.group(3);
                query = irim.group(4);
                fragment = irim.group(5);
                parseAuthority();
                try {
                    CharUtils.verify(scheme, Profile.SCHEME);
                    CharUtils.verify(path, Profile.IPATH);
                    CharUtils.verify(query, Profile.IQUERY);
                    CharUtils.verify(fragment, Profile.IFRAGMENT);
                } catch (InvalidCharacterException e) {
                    throw new IRISyntaxException(e);
                }
            } else {
                throw new IRISyntaxException("Invalid Syntax");
            }
        } catch (IRISyntaxException e) {
            throw e;
        } catch (Exception e) {
            throw new IRISyntaxException(e);
        }
    }

    private static final Pattern IRIPATTERN =
            Pattern.compile("^(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*)(?:\\?([^#]*))?(?:#(.*))?");

    private static final Pattern AUTHORITYPATTERN =
            Pattern.compile("^(?:(.*)?@)?((?:\\[.*\\])|(?:[^:]*))?(?::(\\d+))?");


    @Override
    public int compareTo(IRI that) {
        int c;
        if ((c = compareIgnoringCase(scheme, that.scheme)) != 0) {
            return c;
        }
        if (isOpaque()) {
            if (that.isOpaque()) {
                // Both opaque
                if ((c = compare(schemeSpecificPart, that.schemeSpecificPart)) != 0) {
                    return c;
                }
                return compare(fragment, that.fragment);
            }
            return +1;
        } else if (that.isOpaque()) {
            return -1;
        }
        // Hierarchical
        if ((host != null) && (that.host != null)) {
            // Both server-based
            if ((c = compare(userinfo, that.userinfo)) != 0) {
                return c;
            }
            if ((c = compareIgnoringCase(host, that.host)) != 0) {
                return c;
            }
            if ((c = port - that.port) != 0) {
                return c;
            }
        } else {
            if ((c = compare(authority, that.authority)) != 0) {
                return c;
            }
        }
        if ((c = compare(path, that.path)) != 0) {
            return c;
        }
        if ((c = compare(query, that.query)) != 0) {
            return c;
        }
        return compare(fragment, that.fragment);
    }

    private int compare(String s, String t) {
        if (s != null) {
            if (s.equals(t)) {
                return 0;
            }
            if (t != null) {
                return s.compareTo(t);
            } else {
                return +1;
            }
        } else {
            return -1;
        }
    }

    private int compareIgnoringCase(String s, String t) {
        if (s != null) {
            if (s.equals(t)) {
                return 0;
            }
            if (t != null) {
                int sn = s.length();
                int tn = t.length();
                int n = sn < tn ? sn : tn;
                for (int i = 0; i < n; i++) {
                    int c = toLower(s.charAt(i)) - toLower(t.charAt(i));
                    if (c != 0) {
                        return c;
                    }
                }
                return sn - tn;
            }
            return +1;
        } else {
            return -1;
        }
    }

    private int toLower(char c) {
        if ((c >= 'A') && (c <= 'Z')) {
            return c + ('a' - 'A');
        }
        return c;
    }
}
