package org.xbib.io.http.client.proxy;

import org.xbib.io.http.client.Realm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.xbib.io.http.client.util.MiscUtils.isNonEmpty;

/**
 * Represents a proxy server.
 */
public class ProxyServer {

    private final String host;
    private final int port;
    private final int securedPort;
    private final Realm realm;
    private final List<String> nonProxyHosts;
    private final boolean forceHttp10;

    public ProxyServer(String host, int port, int securedPort, Realm realm, List<String> nonProxyHosts, boolean forceHttp10) {
        this.host = host;
        this.port = port;
        this.securedPort = securedPort;
        this.realm = realm;
        this.nonProxyHosts = nonProxyHosts;
        this.forceHttp10 = forceHttp10;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getSecuredPort() {
        return securedPort;
    }

    public List<String> getNonProxyHosts() {
        return nonProxyHosts;
    }

    public boolean isForceHttp10() {
        return forceHttp10;
    }

    public Realm getRealm() {
        return realm;
    }

    /**
     * Checks whether proxy should be used according to nonProxyHosts settings of it, or we want to go directly to
     * target host. If <code>null</code> proxy is passed in, this method returns true -- since there is NO proxy, we
     * should avoid to use it. Simple hostname pattern matching using "*" are supported, but only as prefixes.
     *
     * @param hostname the hostname
     * @return true if we have to ignore proxy use (obeying non-proxy hosts settings), false otherwise.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/net/doc-files/net-properties.html">Networking
     * Properties</a>
     */
    public boolean isIgnoredForHost(String hostname) {
        if (isNonEmpty(nonProxyHosts)) {
            for (String nonProxyHost : nonProxyHosts) {
                if (matchNonProxyHost(hostname, nonProxyHost)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchNonProxyHost(String targetHost, String nonProxyHost) {

        if (nonProxyHost.length() > 1) {
            if (nonProxyHost.charAt(0) == '*') {
                return targetHost.regionMatches(true, targetHost.length() - nonProxyHost.length() + 1, nonProxyHost, 1,
                        nonProxyHost.length() - 1);
            } else if (nonProxyHost.charAt(nonProxyHost.length() - 1) == '*') {
                return targetHost.regionMatches(true, 0, nonProxyHost, 0, nonProxyHost.length() - 1);
            }
        }

        return nonProxyHost.equalsIgnoreCase(targetHost);
    }


    public static class Builder {

        private String host;
        private int port;
        private int securedPort;
        private Realm realm;
        private List<String> nonProxyHosts;
        private boolean forceHttp10;

        public Builder(String host, int port) {
            this.host = host;
            this.port = port;
            this.securedPort = port;
        }

        public Builder setSecuredPort(int securedPort) {
            this.securedPort = securedPort;
            return this;
        }

        public Builder setRealm(Realm realm) {
            this.realm = realm;
            return this;
        }

        public Builder setNonProxyHost(String nonProxyHost) {
            if (nonProxyHosts == null) {
                nonProxyHosts = new ArrayList<String>(1);
            }
            nonProxyHosts.add(nonProxyHost);
            return this;
        }

        public Builder setNonProxyHosts(List<String> nonProxyHosts) {
            this.nonProxyHosts = nonProxyHosts;
            return this;
        }

        public Builder setForceHttp10(boolean forceHttp10) {
            this.forceHttp10 = forceHttp10;
            return this;
        }

        public ProxyServer build() {
            List<String> nonProxyHosts = this.nonProxyHosts != null ? Collections.unmodifiableList(this.nonProxyHosts) : Collections.emptyList();
            return new ProxyServer(host, port, securedPort, realm, nonProxyHosts, forceHttp10);
        }
    }
}
