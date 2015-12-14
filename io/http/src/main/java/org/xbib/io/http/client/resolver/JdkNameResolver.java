package org.xbib.io.http.client.resolver;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * A blocking {@link NameResolver} that uses Java <code>InetAddress.getAllByName</code>.
 */
public enum JdkNameResolver implements NameResolver {

    INSTANCE;

    @Override
    public Future<List<InetSocketAddress>> resolve(String name, int port) {

        Promise<List<InetSocketAddress>> promise = ImmediateEventExecutor.INSTANCE.newPromise();
        try {
            InetAddress[] resolved = InetAddress.getAllByName(name);
            List<InetSocketAddress> socketResolved = new ArrayList<InetSocketAddress>(resolved.length);
            for (InetAddress res : resolved) {
                socketResolved.add(new InetSocketAddress(res, port));
            }
            return promise.setSuccess(socketResolved);
        } catch (UnknownHostException e) {
            return promise.setFailure(e);
        }
    }
}
