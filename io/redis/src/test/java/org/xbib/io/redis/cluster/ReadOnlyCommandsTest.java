package org.xbib.io.redis.cluster;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadOnlyCommandsTest {

    @Test
    public void testCount() throws Exception {
        assertThat(ReadOnlyCommands.READ_ONLY_COMMANDS).hasSize(62);
    }
}