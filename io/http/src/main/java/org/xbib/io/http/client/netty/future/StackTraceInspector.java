package org.xbib.io.http.client.netty.future;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

public class StackTraceInspector {

    private static boolean exceptionInMethod(Throwable t, String className, String methodName) {
        try {
            for (StackTraceElement element : t.getStackTrace()) {
                if (element.getClassName().equals(className) && element.getMethodName().equals(methodName)) {
                    return true;
                }
            }
        } catch (Throwable ignore) {
        }
        return false;
    }

    private static boolean recoverOnConnectCloseException(Throwable t) {
        return exceptionInMethod(t, "sun.nio.ch.SocketChannelImpl", "checkConnect")
                || (t.getCause() != null && recoverOnConnectCloseException(t.getCause()));
    }

    public static boolean recoverOnNettyDisconnectException(Throwable t) {
        return t instanceof ClosedChannelException
                || exceptionInMethod(t, "io.netty.handler.ssl.SslHandler", "disconnect")
                || (t.getCause() != null && recoverOnConnectCloseException(t.getCause()));
    }

    public static boolean recoverOnReadOrWriteException(Throwable t) {

        if (t instanceof IOException && "Connection reset by peer".equalsIgnoreCase(t.getMessage())) {
            return true;
        }

        try {
            for (StackTraceElement element : t.getStackTrace()) {
                String className = element.getClassName();
                String methodName = element.getMethodName();
                if (className.equals("sun.nio.ch.SocketDispatcher") && (methodName.equals("read") || methodName.equals("write"))) {
                    return true;
                }
            }
        } catch (Throwable ignore) {
        }

        if (t.getCause() != null) {
            return recoverOnReadOrWriteException(t.getCause());
        }

        return false;
    }
}
