
package org.xbib.io.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AsyncConnectionTest extends AbstractCommandTest {
    private RedisAsyncConnection<String, String> async;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void openAsyncConnection() throws Exception {
        async = client.connectAsync();
    }

    @After
    public void closeAsyncConnection() throws Exception {
        async.close();
    }

    @Test(timeout = 10000)
    public void multi() throws Exception {
        assertThat(async.multi().get(), equalTo("OK"));
        Future<String> set = async.set(key, value);
        Future<Long> rpush = async.rpush("list", "1", "2");
        Future<List<String>> lrange = async.lrange("list", 0, -1);
        assertThat(!set.isDone() && !rpush.isDone(), equalTo(true));
        assertThat(async.exec().get(), equalTo(list("OK", 2L, list("1", "2"))));
        assertThat(set.get(), equalTo("OK"));
        assertThat(rpush.get(), equalTo(2L));
        assertThat(lrange.get(), equalTo(list("1", "2")));
    }

    @Test(timeout = 10000)
    public void watch() throws Exception {
        assertThat(async.watch(key).get(), equalTo("OK"));
        redis.set(key, value + "X");
        async.multi();
        Future<String> set = async.set(key, value);
        Future<Long> append = async.append(key, "foo");
        assertThat(async.exec().get(), equalTo(list()));
        assertThat(set.get(), equalTo(null));
        assertThat(append.get(), equalTo(null));
    }

    @Test(timeout = 10000)
    public void futureListener() throws Exception {
        final List<Object> run = new ArrayList<>();
        Runnable listener = () -> run.add(new Object());
        for (int i = 0; i < 1000; i++) {
            redis.lpush(key, "" + i);
        }
        ListeningExecutorService executor = MoreExecutors.sameThreadExecutor();
        RedisAsyncConnection<String, String> connection = client.connectAsync();
        Long len = connection.llen(key).get();
        assertThat(len.intValue(), equalTo(1000));
        RedisFuture<List<String>> sort = connection.sort(key);
        assertThat(sort.isCancelled(), equalTo(false));
        sort.addListener(listener, executor);
        sort.get();
        Thread.sleep(100);
        assertThat(run, hasSize(1));
    }

    @Test(timeout = 1000)
    public void futureListenerCompleted() throws Exception {
        final List<Object> run = new ArrayList<>();
        Runnable listener = () -> run.add(new Object());
        ListeningExecutorService executor = MoreExecutors.sameThreadExecutor();
        RedisAsyncConnection<String, String> connection = client.connectAsync();
        RedisFuture<String> set = connection.set(key, value);
        set.get();
        assertThat(set.get(), equalTo("OK"));
        set.addListener(listener, executor);
        assertThat(run, hasSize(1));
    }

    @Test(timeout = 100)
    public void discardCompletesFutures() throws Exception {
        async.multi();
        Future<String> set = async.set(key, value);
        async.discard();
        assertThat(set.get(), nullValue());
    }

    @Test(timeout = 10000)
    public void awaitAll() throws Exception {
        Future<String> get1 = async.get(key);
        Future<String> set = async.set(key, value);
        Future<String> get2 = async.get(key);
        Future<Long> append = async.append(key, value);
        assertThat(LettuceFutures.awaitAll(1, TimeUnit.SECONDS, get1, set, get2, append), equalTo(true));
        assertThat(get1.get(), equalTo(null));
        assertThat(set.get(), equalTo("OK"));
        assertThat(get2.get(), equalTo(value));
        assertThat(append.get(), equalTo(value.length() * 2));
    }

    @Test(timeout = 100)
    public void awaitAllTimeout() throws Exception {
        Future<KeyValue<String, String>> blpop = async.blpop(1, key);
        assertThat(LettuceFutures.awaitAll(1, TimeUnit.NANOSECONDS, blpop), equalTo(false));
    }

}
