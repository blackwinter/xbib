
package org.xbib.io.redis;

import static org.xbib.io.redis.ScriptOutputType.*;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScriptingCommandTest extends AbstractCommandTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @After
    public void closeConnection() throws Exception {
        try {
            redis.scriptKill();
        } catch (RedisException e) {
            //
        }
        super.closeConnection();
    }

    @Test
    public void eval() throws Exception {
        assertThat((boolean)redis.eval("return 1 + 1 == 4", BOOLEAN)).isFalse();
        assertThat((int)redis.eval("return 1 + 1", INTEGER)).isEqualTo(2);
        assertThat((String)redis.eval("return {ok='status'}", STATUS)).isEqualTo("status");
        assertThat((String)redis.eval("return 'one'", VALUE)).isEqualTo("one");
        assertThat((List)redis.eval("return {1, 'one', {2}}", MULTI)).isEqualTo(list(1L, "one", list(2L)));
        exception.expectMessage("Oops!");
        redis.eval("return {err='Oops!'}", STATUS);
    }

    @Test
    public void evalWithKeys() throws Exception {
        assertThat((List)redis.eval("return {KEYS[1], KEYS[2]}", MULTI, "one", "two")).isEqualTo(list("one", "two"));
    }

    @Test
    public void evalWithArgs() throws Exception {
        String[] keys = new String[0];
        assertThat((List)redis.eval("return {ARGV[1], ARGV[2]}", MULTI, keys, "a", "b")).isEqualTo(list("a", "b"));
    }

    @Test
    public void evalsha() throws Exception {
        redis.scriptFlush();
        String script = "return 1 + 1";
        String digest = redis.digest(script);
        assertThat((int)redis.eval(script, INTEGER)).isEqualTo(2);
        assertThat((int)redis.evalsha(digest, INTEGER)).isEqualTo(2);
        exception.expectMessage("NOSCRIPT No matching script. Please use EVAL.");
        redis.evalsha(redis.digest("return 1 + 1 == 4"), INTEGER);
    }

    @Test
    public void evalshaWithKeys() throws Exception {
        redis.scriptFlush();
        String digest = redis.scriptLoad("return {KEYS[1], KEYS[2]}");
        assertThat((List)redis.evalsha(digest, MULTI, "one", "two")).isEqualTo(list("one", "two"));
    }

    @Test
    public void evalshaWithArgs() throws Exception {
        redis.scriptFlush();
        String digest = redis.scriptLoad("return {ARGV[1], ARGV[2]}");
        String[] keys = new String[0];
        assertThat((List)redis.evalsha(digest, MULTI, keys, "a", "b")).isEqualTo(list("a", "b"));
    }

    @Test
    public void script() throws Exception {
        assertThat(redis.scriptFlush()).isEqualTo("OK");

        String script1 = "return 1 + 1";
        String digest1 = redis.digest(script1);
        String script2 = "return 1 + 1 == 4";
        String digest2 = redis.digest(script2);

        assertThat(redis.scriptExists(digest1, digest2)).isEqualTo(list(false, false));
        assertThat(redis.scriptLoad(script1)).isEqualTo(digest1);
        assertThat((int)redis.evalsha(digest1, INTEGER)).isEqualTo(2);
        assertThat(redis.scriptExists(digest1, digest2)).isEqualTo(list(true, false));

        assertThat(redis.scriptFlush()).isEqualTo("OK");
        assertThat(redis.scriptExists(digest1, digest2)).isEqualTo(list(false, false));

        redis.configSet("lua-time-limit", "10");
        try (RedisAsyncConnection<String, String> async = client.connectAsync()) {
            async.eval("while true do end", STATUS);
            Thread.sleep(100);
            assertThat(redis.scriptKill()).isEqualTo("OK");
        }
    }
}
