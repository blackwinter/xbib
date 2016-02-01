package org.xbib.util.concurrent;

import org.junit.Assert;
import org.junit.Test;

public class ForkJoinPipelineTest extends Assert {

    @Test
    public void testForkJoin() throws Exception {
        MockWorker worker = new MockWorker();
        worker.bootstrap();
        assertTrue(worker.getCount() == 3);
    }
}
