package org.xbib.io.http.client.resolver;

import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.List;

public interface NameResolver {

    Future<List<InetSocketAddress>> resolve(String name, int port);
}
