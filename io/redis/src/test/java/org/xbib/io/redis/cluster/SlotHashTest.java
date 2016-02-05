package org.xbib.io.redis.cluster;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SlotHashTest {

    @Test
    public void testHash() throws Exception {
        int result = SlotHash.getSlot("123456789".getBytes());
        assertThat(result).isEqualTo(0x31C3);

    }

    @Test
    public void testHashWithHash() throws Exception {
        int result = SlotHash.getSlot("key{123456789}a".getBytes());
        assertThat(result).isEqualTo(0x31C3);

    }
}
