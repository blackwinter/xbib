
package org.xbib.io.redis;

import java.util.Arrays;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Utf8StringCodecTest extends AbstractCommandTest {
    @Test
    public void decodeHugeBuffer() throws Exception {
        char[] huge = new char[8192];
        Arrays.fill(huge, 'A');
        String value = new String(huge);
        redis.set(key, value);
        assertThat(redis.get(key), equalTo(value));
    }
}
