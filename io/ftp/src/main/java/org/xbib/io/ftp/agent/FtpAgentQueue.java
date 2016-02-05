package org.xbib.io.ftp.agent;

import org.xbib.io.ftp.FTPConfiguration;
import org.xbib.io.ftp.FTPFileSystemProvider;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A queue of {@link FtpAgent} instances
 *
 * <p>This is the main interaction with a {@link FTPFileSystemProvider}.</p>
 *
 * <p>Given the nature of the FTP protocol, you can only have one data
 * connection active at any time for any one FTP session. This class maintains
 * a bounded, blocking queue of clients (implementations of {@link FtpAgent})
 * which a provider can use. If the queue is exhausted at the time an agent is
 * needed, the provider will block until an agent becomes available.</p>
 */
public final class FtpAgentQueue implements Closeable {
    private final BlockingQueue<FtpAgent> queue;
    private final FTPConfiguration cfg;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final FtpAgentFactory factory;
    private final int maxAgents;

    /**
     * Constructor
     *
     * @param factory   the agent factory
     * @param cfg       the FTP server configuration
     * @param maxAgents the maximum number of agents to maintain into the queue
     */
    public FtpAgentQueue(final FtpAgentFactory factory,
                         final FTPConfiguration cfg, final int maxAgents) {
        queue = new ArrayBlockingQueue<>(maxAgents);
        this.cfg = cfg;
        this.maxAgents = maxAgents;
        this.factory = factory;
    }

    /**
     * Get one agent
     *
     * <p>It may happen that an agent has died in the middle; in this case, this
     * method returns a new agent.</p>
     *
     * @return a suitable {@link FtpAgent}
     * @throws IOException the agent failed to establish a connection to the FTP
     *                     server
     */
    public FtpAgent getAgent()
            throws IOException {
        if (!initialized.getAndSet(true)) {
            fillQueue();
        }
        try {
            FtpAgent agent = queue.take();
            if (agent.isDead()) {
                agent = factory.get(this, cfg);
            }
            agent.connect();
            return agent;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted!", e);
        }
    }

    /**
     * Push an FTP agent back into the queue
     *
     * @param agent the agent to push back
     */
    public void pushBack(final FtpAgent agent) {
        queue.add(agent);
    }

    /**
     * Close this queue
     *
     * <p>All agents into the queue are drained into the list and disconnect
     * from the FTP server.</p>
     *
     * <p>The exception thrown back by this method is the one of the first
     * agent which failed to close the connection properly.</p>
     *
     * @throws IOException One agent fails to disconnect
     */
    @Override
    public void close()
            throws IOException {
        IOException toThrow = null;

        final List<FtpAgent> list = new ArrayList<>();
        queue.drainTo(list);
        for (final FtpAgent agent : list) {
            try {
                agent.disconnect();
            } catch (IOException e) {
                if (toThrow == null) {
                    toThrow = e;
                }
            }
        }

        if (toThrow != null) {
            throw toThrow;
        }
    }

    private void fillQueue() {
        for (int i = 0; i < maxAgents; i++) {
            queue.add(factory.get(this, cfg));
        }
    }
}
