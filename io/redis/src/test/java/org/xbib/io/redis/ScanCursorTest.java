package org.xbib.io.redis;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScanCursorTest {

    @Test
    public void testFactory() throws Exception {
        ScanCursor scanCursor = ScanCursor.of("dummy");
        assertThat(scanCursor.getCursor()).isEqualTo("dummy");
        assertThat(scanCursor.isFinished()).isFalse();
    }
}
