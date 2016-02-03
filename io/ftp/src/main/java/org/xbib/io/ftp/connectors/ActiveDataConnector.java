package org.xbib.io.ftp.connectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ActiveDataConnector implements Runnable, DataConnector {

    private final static Logger logger = LogManager.getLogger("org.xbib.io.ftp");

    private ServerSocket serverSocket;
    private Socket socket;
    private IOException exception;
    private ExecutorService threadPool;
    private Future<?> future;

    public ActiveDataConnector(int start, int stop) throws IOException {
        this.threadPool = Executors.newFixedThreadPool(32);
        boolean done = false;
        for (int port = start; port < stop && !done; port++) {
            while (!done) {
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReceiveBufferSize(8192);
                    serverSocket.bind(new InetSocketAddress(port));
                    logger.debug("bind server socket {} {}",
                            serverSocket.getLocalSocketAddress(),
                            serverSocket.getLocalPort());
                    done = true;
                } catch (IOException e) {
                    //
                }
            }
        }
        if (!done) {
            throw new IOException("cannot open the server socket. No available port found in range");
        }
        this.future = threadPool.submit(this);
    }

    /**
     * Returns the local port the server socket is bounded.
     *
     * @return The local port.
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void run() {
        int timeout = 15000;
        try {
            logger.debug("waiting for accept on socket {} {}",
                    serverSocket.getLocalSocketAddress(),
                    serverSocket.getLocalPort());
            serverSocket.setSoTimeout(timeout);
            socket = serverSocket.accept();
            socket.setSendBufferSize(8192);
        } catch (IOException e) {
            exception = e;
        } finally {
            // Close the server socket.
            try {
                serverSocket.close();
            } catch (IOException e) {
                //
            }
        }
    }

    @Override
    public void close() {
        if (socket != null) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    logger.debug("socket closed {}", socket.getLocalAddress());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (serverSocket != null) {
            try {
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                    logger.debug("server socket closed {}", serverSocket.getInetAddress());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        threadPool.shutdownNow();
    }

    public Socket openDataConnection() throws IOException {
        try {
            future.get();
        } catch (InterruptedException e) {
            throw new IOException("interrupted");
        } catch (ExecutionException e) {
            throw new IOException("execution exception", e);
        }
        if (exception != null) {
            throw new IOException("cannot receive the incoming connection", exception);
        }
        if (socket == null) {
            throw new IOException("no socket available");
        }
        return socket;
    }

}
