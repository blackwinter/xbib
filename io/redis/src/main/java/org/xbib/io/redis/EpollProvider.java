package org.xbib.io.redis;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

/**
 * Wraps and provides Epoll classes. This is to protect the user from {@link ClassNotFoundException}'s caused by the absence of
 * the {@literal netty-transport-native-epoll} library during runtime.
 */
public class EpollProvider {

    public final static Class<EventLoopGroup> epollEventLoopGroupClass;
    public final static Class<Channel> epollDomainSocketChannelClass;
    public final static Class<SocketAddress> domainSocketAddressClass;
    protected static final Logger logger = LogManager.getLogger(EpollProvider.class);

    static {

        epollEventLoopGroupClass = getClass("io.netty.channel.epoll.EpollEventLoopGroup");
        epollDomainSocketChannelClass = getClass("io.netty.channel.epoll.EpollDomainSocketChannel");
        domainSocketAddressClass = getClass("io.netty.channel.unix.DomainSocketAddress");
        if (epollDomainSocketChannelClass == null || epollEventLoopGroupClass == null) {
            logger.debug("Starting without optional Epoll library");
        }
    }

    /**
     * Try to load class {@literal className}.
     *
     * @param className
     * @param <T>       Expected return type for casting.
     * @return instance of {@literal className} or null
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> getClass(String className) {
        try {
            return (Class) forName(className);
        } catch (ClassNotFoundException e) {
            logger.debug("Cannot load class " + className, e);
        }
        return null;
    }

    /**
     * Check whether the Epoll library is available on the class path.
     *
     * @throws IllegalStateException if the {@literal netty-transport-native-epoll} library is not available
     */
    static void checkForEpollLibrary() {

        if (domainSocketAddressClass == null || epollDomainSocketChannelClass == null) {
            throw new IllegalStateException(
                    "Cannot connect using sockets without the optional netty-transport-native-epoll library on the class path");
        }
    }

    static SocketAddress newSocketAddress(String socketPath) {

        try {
            Constructor<SocketAddress> constructor = domainSocketAddressClass.getConstructor(String.class);
            return constructor.newInstance(socketPath);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static EventLoopGroup newEventLoopGroup(int nThreads, ThreadFactory threadFactory) {

        try {
            Constructor<EventLoopGroup> constructor = epollEventLoopGroupClass
                    .getConstructor(Integer.TYPE, ThreadFactory.class);
            return constructor.newInstance(nThreads, threadFactory);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static Class<?> forName(String className) throws ClassNotFoundException {
        return forName(className, getDefaultClassLoader());
    }

    private static Class<?> forName(String className, ClassLoader classLoader) throws ClassNotFoundException {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = className.lastIndexOf('.');
            if (lastDotIndex != -1) {
                String innerClassName = className.substring(0, lastDotIndex) + '$' + className.substring(lastDotIndex + 1);
                try {
                    return classLoader.loadClass(innerClassName);
                } catch (ClassNotFoundException ex2) {
                    // swallow - let original exception get through
                }
            }
            throw ex;
        }
    }

    /**
     * Return the default ClassLoader to use: typically the thread context ClassLoader, if available; the ClassLoader that
     * loaded the ClassUtils class will be used as fallback.
     *
     * @return the default ClassLoader (never <code>null</code>)
     * @see Thread#getContextClassLoader()
     */
    private static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = EpollProvider.class.getClassLoader();
        }
        return cl;
    }
}
