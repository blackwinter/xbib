package org.xbib.io.ftp;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.xbib.io.ftp.agent.FtpAgent;
import org.xbib.io.ftp.agent.FtpInputStream;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public final class FtpInputStreamTest {
    private InputStream stream;
    private FtpAgent agent;

    @Before
    public void init() {
        stream = mock(InputStream.class);
        agent = mock(FtpAgent.class);
    }

    @Test
    public void nullAgentIsNotAllowed() {
        try {
            new FtpInputStream(null, null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "agent is null");
        }
    }

    @Test
    public void nullInputStreamIsNotAllowed() {
        try {
            new FtpInputStream(agent, null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "input stream is null");
        }
    }

    @Test
    public void closingIsDoneInOrder()
            throws IOException {
        final FtpInputStream in = new FtpInputStream(agent, stream);
        final InOrder inOrder = inOrder(agent, stream);

        in.close();

        inOrder.verify(stream).close();
        inOrder.verify(agent).completeTransfer();
        inOrder.verify(agent).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void closingProceedsEvenIfStreamFailsToClose()
            throws IOException {
        final FtpInputStream in = new FtpInputStream(agent, stream);
        final InOrder inOrder = inOrder(agent, stream);

        doThrow(new IOException()).when(stream).close();

        try {
            in.close();
        } catch (IOException ignored) {
        } finally {
            inOrder.verify(stream).close();
            inOrder.verify(agent).completeTransfer();
            inOrder.verify(agent).close();
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test
    public void closingProceedsEventIfTransferDoesNotCompleteProperly()
            throws IOException {
        final FtpInputStream in = new FtpInputStream(agent, stream);
        final InOrder inOrder = inOrder(agent, stream);

        doThrow(new IOException()).when(agent).completeTransfer();

        try {
            in.close();
        } catch (IOException ignored) {
        } finally {
            inOrder.verify(stream).close();
            inOrder.verify(agent).completeTransfer();
            inOrder.verify(agent).close();
            inOrder.verifyNoMoreInteractions();
        }

    }
}
