package org.xbib.io.redis;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ClientOptionsTest {

    @Test
    public void testNew() throws Exception {
        checkAssertions(ClientOptions.create());
    }

    @Test
    public void testBuilder() throws Exception {
        checkAssertions(new ClientOptions.Builder().build());
    }

    @Test
    public void testCopy() throws Exception {
        checkAssertions(ClientOptions.copyOf(new ClientOptions.Builder().build()));
    }

    protected void checkAssertions(ClientOptions sut) {
        assertThat(sut.isAutoReconnect(), equalTo(true));
        assertThat(sut.isCancelCommandsOnReconnectFailure(), equalTo(false));
        assertThat(sut.isPingBeforeActivateConnection(), equalTo(false));
        assertThat(sut.isSuspendReconnectOnProtocolFailure(), equalTo(false));
    }

}