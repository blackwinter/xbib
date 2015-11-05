
package org.xbib.io.iso23950.service;

import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.iso23950.ZSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

public class ZServiceFactory {

    private final static ZServiceFactory instance = new ZServiceFactory();

    private ZServiceFactory() {
    }

    public static ZService getService(String name) throws IOException {
        Properties properties = new Properties();
        InputStream in = instance.getClass().getResourceAsStream("/org/xbib/io/iso23950/service/" + name + ".properties");
        if (in != null) {
            properties.load(in);
        } else {
            throw new IllegalArgumentException("service " + name + " not found");
        }
        URI uri = URI.create(properties.getProperty("uri"));
        ConnectionService<ZSession> service = ConnectionService.getInstance();
        Connection<ZSession> connection = service
                .getConnectionFactory(uri)
                .getConnection(uri);
        return connection.createSession().setProperties(properties);
    }

}