
package org.xbib.io.iso23950.service;

import org.xbib.io.iso23950.ZConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class ZServiceFactory {

    private final static ZServiceFactory instance = new ZServiceFactory();

    private ZServiceFactory() {
    }

    public static ZService getService(String name) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = instance.getClass().getResourceAsStream("/org/xbib/io/iso23950/service/" + name + ".properties")) {
            properties.load(in);
        }
        ZConnection connection = new ZConnection(new URL(properties.getProperty("uri")));
        return connection.createSession().setProperties(properties);
    }

}