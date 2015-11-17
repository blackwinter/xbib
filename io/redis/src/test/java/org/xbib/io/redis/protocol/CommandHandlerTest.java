package org.xbib.io.redis.protocol;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import org.xbib.io.redis.ClientOptions;
import org.xbib.io.redis.ConnectionEvents;
import org.xbib.io.redis.RedisException;
import org.xbib.io.redis.codec.Utf8StringCodec;
import org.xbib.io.redis.output.StatusOutput;
import org.xbib.io.redis.resource.ClientResources;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CommandHandlerTest {

    private Queue<RedisCommand<String, String, ?>> q = new ArrayDeque<RedisCommand<String, String, ?>>(10);

    private CommandHandler<String, String> sut;

    private Command<String, String, String> command = new Command<String, String, String>(CommandType.APPEND,
            new StatusOutput<String, String>(new Utf8StringCodec()), null);

    @Mock
    private ChannelHandlerContext context;

    @Mock
    private Channel channel;

    @Mock
    private ByteBufAllocator byteBufAllocator;

    @Mock
    private ChannelPipeline pipeline;

    @Mock
    private EventLoop eventLoop;

    @Mock
    private ClientResources clientResources;

    @Before
    public void before() throws Exception {
        when(context.channel()).thenReturn(channel);
        when(context.alloc()).thenReturn(byteBufAllocator);
        when(channel.pipeline()).thenReturn(pipeline);
        when(channel.eventLoop()).thenReturn(eventLoop);
        when(eventLoop.submit(any(Runnable.class))).thenAnswer(new Answer<Future<?>>() {
            @Override
            public Future<?> answer(InvocationOnMock invocation) throws Throwable {
                Runnable r = (Runnable) invocation.getArguments()[0];
                r.run();
                return null;
            }
        });

        when(channel.write(any())).thenAnswer(new Answer<ChannelPromise>() {
            @Override
            public ChannelPromise answer(InvocationOnMock invocation) throws Throwable {
                return new DefaultChannelPromise(channel);
            }
        });

        when(channel.writeAndFlush(any())).thenAnswer(new Answer<ChannelPromise>() {
            @Override
            public ChannelPromise answer(InvocationOnMock invocation) throws Throwable {
                return new DefaultChannelPromise(channel);
            }
        });

        sut = new CommandHandler<String, String>(ClientOptions.create(), clientResources, q);
    }

    @Test
    public void testChannelActive() throws Exception {
        sut.channelRegistered(context);

        sut.channelActive(context);

        verify(pipeline).fireUserEventTriggered(any(ConnectionEvents.Activated.class));

    }

    @Test
    public void testExceptionChannelActive() throws Exception {
        sut.setState(CommandHandler.LifecycleState.ACTIVE);

        when(channel.isActive()).thenReturn(true);

        sut.channelActive(context);
        sut.exceptionCaught(context, new Exception());
    }

    @Test
    public void testIOExceptionChannelActive() throws Exception {
        sut.setState(CommandHandler.LifecycleState.ACTIVE);

        when(channel.isActive()).thenReturn(true);

        sut.channelActive(context);
        sut.exceptionCaught(context, new IOException("Connection timed out"));
    }

    @Test
    public void testExceptionChannelInactive() throws Exception {
        sut.setState(CommandHandler.LifecycleState.DISCONNECTED);
        sut.exceptionCaught(context, new Exception());
        verify(context, never()).fireExceptionCaught(any(Exception.class));
    }

    @Test
    public void testExceptionWithQueue() throws Exception {
        sut.setState(CommandHandler.LifecycleState.ACTIVE);
        q.clear();

        sut.channelActive(context);
        when(channel.isActive()).thenReturn(true);

        q.add(command);
        sut.exceptionCaught(context, new Exception());

        assertThat(q).isEmpty();
        assertThat(command.getException()).isNotNull();
    }

    @Test(expected = RedisException.class)
    public void testWriteWhenClosed() throws Exception {

        sut.setState(CommandHandler.LifecycleState.CLOSED);

        sut.write(command);
    }

    @Test
    public void testExceptionWhenClosed() throws Exception {

        sut.setState(CommandHandler.LifecycleState.CLOSED);

        sut.exceptionCaught(context, new Exception());
        verifyZeroInteractions(context);
    }

}
