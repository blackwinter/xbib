package org.xbib.cluster;

import io.netty.channel.ChannelHandlerContext;
import org.xbib.cluster.network.Packet;

public class RemoteOperationContext implements OperationContext {

    private final ChannelHandlerContext ctx;
    private final int packageId;
    private final int serviceId;

    public RemoteOperationContext(ChannelHandlerContext ctx, int serviceId, int packageId) {
        this.serviceId = serviceId;
        this.ctx = ctx;
        this.packageId = packageId;
    }

    @Override
    public Member getSender() {
        // i don't want to include sender identity to each message
        // so we need a clever way to find the sender.
        // TODO: maybe we can keep a reverse map to resolve sender?
//        System.out.println(ctx.channel().remoteAddress());
//        Optional<Member> first = cluster.getMembers().stream()
//                .filter(x -> x.getAddress().equals(ctx.channel().remoteAddress()))
//                .findFirst();
//        return first.get();
        return null;
    }

    @Override
    public void reply(Object obj) {
        Packet msg = new Packet(packageId, obj, serviceId);
        ctx.writeAndFlush(msg);
    }

    @Override
    public int serviceId() {
        return serviceId;
    }

}
