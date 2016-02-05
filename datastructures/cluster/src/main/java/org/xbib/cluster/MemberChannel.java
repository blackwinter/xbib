package org.xbib.cluster;

import org.xbib.cluster.network.Packet;

import java.util.concurrent.CompletableFuture;

public interface MemberChannel {
    CompletableFuture ask(Packet message);

    void send(Packet message);

    void close() throws InterruptedException;
}
