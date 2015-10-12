package org.xbib.io.redis.metrics;

import org.xbib.io.redis.protocol.CommandKeyword;
import io.netty.channel.local.LocalAddress;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandLatencyIdTest {

    private CommandLatencyId sut = CommandLatencyId.create(LocalAddress.ANY, new LocalAddress("me"), CommandKeyword.ADDR);

    @Test
    public void testToString() throws Exception {
        assertThat(sut.toString()).contains("local:any -> local:me");
    }

    @Test
    public void testValues() throws Exception {
        assertThat(sut.localAddress()).isEqualTo(LocalAddress.ANY);
        assertThat(sut.remoteAddress()).isEqualTo(new LocalAddress("me"));
    }
}
