package org.xbib.io.ftp.connectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActiveDataConnector implements Runnable, DataConnector {

    private final static Logger logger = LogManager.getLogger(ActiveDataConnector.class.getName());

    private ServerSocket serverSocket ;

    private Socket socket;
    private IOException exception;

    private ExecutorService threadPool;
    private Thread thread;

    public ActiveDataConnector(int start, int stop) throws IOException {
        this.threadPool = Executors.newFixedThreadPool(32);
        boolean done = false;
        for (int port = start; port < stop && !done; port++) {
            while (!done) {
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReceiveBufferSize(8192);
                    serverSocket.bind(new InetSocketAddress(port));
                    logger.info("bind server socket {} {}",
                            serverSocket.getLocalSocketAddress(),
                            serverSocket.getLocalPort());
                    done = true;
                } catch (IOException e) {
                    //
                }
            }
        }
        if (!done) {
            throw new IOException("cannot open the server ocket. " + "No available port found in range");
        }
        threadPool.submit(this);
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
            logger.info("waiting for accept on socket {} {}",
                    serverSocket.getLocalSocketAddress(),
                    serverSocket.getLocalPort());
            serverSocket.setSoTimeout(timeout);
            socket = serverSocket.accept();
            socket.setSendBufferSize(8 * 1024);
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

    public void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                //
            }
        }
        threadPool.shutdownNow();
    }

    public Socket openDataConnection () throws IOException {
        if (socket == null && exception == null) {
            try {
                thread.join();
            } catch (Exception e) {
                //
            }
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
