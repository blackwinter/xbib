package org.xbib.cluster.transport;

import org.xbib.cluster.Member;
import org.xbib.cluster.MemberChannel;

public interface Transport {

    MemberChannel connect(Member member) throws InterruptedException;

    void initialize();

    void close();

}
