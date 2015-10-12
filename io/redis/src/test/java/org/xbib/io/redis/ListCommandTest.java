
package org.xbib.io.redis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ListCommandTest extends AbstractCommandTest {
    @Test
    public void blpop() throws Exception {
        redis.rpush("two", "2", "3");
        assertThat(redis.blpop(1, "one", "two")).isEqualTo(kv("two", "2"));
    }

    @Test
    public void blpopTimeout() throws Exception {
        redis.setTimeout(10, TimeUnit.SECONDS);
        assertThat(redis.blpop(1, key)).isNull();
    }

    @Test
    public void brpop() throws Exception {
        redis.rpush("two", "2", "3");
        assertThat(redis.brpop(1, "one", "two")).isEqualTo(kv("two", "3"));
    }

    @Test
    public void brpoplpush() throws Exception {
        redis.rpush("one", "1", "2");
        redis.rpush("two", "3", "4");
        assertThat(redis.brpoplpush(1, "one", "two")).isEqualTo("2");
        assertThat(redis.lrange("one", 0, -1)).isEqualTo(list("1"));
        assertThat(redis.lrange("two", 0, -1)).isEqualTo(list("2", "3", "4"));
    }

    @Test
    public void brpoplpushTimeout() throws Exception {
        assertThat(redis.brpoplpush(1, "one", "two")).isNull();
    }

    @Test
    public void lindex() throws Exception {
        assertThat(redis.lindex(key, 0)).isNull();
        redis.rpush(key, "one");
        assertThat(redis.lindex(key, 0)).isEqualTo("one");
    }

    @Test
    public void linsert() throws Exception {
        assertThat(redis.linsert(key, false, "one", "two")).isEqualTo(0);
        redis.rpush(key, "one");
        redis.rpush(key, "three");
        assertThat(redis.linsert(key, true, "three", "two")).isEqualTo(3);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("one", "two", "three"));
    }

    @Test
    public void llen() throws Exception {
        assertThat((long) redis.llen(key)).isEqualTo(0);
        redis.lpush(key, "one");
        assertThat((long) redis.llen(key)).isEqualTo(1);
    }

    @Test
    public void lpop() throws Exception {
        assertThat(redis.lpop(key)).isNull();
        redis.rpush(key, "one", "two");
        assertThat(redis.lpop(key)).isEqualTo("one");
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("two"));
    }

    @Test
    public void lpush() throws Exception {
        assertThat((long) redis.lpush(key, "two")).isEqualTo(1);
        assertThat((long) redis.lpush(key, "one")).isEqualTo(2);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("one", "two"));
        assertThat((long) redis.lpush(key, "three", "four")).isEqualTo(4);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("four", "three", "one", "two"));
    }

    @Test
    public void lpushx() throws Exception {
        assertThat((long) redis.lpushx(key, "two")).isEqualTo(0);
        redis.lpush(key, "two");
        assertThat((long) redis.lpushx(key, "one")).isEqualTo(2);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("one", "two"));
    }

    @Test
    public void lrange() throws Exception {
        assertThat(redis.lrange(key, 0, 10).isEmpty()).isTrue();
        redis.rpush(key, "one", "two", "three");
        List<String> range = redis.lrange(key, 0, 1);
        assertThat(range).hasSize(2);
        assertThat(range.get(0)).isEqualTo("one");
        assertThat(range.get(1)).isEqualTo("two");
        assertThat(redis.lrange(key, 0, -1)).hasSize(3);
    }

    @Test
    public void lrangeStreaming() throws Exception {
        assertThat(redis.lrange(key, 0, 10).isEmpty()).isTrue();
        redis.rpush(key, "one", "two", "three");

        ListStreamingAdapter<String> adapter = new ListStreamingAdapter<String>();

        Long count = redis.lrange(adapter, key, 0, 1);
        assertThat(count.longValue()).isEqualTo(2);

        List<String> range = adapter.getList();

        assertThat(range).hasSize(2);
        assertThat(range.get(0)).isEqualTo("one");
        assertThat(range.get(1)).isEqualTo("two");
        assertThat(redis.lrange(key, 0, -1)).hasSize(3);
    }

    @Test
    public void lrem() throws Exception {
        assertThat(redis.lrem(key, 0, value)).isEqualTo(0);

        redis.rpush(key, "1", "2", "1", "2", "1");
        assertThat((long) redis.lrem(key, 1, "1")).isEqualTo(1);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("2", "1", "2", "1"));

        redis.lpush(key, "1");
        assertThat((long) redis.lrem(key, -1, "1")).isEqualTo(1);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("1", "2", "1", "2"));

        redis.lpush(key, "1");
        assertThat((long) redis.lrem(key, 0, "1")).isEqualTo(3);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("2", "2"));
    }

    @Test
    public void lset() throws Exception {
        redis.rpush(key, "one", "two", "three");
        assertThat(redis.lset(key, 2, "san")).isEqualTo("OK");
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("one", "two", "san"));
    }

    @Test
    public void ltrim() throws Exception {
        redis.rpush(key, "1", "2", "3", "4", "5", "6");
        assertThat(redis.ltrim(key, 0, 3)).isEqualTo("OK");
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("1", "2", "3", "4"));
        assertThat(redis.ltrim(key, -2, -1)).isEqualTo("OK");
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("3", "4"));
    }

    @Test
    public void rpop() throws Exception {
        assertThat(redis.rpop(key)).isNull();
        redis.rpush(key, "one", "two");
        assertThat(redis.rpop(key)).isEqualTo("two");
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("one"));
    }

    @Test
    public void rpoplpush() throws Exception {
        assertThat(redis.rpoplpush("one", "two")).isNull();
        redis.rpush("one", "1", "2");
        redis.rpush("two", "3", "4");
        assertThat(redis.rpoplpush("one", "two")).isEqualTo("2");
        assertThat(redis.lrange("one", 0, -1)).isEqualTo(list("1"));
        assertThat(redis.lrange("two", 0, -1)).isEqualTo(list("2", "3", "4"));
    }

    @Test
    public void rpush() throws Exception {
        assertThat((long) redis.rpush(key, "one")).isEqualTo(1);
        assertThat((long) redis.rpush(key, "two")).isEqualTo(2);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("one", "two"));
        assertThat((long) redis.rpush(key, "three", "four")).isEqualTo(4);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("one", "two", "three", "four"));
    }

    @Test
    public void rpushx() throws Exception {
        assertThat((long) redis.rpushx(key, "one")).isEqualTo(0);
        redis.rpush(key, "one");
        assertThat((long) redis.rpushx(key, "two")).isEqualTo(2);
        assertThat(redis.lrange(key, 0, -1)).isEqualTo(list("one", "two"));
    }
}
