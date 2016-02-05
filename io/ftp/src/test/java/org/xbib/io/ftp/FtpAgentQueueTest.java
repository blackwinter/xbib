package org.xbib.io.ftp;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.InOrder;
import org.xbib.io.ftp.agent.FtpAgent;
import org.xbib.io.ftp.agent.FtpAgentFactory;
import org.xbib.io.ftp.agent.FtpAgentQueue;

import java.io.IOException;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class FtpAgentQueueTest {
    private FtpAgentFactory agentFactory;
    private FtpAgent agent1, agent2, agent3;
    private FTPConfiguration cfg;

    @Before
    public void initMocks() {
        agentFactory = mock(FtpAgentFactory.class);
        agent1 = mock(FtpAgent.class);
        agent2 = mock(FtpAgent.class);
        agent3 = mock(FtpAgent.class);
        cfg = FTPConfiguration.newBuilder().setHostname("foo").build();
    }

    @Test
    public void agentsAreNotCreatedOnInit() {
        final int maxAgents = 3;
        new FtpAgentQueue(agentFactory, cfg, maxAgents);
        verify(agentFactory, never()).get(any(FtpAgentQueue.class),
                any(FTPConfiguration.class));
    }

    @Test
    public void agentsAreTakenInOrder()
            throws IOException {
        final int maxAgents = 3;
        final FtpAgentQueue queue = new FtpAgentQueue(agentFactory, cfg,
                maxAgents);
        when(agentFactory.get(same(queue), same(cfg)))
                .thenReturn(agent1).thenReturn(agent2).thenReturn(agent3);
        final InOrder inOrder = inOrder(agentFactory, agent1, agent2, agent3);
        FtpAgent agent;
        agent = queue.getAgent();
        assertSame(agent, agent1);
        agent = queue.getAgent();
        assertSame(agent, agent2);
        agent = queue.getAgent();
        assertSame(agent, agent3);
        inOrder.verify(agentFactory, times(maxAgents))
                .get(same(queue), same(cfg));
        inOrder.verify(agent1).connect();
        inOrder.verify(agent2).connect();
        inOrder.verify(agent3).connect();
        inOrder.verifyNoMoreInteractions();
    }

    @Test /*(dependsOnMethods = "agentsAreTakenInOrder")*/
    public void deadAgentsAreScrapped()
            throws IOException {
        final int maxAgents = 3;
        final FtpAgentQueue queue = new FtpAgentQueue(agentFactory, cfg,
                maxAgents);
        when(agent1.isDead()).thenReturn(true);
        when(agentFactory.get(same(queue), same(cfg)))
                .thenReturn(agent1).thenReturn(agent2);

        final FtpAgent agent = queue.getAgent();
        assertSame(agent, agent2);
        verify(agent1, never()).connect();
        verify(agentFactory, times(maxAgents + 1))
                .get(same(queue), same(cfg));
    }

    @Test /*(dependsOnMethods = "agentsAreTakenInOrder")*/
    public void closingQueueDisconnectsAgents()
            throws IOException {
        final int maxAgents = 3;
        final FtpAgentQueue queue = new FtpAgentQueue(agentFactory, cfg,
                maxAgents);
        when(agentFactory.get(same(queue), same(cfg)))
                .thenReturn(agent1).thenReturn(agent2).thenReturn(agent3);

        final InOrder inOrder = inOrder(agent1, agent2, agent3);

        queue.pushBack(queue.getAgent());
        queue.close();
        inOrder.verify(agent2).disconnect();
        inOrder.verify(agent3).disconnect();
        inOrder.verify(agent1).disconnect();
    }

    @Test
    public void allAgentsDisconnectEvenOnIOException()
            throws IOException {
        final int maxAgents = 3;
        final FtpAgentQueue queue = new FtpAgentQueue(agentFactory, cfg,
                maxAgents);
        when(agentFactory.get(same(queue), same(cfg)))
                .thenReturn(agent1).thenReturn(agent2).thenReturn(agent3);

        final InOrder inOrder = inOrder(agent1, agent2, agent3);
        final IOException e = new IOException();
        doThrow(e).when(agent2).disconnect();

        queue.pushBack(queue.getAgent());
        try {
            queue.close();
        } catch (IOException actual) {
            assertSame(actual, e);
        }
        inOrder.verify(agent2).disconnect();
        inOrder.verify(agent3).disconnect();
        inOrder.verify(agent1).disconnect();
    }
}
