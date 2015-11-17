package org.xbib.io.redis.pubsub;

import org.xbib.io.redis.ClientOptions;
import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandHandler;
import org.xbib.io.redis.protocol.CommandOutput;
import org.xbib.io.redis.protocol.RedisCommand;
import org.xbib.io.redis.resource.ClientResources;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.Queue;

/**
 * A netty {@link ChannelHandler} responsible for writing redis pub/sub commands and reading the response stream from the
 * server.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class PubSubCommandHandler<K, V> extends CommandHandler<K, V> {
    private RedisCodec<K, V> codec;
    private PubSubOutput<K, V, V> output;

    /**
     * Initialize a new instance.
     *
     * @param clientOptions   client options for the connection
     * @param clientResources client resources for this connection
     * @param queue           Command queue.
     * @param codec           Codec.
     */
    public PubSubCommandHandler(ClientOptions clientOptions, ClientResources clientResources,
                                Queue<RedisCommand<K, V, ?>> queue, RedisCodec<K, V> codec) {
        super(clientOptions, clientResources, queue);
        this.codec = codec;
        this.output = new PubSubOutput<K, V, V>(codec);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer) throws InterruptedException {
        while (output.type() == null && !queue.isEmpty()) {
            CommandOutput<K, V, ?> currentOutput = queue.peek().getOutput();
            if (!rsm.decode(buffer, currentOutput)) {
                return;
            }
            queue.poll().complete();
            buffer.discardReadBytes();
            if (currentOutput instanceof PubSubOutput) {
                ctx.fireChannelRead(currentOutput);
            }
        }

        while (rsm.decode(buffer, output)) {
            ctx.fireChannelRead(output);
            output = new PubSubOutput<K, V, V>(codec);
            buffer.discardReadBytes();
        }
    }

}
