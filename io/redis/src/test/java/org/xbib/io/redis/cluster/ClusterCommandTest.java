package org.xbib.io.redis.cluster;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import org.xbib.io.redis.codec.Utf8StringCodec;
import org.xbib.io.redis.output.StatusOutput;
import org.xbib.io.redis.protocol.Command;
import org.xbib.io.redis.protocol.CommandType;

import static org.assertj.core.api.Assertions.assertThat;

public class ClusterCommandTest {

    private ClusterCommand<String, String, String> sut;
    private Command<String, String, String> command = new Command<String, String, String>(CommandType.TYPE,
            new StatusOutput<String, String>(new Utf8StringCodec()), null);

    @Before
    public void before() throws Exception {
        sut = new ClusterCommand<String, String, String>(command, null, 1);
    }

    @Test
    public void testException() throws Exception {

        assertThat(command.getException()).isNull();
        sut.setException(new Exception());
        assertThat(command.getException()).isNotNull();
    }

    @Test
    public void testCancel() throws Exception {

        assertThat(command.isCancelled()).isFalse();
        sut.cancel(true);
        assertThat(command.isCancelled()).isTrue();
    }

    @Test
    public void testComplete() throws Exception {

        command.complete();
        sut.await(1, TimeUnit.MINUTES);
        assertThat(sut.isDone()).isTrue();
        assertThat(sut.isCancelled()).isFalse();
    }

    @Test
    public void testCompleteListener() throws Exception {

        final List<String> someList = Lists.newArrayList();
        sut.addListener(new Runnable() {
            @Override
            public void run() {
                someList.add("");
            }
        }, MoreExecutors.sameThreadExecutor());
        command.complete();
        sut.await(1, TimeUnit.MINUTES);

        assertThat(sut.isDone()).isTrue();
        assertThat(someList.size()).describedAs("Inner listener has to add one element").isEqualTo(1);
    }
}
