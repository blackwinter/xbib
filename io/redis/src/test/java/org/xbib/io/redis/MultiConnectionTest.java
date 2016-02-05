package org.xbib.io.redis;

import java.util.Set;
import java.util.concurrent.Future;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiConnectionTest extends AbstractCommandTest {

    @Test
    public void twoConnections() throws Exception {

        RedisAsyncConnection<String, String> connection1 = client.connectAsync();

        RedisAsyncConnection<String, String> connection2 = client.connectAsync();

        connection1.sadd("key", "member1", "member2").get();

        Future<Set<String>> members = connection2.smembers("key");

        assertThat(members.get()).hasSize(2);

    }

}
