
package org.xbib.io.archive;

import org.xbib.io.ConnectionFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

public class ArchiveService {

    private final static Map<String, ConnectionFactory> factories = new WeakHashMap();

    private final static ArchiveService instance = new ArchiveService();

    private ArchiveService() {
        ServiceLoader<ConnectionFactory> loader = ServiceLoader.load(ConnectionFactory.class);
        for (ConnectionFactory factory : loader) {
            if (!factories.containsKey(factory.getName())) {
                factories.put(factory.getName(), factory);
            }
        }
    }

    public static ConnectionFactory getConnectionFactory(String name) {
        if (factories.containsKey(name)) {
            return factories.get(name);
        }
        throw new IllegalArgumentException("Connection factory for " + name + " not found in " + factories);
    }

}
