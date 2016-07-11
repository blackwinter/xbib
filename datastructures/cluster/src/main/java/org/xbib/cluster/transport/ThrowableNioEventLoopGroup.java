package org.xbib.cluster.transport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

public class ThrowableNioEventLoopGroup extends NioEventLoopGroup {
    private final List<EventExecutor> children = new ArrayList<>();

    public ThrowableNioEventLoopGroup(int nThreads, String name, UncaughtExceptionHandler exceptionHandler) {
        super(nThreads, new ThreadFactoryBuilder()
                .setNameFormat(name + "-%d")
                .setUncaughtExceptionHandler(exceptionHandler)
                .build());
        super.iterator().forEachRemaining(children::add);
    }

    public ThrowableNioEventLoopGroup(String name, UncaughtExceptionHandler exceptionHandler) {
        this(Runtime.getRuntime().availableProcessors() * 2, name, exceptionHandler);
    }

    public EventExecutor getChild(int id) {
        return children.get(id % children.size());
    }

}
