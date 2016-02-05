
package org.xbib.io.redis;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class NumericCommandTest extends AbstractCommandTest {
    @Test
    public void decr() throws Exception {
        assertThat((long) redis.decr(key)).isEqualTo(-1);
        assertThat((long) redis.decr(key)).isEqualTo(-2);
    }

    @Test
    public void decrby() throws Exception {
        assertThat(redis.decrby(key, 3)).isEqualTo(-3);
        assertThat(redis.decrby(key, 3)).isEqualTo(-6);
    }

    @Test
    public void incr() throws Exception {
        assertThat((long) redis.incr(key)).isEqualTo(1);
        assertThat((long) redis.incr(key)).isEqualTo(2);
    }

    @Test
    public void incrby() throws Exception {
        assertThat(redis.incrby(key, 3)).isEqualTo(3);
        assertThat(redis.incrby(key, 3)).isEqualTo(6);
    }

    @Test
    public void incrbyfloat() throws Exception {

        assertThat(redis.incrbyfloat(key, 3.0)).isEqualTo(3.0, offset(0.1));
        assertThat(redis.incrbyfloat(key, 0.2)).isEqualTo(3.2, offset(0.1));
    }
}
