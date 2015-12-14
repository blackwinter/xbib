package org.xbib.io.http.client.channel;

import org.xbib.io.http.client.proxy.ProxyServer;
import org.xbib.io.http.client.uri.Uri;
import org.xbib.io.http.client.util.HttpUtils;

public interface ChannelPoolPartitioning {

    Object getPartitionKey(Uri uri, String virtualHost, ProxyServer proxyServer);

    enum PerHostChannelPoolPartitioning implements ChannelPoolPartitioning {

        INSTANCE;

        public Object getPartitionKey(Uri uri, String virtualHost, ProxyServer proxyServer) {
            String targetHostBaseUrl = virtualHost != null ? virtualHost : HttpUtils.getBaseUrl(uri);
            if (proxyServer != null) {
                return uri.isSecured() ? //
                        new ProxyPartitionKey(proxyServer.getHost(), proxyServer.getSecuredPort(), true, targetHostBaseUrl)
                        : new ProxyPartitionKey(proxyServer.getHost(), proxyServer.getPort(), false, targetHostBaseUrl);
            } else {
                return targetHostBaseUrl;
            }
        }
    }

    class ProxyPartitionKey {
        private final String proxyHost;
        private final int proxyPort;
        private final boolean secured;
        private final String targetHostBaseUrl;

        public ProxyPartitionKey(String proxyHost, int proxyPort, boolean secured, String targetHostBaseUrl) {
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.secured = secured;
            this.targetHostBaseUrl = targetHostBaseUrl;
        }

        @Override
        public String toString() {
            return new StringBuilder()//
                    .append("ProxyPartitionKey(proxyHost=").append(proxyHost)//
                    .append(", proxyPort=").append(proxyPort)//
                    .append(", secured=").append(secured)//
                    .append(", targetHostBaseUrl=").append(targetHostBaseUrl)//
                    .toString();
        }
    }
}
