package org.xbib.cluster.transport;

import org.xbib.cluster.Member;
import org.xbib.cluster.service.Service;

import java.util.List;

public interface TransportConstructor {
    Transport newInstance(ThrowableNioEventLoopGroup requestExecutor, List<Service> services, Member localMember);
}
