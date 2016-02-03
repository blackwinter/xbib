package org.xbib.io.ftp;

import java.util.Objects;

public final class FTPConfiguration {
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    private FTPConfiguration(final Builder builder) {
        hostname = builder.hostname;
        port = builder.port;
        username = builder.username;
        password = builder.password;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static final class Builder {
        private static final int MIN_PORT = 0;
        private static final int MAX_PORT = 65535;

        private String hostname;
        private int port = 21;
        private String username = "anonymous";
        private String password = "jftp@";

        private Builder() {
        }

        public Builder setHostname(String hostname) {
            this.hostname = Objects.requireNonNull(hostname,
                    "hostname cannot be null");
            return this;
        }

        public Builder setPort(final int port) {
            if (port < MIN_PORT || port > MAX_PORT) {
                throw new IllegalArgumentException("illegal port number "
                        + port);
            }
            this.port = port;
            return this;
        }

        public Builder setUsername(final String username) {
            this.username = Objects.requireNonNull(username,
                    "username cannot be null");
            return this;
        }

        public Builder setPassword(final String password) {
            this.password = Objects.requireNonNull(password,
                    "password cannot be null");
            return this;
        }

        public FTPConfiguration build() {
            Objects.requireNonNull(hostname, "no hostname has been provided");
            return new FTPConfiguration(this);
        }
    }
}
